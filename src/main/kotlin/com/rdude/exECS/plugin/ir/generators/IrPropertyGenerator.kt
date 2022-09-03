package com.rdude.exECS.plugin.ir.generators

import com.rdude.exECS.plugin.ir.utils.IR_FACTORY
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.isSynthesized
import org.jetbrains.kotlin.backend.common.ir.copyTo
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildProperty
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.Name

object IrPropertyGenerator {

    /** @return generated property or existing property if exist and [forceOverride] is false.*/
    fun generatePropertyWithBackingField(
        inClass: IrClass,
        name: String,
        type: IrType,
        isVar: Boolean = true,
        isFinal: Boolean = false,
        isLateInit: Boolean = false,
        visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC,
        annotations: List<IrConstructorCall> = listOf(),
        overridden: List<IrPropertySymbol>? = null,
        initializer: IrExpressionBody? = null,
        forceOverride: Boolean = false // if false and property exists, new property will not be generated
    ): IrProperty {

        val existingProperty =
            inClass.properties.find { it.name.asString() == name }

        if (existingProperty != null && !existingProperty.isSynthesized() && !existingProperty.isFakeOverride && !forceOverride) {
            return existingProperty
        }

        val property = inClass.properties.firstOrNull { it.name.asString() == name && !it.isFakeOverride }
            ?: MetaData.context.irFactory.buildProperty {
                this.startOffset = inClass.startOffset
                this.endOffset = inClass.endOffset
                this.name = Name.identifier(name)
                this.isVar = isVar
                this.isLateinit = isLateInit
                this.visibility = visibility
            }

        val field = MetaData.context.irFactory.buildField {
            this.startOffset = inClass.startOffset
            this.endOffset = inClass.endOffset
            this.type = type
            this.isFinal = isFinal
            this.name = Name.identifier(name)
            this.origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD
        }

        val declarationIrBuilder = DeclarationIrBuilder(MetaData.context, field.symbol)

        val getterFun = property.getter ?: IR_FACTORY.buildFun {
            this.startOffset = inClass.startOffset
            this.endOffset = inClass.endOffset
            this.name = Name.special("<get-$name>")
            this.returnType = type
            this.origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
        }

        getterFun.dispatchReceiverParameter = inClass.thisReceiver!!.copyTo(getterFun)
        overridden?.let { getterFun.overriddenSymbols = listOf(it.first().owner.getter!!.symbol) }
        getterFun.correspondingPropertySymbol = property.symbol
        getterFun.body = DeclarationIrBuilder(MetaData.context, getterFun.symbol).irBlockBody {
            +this.irReturn(
                declarationIrBuilder.irGetField(
                    this.irGet(getterFun.dispatchReceiverParameter!!),
                    field
                )
            )
        }

        if (isVar) {
            val setterFun = IR_FACTORY.buildFun {
                this.startOffset = inClass.startOffset
                this.endOffset = inClass.endOffset
                this.name = Name.special("<set-$name>")
                this.returnType = MetaData.context.irBuiltIns.unitType
                this.origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
            }.also { function ->
                function.dispatchReceiverParameter = inClass.thisReceiver!!.copyTo(function)

                val valueParameter = buildValueParameter(function) {
                    this.startOffset = inClass.startOffset
                    this.endOffset = inClass.endOffset
                    this.name = Name.special("<set-?>")
                    this.type = type
                    this.index = 0
                }
                function.valueParameters = listOf(valueParameter)

                overridden?.let { function.overriddenSymbols = listOf(it.first().owner.setter!!.symbol) }

                function.correspondingPropertySymbol = property.symbol
                function.body = DeclarationIrBuilder(MetaData.context, function.symbol).irBlockBody {
                    +this.irSetField(
                        receiver = irGet(function.dispatchReceiverParameter!!),
                        field = field,
                        value = irGet(valueParameter)
                    )
                }
            }

            property.setter = setterFun
            property.setter!!.parent = inClass
        }

        property.getter = getterFun

        overridden?.let {
            property.overriddenSymbols = it
        }

        initializer?.let { field.initializer = it }

        property.backingField = field
        field.correspondingPropertySymbol = property.symbol

        property.annotations = annotations
        field.annotations = annotations

        property.parent = inClass
        property.backingField!!.parent = inClass
        property.getter!!.parent = inClass

        return property
    }

}