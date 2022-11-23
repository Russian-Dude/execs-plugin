package com.rdude.exECS.plugin.ir.utils

import com.rdude.exECS.plugin.ir.generators.IrPropertyGenerator
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlockBody
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.addMember
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

fun IrClass.createPropertyWithBackingField(
    name: String,
    type: IrType,
    isVar: Boolean = true,
    isFinal: Boolean = false,
    isLateInit: Boolean = false,
    visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC,
    annotations: List<IrConstructorCall> = listOf(),
    overridden: List<IrPropertySymbol>? = null,
    initializer: IrExpressionBody? = null
): IrProperty {

    return IrPropertyGenerator.generatePropertyWithBackingField(
        this,
        name,
        type,
        isVar,
        isFinal,
        isLateInit,
        visibility,
        annotations,
        overridden,
        initializer
    )
}

fun IrClass.createAndAddPropertyWithBackingField(
    name: String,
    type: IrType,
    isVar: Boolean = true,
    isFinal: Boolean = false,
    isLateInit: Boolean = false,
    visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC,
    annotations: List<IrConstructorCall> = listOf(),
    overridden: List<IrPropertySymbol>? = null,
    initializer: IrExpressionBody? = null
): IrProperty {

    return createPropertyWithBackingField(
        name, type, isVar, isFinal, isLateInit, visibility, annotations, overridden, initializer
    ).apply { this@createAndAddPropertyWithBackingField.addMember(this) }
}


fun IrClass.createCompanionObject(
    name: String = "Companion"
): IrClass {

    companionObject()?.let { return it }

    val companion = IR_FACTORY.buildClass {
        this.isCompanion = true
        this.name = Name.identifier(name)
        this.modality = Modality.FINAL
        this.visibility = DescriptorVisibilities.PUBLIC
        this.kind = ClassKind.OBJECT
        this.isCompanion = true
        this.startOffset = this@createCompanionObject.startOffset
        this.endOffset = this@createCompanionObject.endOffset
    }

    companion.createImplicitParameterDeclarationWithWrappedDescriptor()

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