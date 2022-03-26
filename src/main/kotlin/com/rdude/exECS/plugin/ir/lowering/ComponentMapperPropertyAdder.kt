package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.ir.utils.IR_FACTORY
import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.ir.copyTo
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildProperty
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.addMember
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class ComponentMapperPropertyAdder : ClassLoweringPass {

    private lateinit var currentType: IrType
    private lateinit var currentResultProperty: IrProperty
    private val addedProperties: MutableMap<IrClass, MutableMap<IrType, IrProperty>> = HashMap()

    fun addIfAbsent(irClass: IrClass, irType: IrType): IrProperty {
        val alreadyAdded = addedProperties[irClass]?.get(irType)
        if (alreadyAdded != null) return alreadyAdded

        currentType = irType
        lower(irClass)
        addedProperties.putIfAbsent(irClass, HashMap())
        addedProperties[irClass]!![irType] = currentResultProperty
        return currentResultProperty
    }

    override fun lower(irClass: IrClass) {

        val typeArgumentString = currentType.classFqName!!.asString().replace(".", "_")
        val idPropertyName = "generated_component_mapper_for_$typeArgumentString"
        val thisPropertyType = MetaData.context.referenceClass(FqName("com.rdude.exECS.component.ComponentMapper"))!!
            .typeWith(currentType)

        val property = irClass.factory.buildProperty {
            this.startOffset = irClass.startOffset
            this.endOffset = irClass.endOffset
            this.name = Name.identifier(idPropertyName)
            this.isVar = true
            this.isLateinit = true
            this.visibility = DescriptorVisibilities.PUBLIC
        }

        val field = MetaData.context.irFactory.buildField {
            this.startOffset = irClass.startOffset
            this.endOffset = irClass.endOffset
            this.type = thisPropertyType
            this.isFinal = false
            this.name = Name.identifier(idPropertyName)
            this.origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD
        }

        val declarationIrBuilder = DeclarationIrBuilder(MetaData.context, field.symbol)

        // default property accessor
        val accessorFun = IR_FACTORY.buildFun {
            this.startOffset = irClass.startOffset
            this.endOffset = irClass.endOffset
            this.name = Name.special("<get-$idPropertyName>")
            this.returnType = thisPropertyType
            this.origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
        }.also { function ->
            function.dispatchReceiverParameter = irClass.thisReceiver!!.copyTo(function)

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

        property.parent = irClass
        irClass.addMember(property)
        field.parent = irClass
        accessorFun.parent = irClass

        currentResultProperty = property
    }


}