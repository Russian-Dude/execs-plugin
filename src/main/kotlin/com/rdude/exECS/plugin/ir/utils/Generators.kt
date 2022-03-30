package com.rdude.exECS.plugin.ir.utils

import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.backend.common.ir.copyTo
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlockBody
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.addMember
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.name.Name

fun IrClass.createPropertyWithBackingField(
    name: String,
    type: IrType,
    isVar: Boolean = true,
    isLateInit: Boolean = false,
): IrProperty {

    val property = this.factory.buildProperty {
        this.startOffset = this@createPropertyWithBackingField.startOffset
        this.endOffset = this@createPropertyWithBackingField.endOffset
        this.name = Name.identifier(name)
        this.isVar = isVar
        this.isLateinit = isLateInit
        this.visibility = DescriptorVisibilities.PUBLIC
    }

    val field = MetaData.context.irFactory.buildField {
        this.startOffset = this@createPropertyWithBackingField.startOffset
        this.endOffset = this@createPropertyWithBackingField.endOffset
        this.type = type
        this.isFinal = !isVar
        this.name = Name.identifier(name)
        this.origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD
    }

    val declarationIrBuilder = DeclarationIrBuilder(MetaData.context, field.symbol)

    val accessorFun = IR_FACTORY.buildFun {
        this.startOffset = this@createPropertyWithBackingField.startOffset
        this.endOffset = this@createPropertyWithBackingField.endOffset
        this.name = Name.special("<get-$name>")
        this.returnType = type
        this.origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    }.also { function ->
        function.dispatchReceiverParameter = this.thisReceiver!!.copyTo(function)

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

    property.getter = accessorFun

    property.backingField = field
    field.correspondingPropertySymbol = property.symbol

    property.parent = this
    this.addMember(property)
    field.parent = this
    accessorFun.parent = this

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