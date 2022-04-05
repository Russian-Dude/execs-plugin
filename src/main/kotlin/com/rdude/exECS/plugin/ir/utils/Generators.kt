package com.rdude.exECS.plugin.ir.utils

import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.backend.common.ir.copyTo
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlockBody
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.addMember
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.name.Name

fun createPropertyWithBackingField(
    inClass: IrClass,
    name: String,
    type: IrType,
    isVar: Boolean = true,
    isFinal: Boolean = false,
    isLateInit: Boolean = false,
    overridden: List<IrPropertySymbol>? = null,
    initializer: IrExpressionBody? = null
): IrProperty {
    val property = MetaData.context.irFactory.buildProperty {
        this.startOffset = inClass.startOffset
        this.endOffset = inClass.endOffset
        this.name = Name.identifier(name)
        this.isVar = isVar
        this.isLateinit = isLateInit
        this.visibility = DescriptorVisibilities.PUBLIC
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

    val getterFun = IR_FACTORY.buildFun {
        this.startOffset = inClass.startOffset
        this.endOffset = inClass.endOffset
        this.name = Name.special("<get-$name>")
        this.returnType = type
        this.origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    }.also { function ->
        function.dispatchReceiverParameter = inClass.thisReceiver!!.copyTo(function)

        overridden?.let { function.overriddenSymbols = listOf(it.first().owner.getter!!.symbol) }

        function.correspondingPropertySymbol = property.symbol
        function.body = DeclarationIrBuilder(MetaData.context, function.symbol).irBlockBody {
            +this.irReturn(
                declarationIrBuilder.irGetField(
                    this.irGet(function.dispatchReceiverParameter!!),
                    field
                )
            )
        }
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

    property.parent = inClass
    property.backingField!!.parent = inClass
    property.getter!!.parent = inClass

    return property
}



fun IrClass.createAndAddPropertyWithBackingField(
    name: String,
    type: IrType,
    isVar: Boolean = true,
    isFinal: Boolean = false,
    isLateInit: Boolean = false,
    overridden: List<IrPropertySymbol>? = null,
    initializer: IrExpressionBody? = null
): IrProperty {

    val property = createPropertyWithBackingField(this, name, type, isVar, isFinal, isLateInit, overridden, initializer)

    this.addMember(property)

    return property
}



fun IrClass.createCompanionObject(
    name: String = "Companion"
): IrClass {

    val companion = IR_FACTORY.buildClass {
        this.isCompanion = true
        this.name = Name.identifier(name)
        this.modality = Modality.FINAL
        this.visibility = DescriptorVisibilities.PUBLIC
        this.kind = ClassKind.OBJECT
        this.startOffset = this@createCompanionObject.startOffset
        this.endOffset = this@createCompanionObject.endOffset
    }

    val receiver = buildValueParameter(companion) {
        this.name = Name.special("<this>")
        this.type = IrSimpleTypeImpl(
            classifier = companion.symbol,
            hasQuestionMark = false,
            arguments = emptyList(),
            annotations = emptyList()
        )
        origin = IrDeclarationOrigin.INSTANCE_RECEIVER
    }
    companion.thisReceiver = receiver
    receiver.parent = companion

    val constructor = companion.addConstructor {
        this.visibility = DescriptorVisibilities.PRIVATE
        this.returnType = companion.defaultType
        this.isPrimary = true
    }

    val builder = DeclarationIrBuilder(MetaData.context, companion.symbol, companion.startOffset, companion.endOffset)

    constructor.body = builder.irBlockBody(constructor) {

        +IrDelegatingConstructorCallImpl(
            startOffset,
            endOffset,
            MetaData.context.irBuiltIns.anyType,
            MetaData.context.irBuiltIns.anyClass.constructors.first(),
            0,
            0
        )

        +IrInstanceInitializerCallImpl(
            startOffset,
            endOffset,
            companion.symbol,
            companion.defaultType
        )
    }

    this.addChild(companion)

    return companion
}