package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.ExEcsAnnotations
import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.Pool
import com.rdude.exECS.plugin.describer.Poolable
import com.rdude.exECS.plugin.ir.utils.*
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

class DefaultPoolsAdder : IrTransformerElement() {

    private val poolFieldsForTransform = mutableMapOf<IrField, IrField>()
    private val poolGettersForTransform = mutableMapOf<IrFunction, IrFunction>()


    override fun visitProperty(property: IrProperty) {
        if (!property.isSynthesized()) return
        val companion = currentClass ?: return
        if (!companion.isCompanion) return
        val parentCl = companion.parentAsClass
        if (!parentCl.isSubclassOf(Poolable)) return
        if (property.name.asString() != Poolable.generatedDefaultPoolNameFor(parentCl)) return

        val poolType = Pool.irTypeWith(parentCl.defaultType)

        // find which constructor plugin interested in
        var constructorSymbol: IrConstructorSymbol? = null

        // prefer constructor annotated with @ConstructorForDefaultPool
        val annotatedConstructors = parentCl.constructors
            .filter { it.hasAnnotation(ExEcsAnnotations.ConstructorForDefaultPool.symbol) }
            .distinct()
            .toList()
        if (annotatedConstructors.size == 1) {
            val irConstructor = annotatedConstructors[0]
            val valueParameters = irConstructor.valueParameters
            if (valueParameters.isNotEmpty() && valueParameters.any { !it.hasDefaultValue() }) {
                throw IllegalStateException(
                    "All arguments presented in the constructor annotated with @ConstructorForDefaultPool annotation must have a default value. " +
                            "Annotated constructor in ${parentCl.kotlinFqName.asString()} has arguments without a default value."
                )
            }
            constructorSymbol = irConstructor.symbol
        } else if (annotatedConstructors.size > 1) {
            throw IllegalStateException(
                "Only one constructor per class can be annotated with @ConstructorForDefaultPool annotation. " +
                        "${annotatedConstructors.size} constructors with this annotation found in ${parentCl.kotlinFqName.asString()}"
            )
        }

        // then prefer primary constructor
        if (constructorSymbol == null && parentCl.primaryConstructor != null) {
            val valueParameters = parentCl.primaryConstructor!!.valueParameters
            if (valueParameters.isEmpty() || valueParameters.all { it.hasDefaultValue() }) {
                constructorSymbol = parentCl.primaryConstructor!!.symbol
            }
        }

        // then any other with no args or with all optional args
        if (constructorSymbol == null) {
            constructorSymbol = parentCl.constructors
                .filter { con -> con.valueParameters.isEmpty() || con.valueParameters.all { it.hasDefaultValue() } }
                .firstOrNull()?.symbol
                ?: return
        }

        val builder =
            DeclarationIrBuilder(MetaData.context, companion.symbol, companion.startOffset, companion.endOffset)

        val poolableConstructorCall = builder.irCallConstructor(constructorSymbol, listOf())
        poolableConstructorCall.type = parentCl.defaultType

        val function = IR_FACTORY.buildFun {
            this.startOffset = Pool.companion.startOffset
            this.endOffset = Pool.companion.endOffset
            this.returnType = parentCl.defaultType
            this.name = Name.identifier("<anonymous>")
            this.visibility = DescriptorVisibilities.LOCAL
            this.origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
        }
        function.body = DeclarationIrBuilder(
            MetaData.context,
            function.symbol,
            function.startOffset,
            function.endOffset
        ).irBlockBody {
            +irReturn(poolableConstructorCall)
        }

        val supplierExpression = IrFunctionExpressionImpl(
            startOffset = parentCl.startOffset,
            endOffset = parentCl.endOffset,
            type = MetaData.context.irBuiltIns.functionN(0).typeWith(parentCl.defaultType),
            function = function,
            origin = IrStatementOrigin.LAMBDA
        )

        val initializerCall =
            builder.buildStatement(
                Pool.companion.startOffset,
                Pool.companion.endOffset,
                origin = IrStatementOrigin.INVOKE
            ) {
                val irCall = this.irCall(Pool.invokeFun.single())
                irCall.type = poolType
                irCall.putTypeArgument(0, parentCl.defaultType)
                irCall.dispatchReceiver = builder.irGetObject(Pool.companion.symbol)
                irCall.putValueArgument(0, supplierExpression)
                return@buildStatement irCall
            }


        val resultProperty = companion.createPropertyWithBackingField(
            name = Poolable.generatedDefaultPoolNameFor(parentCl),
            type = poolType,
            isVar = false,
            isLateInit = false,
            isFinal = true,
            initializer = builder.irExprBody(initializerCall),
            annotations = listOf(
                Kotlin.TransientAnnotation.constructorCall(),
                // JvmField does not work properly with synthetic properties. Need to try again when new K2 compiler is out
                //Kotlin.JvmFieldAnnotation.constructorCall(),
                ExEcsAnnotations.GeneratedDefaultPoolProperty.constructorCall(parentCl.defaultType)
            )
        )

        function.parent = resultProperty.backingField!!

        poolFieldsForTransform[property.backingField!!] = resultProperty.backingField!!
        poolGettersForTransform[property.getter!!] = resultProperty.getter!!

        transformCurrent(resultProperty)
    }


    override fun visitField(field: IrField) {
        val transformed = poolFieldsForTransform[field] ?: return
        transformCurrent(transformed)
    }


    override fun visitFunction(function: IrFunction) {
        if (!function.isGetter) return
        val transformed = poolGettersForTransform[function] ?: return
        transformCurrent(transformed)
    }
}