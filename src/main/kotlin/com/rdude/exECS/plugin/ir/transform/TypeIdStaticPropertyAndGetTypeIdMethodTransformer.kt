package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.*
import com.rdude.exECS.plugin.ir.utils.*
import org.jetbrains.kotlin.backend.common.ir.copyTo
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

class TypeIdStaticPropertyAndGetTypeIdMethodTransformer : IrTransformerElement() {

    private val fieldsForTransform = mutableMapOf<IrField, IrField>()
    private val gettersForTransform = mutableMapOf<IrFunction, IrFunction>()


    override fun visitProperty(property: IrProperty) {
        if (!property.isSynthesized()) return
        val companion = currentClass ?: return
        if (!companion.isCompanion && !companion.isObject) return
        val parentCl = if (companion.isCompanion) companion.parentAsClass else companion

        val superDescriber =
            if (parentCl.isSubclassOf(Event)) Event
            else if (parentCl.isSubclassOf(Component)) Component
            else return

        val getGeneratedByExEcsIdMethodDescriber = when (superDescriber) {
            Component -> ExEcsGeneratedCallsObject.getComponentTypeIdFun
            Event -> ExEcsGeneratedCallsObject.getEventTypeIdFun
            else -> return
        }

        if (property.name.asString() != superDescriber.typeIdPropertyNameFor(parentCl)) return

        val builder =
            DeclarationIrBuilder(MetaData.context, companion.symbol, companion.startOffset, companion.endOffset)

        val resultProperty = companion.createPropertyWithBackingField(
            name = superDescriber.typeIdPropertyNameFor(parentCl),
            type = MetaData.context.irBuiltIns.intType,
            isVar = false,
            isLateInit = false,
            isFinal = true,
            initializer = builder.irExprBody(
                builder.irCall(getGeneratedByExEcsIdMethodDescriber.single())
                    .apply {
                        this.type = MetaData.context.irBuiltIns.intType
                        dispatchReceiver = builder.irGetObject(ExEcsGeneratedCallsObject.symbol)
                        putTypeArgument(0, parentCl.defaultType)
                    }
            ),
            annotations = listOf(
                Kotlin.TransientAnnotation.constructorCall(),
                // JvmField does not work properly with synthetic properties. Need to try again when new K2 compiler is out
                //Kotlin.JvmFieldAnnotation.constructorCall(),
                ExEcsAnnotations.GeneratedTypeIdProperty.constructorCall(superDescriber.irType, parentCl.defaultType)
            )
        )

        val getTypeIdFunction = parentCl.functions.find { it.name.asString() == superDescriber.getTypeIdMethodName }!!
        if (getTypeIdFunction.isFakeOverride) {

            val transformedFunction = IR_FACTORY.buildFun {
                this.name = Name.identifier(superDescriber.getTypeIdMethodName)
                this.visibility = DescriptorVisibilities.PUBLIC
                this.modality = Modality.OPEN
                this.returnType = MetaData.context.irBuiltIns.intType
                this.startOffset = parentCl.startOffset
                this.endOffset = parentCl.endOffset
            }.apply {
                dispatchReceiverParameter = parentCl.thisReceiver!!.copyTo(this)
                overriddenSymbols = superDescriber.getTypeIdFun.symbols
                parent = parentCl
            }

            val builderWithFunctionScope =
                DeclarationIrBuilder(
                    MetaData.context,
                    transformedFunction.symbol,
                    transformedFunction.startOffset,
                    transformedFunction.endOffset
                )


            val getIdCall = builderWithFunctionScope.irCall(resultProperty.getter!!)
                .apply {
                    dispatchReceiver = builderWithFunctionScope.irGetObject(companion.symbol)
                    this.type = MetaData.context.irBuiltIns.intType
                }

            transformedFunction.body = builderWithFunctionScope.irBlockBody {
                +irReturn(
                    getIdCall
                )
            }

            transformLazy(from = getTypeIdFunction, to = transformedFunction)
        }

        fieldsForTransform[property.backingField!!] = resultProperty.backingField!!
        gettersForTransform[property.getter!!] = resultProperty.getter!!

        transformCurrent(resultProperty)
    }


    override fun visitField(field: IrField) {
        val transformed = fieldsForTransform[field] ?: return
        transformCurrent(transformed)
    }


    override fun visitFunction(function: IrFunction) {
        if (function.isGetter) {
            val transformed = gettersForTransform[function] ?: return
            transformCurrent(transformed)
        }
    }

}