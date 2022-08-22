package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.describer.ExEcsAnnotations
import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.Pool
import com.rdude.exECS.plugin.ir.utils.IR_FACTORY
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createAndAddPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.createCompanionObject
import com.rdude.exECS.plugin.ir.visit.PoolsMapper
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

class GeneratedDefaultPoolAdder(private val existingCompanions: MutableMap<IrClass, IrClass>, private val pools: PoolsMapper) {

    fun addTo(irClasses: Collection<IrClass>) {
        for (cl in irClasses) {
            if (cl.modality == Modality.ABSTRACT || cl.isInner || cl.isEnumClass) continue
            tryAddPoolPropertyTo(cl)
        }
    }

    private fun tryAddPoolPropertyTo(inClass: IrClass) {

        val poolType = Pool.irTypeWith(inClass.defaultType)

        // find which constructor plugin interested in
        var constructorSymbol: IrConstructorSymbol? = null

        // prefer constructor annotated with @ConstructorForDefaultPool
        val annotatedConstructors = inClass.constructors
            .filter { it.hasAnnotation(ExEcsAnnotations.ConstructorForDefaultPool.symbol) }
            .distinct()
            .toList()
        if (annotatedConstructors.size == 1) {
            val irConstructor = annotatedConstructors[0]
            val valueParameters = irConstructor.valueParameters
            if (valueParameters.isNotEmpty() && valueParameters.any { !it.hasDefaultValue() }) {
                throw IllegalStateException(
                    "All arguments presented in the constructor annotated with @ConstructorForDefaultPool annotation must have a default value. " +
                            "Annotated constructor in ${inClass.kotlinFqName.asString()} has arguments without a default value."
                )
            }
            constructorSymbol = irConstructor.symbol
        } else if (annotatedConstructors.size > 1) {
            throw IllegalStateException(
                "Only one constructor per class can be annotated with @ConstructorForDefaultPool annotation. " +
                        "${annotatedConstructors.size} constructors with this annotation found in ${inClass.kotlinFqName.asString()}"
            )
        }

        // then prefer primary constructor
        if (constructorSymbol == null && inClass.primaryConstructor != null) {
            val valueParameters = inClass.primaryConstructor!!.valueParameters
            if (valueParameters.isEmpty() || valueParameters.all { it.hasDefaultValue() }) {
                constructorSymbol = inClass.primaryConstructor!!.symbol
            }
        }

        // then any other with no args or with all optional args
        if (constructorSymbol == null) {
            constructorSymbol =  inClass.constructors
                .filter { con -> con.valueParameters.isEmpty() || con.valueParameters.all { it.hasDefaultValue() } }
                .firstOrNull()?.symbol
                ?: return
        }

        val companion = existingCompanions.getOrPut(inClass) { inClass.createCompanionObject() }

        val builder =
            DeclarationIrBuilder(MetaData.context, companion.symbol, companion.startOffset, companion.endOffset)

        val poolableConstructorCall = builder.irCallConstructor(constructorSymbol, listOf())
        poolableConstructorCall.type = inClass.defaultType

        val function = IR_FACTORY.buildFun {
            this.startOffset = Pool.companion.startOffset
            this.endOffset = Pool.companion.endOffset
            this.returnType = inClass.defaultType
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
            startOffset = inClass.startOffset,
            endOffset = inClass.endOffset,
            type = MetaData.context.irBuiltIns.functionN(0).typeWith(inClass.defaultType),
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
                irCall.putTypeArgument(0, inClass.defaultType)
                irCall.dispatchReceiver = builder.irGetObject(Pool.companion.symbol)
                irCall.putValueArgument(0, supplierExpression)
                return@buildStatement irCall
            }


        val resultProperty = companion.createAndAddPropertyWithBackingField(
            name = "execs_generated_pool_for_${inClass.kotlinFqName.asString().replace(".", "_")}",
            type = poolType,
            isVar = false,
            isLateInit = false,
            isFinal = true,
            initializer = builder.irExprBody(initializerCall),
            annotations = listOf(
                Kotlin.TransientAnnotation.constructorCall(),
                // JvmField does not work properly with synthetic properties. Need to try again when new K2 compiler is out
                //Kotlin.JvmFieldAnnotation.constructorCall(),
                ExEcsAnnotations.GeneratedDefaultPoolProperty.constructorCall(inClass.defaultType)
            )
        )

        function.parent = resultProperty.backingField!!

        pools.store(inClass.defaultType, resultProperty, companion)
    }

}