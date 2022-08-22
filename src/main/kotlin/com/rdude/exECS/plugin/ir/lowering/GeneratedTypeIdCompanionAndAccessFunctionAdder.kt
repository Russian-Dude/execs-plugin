package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.describer.*
import com.rdude.exECS.plugin.ir.transform.GetTypeIdFakeToCompanionBasedFunctionTransformer
import com.rdude.exECS.plugin.ir.utils.IR_FACTORY
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createAndAddPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.createCompanionObject
import org.jetbrains.kotlin.backend.common.ir.copyTo
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

class GeneratedTypeIdCompanionAndAccessFunctionAdder {

    private val functionTransformer = GetTypeIdFakeToCompanionBasedFunctionTransformer()

    fun addTo(irClasses: Collection<IrClass>, type: HasTypeId) {
        for (cl in irClasses) {
            if (cl.modality == Modality.ABSTRACT || cl.isInterface) continue
            val companion =
                if (cl.isObject) cl
                else cl.companionObject() ?: cl.createCompanionObject()
            val property = addIdPropertyTo(companion, cl, type)
            addGetIdFunction(cl, companion, property, type)
        }
    }


    private fun addIdPropertyTo(toCompanion: IrClass, inClass: IrClass, type: HasTypeId): IrProperty {

        val builder = DeclarationIrBuilder(MetaData.context, toCompanion.symbol, toCompanion.startOffset, toCompanion.endOffset)

        val getGeneratedByExEcsIdMethodDescriber = when(type) {
            Component -> ExEcsGeneratedCallsObject.getComponentTypeIdFun
            Event -> ExEcsGeneratedCallsObject.getEventTypeIdFun
            else -> throw NotImplementedError("GetTypeId for ${type.irType.classFqName} is not implemented in ExEcsGeneratedCallsObject")
        }

        return toCompanion.createAndAddPropertyWithBackingField(
            name = "execs_generated_${type.name.toLowerCase()}_type_id_property_for_${inClass.kotlinFqName.asString().replace(".", "_")}",
            type = MetaData.context.irBuiltIns.intType,
            isVar = false,
            isLateInit = false,
            isFinal = true,
            annotations = listOf(
                Kotlin.TransientAnnotation.constructorCall(),
                // JvmField does not work properly with synthetic properties. Need to try again when new K2 compiler is out
                //Kotlin.JvmFieldAnnotation.constructorCall(),
                ExEcsAnnotations.GeneratedTypeIdProperty.constructorCall(type.irType, inClass.defaultType)
            ),
            initializer = builder.irExprBody(
                builder.irCall(getGeneratedByExEcsIdMethodDescriber.single())
                    .apply {
                        this.type = MetaData.context.irBuiltIns.intType
                        dispatchReceiver = builder.irGetObject(ExEcsGeneratedCallsObject.symbol)
                        putTypeArgument(0, inClass.defaultType)
                    }
            )
        )
    }


    private fun addGetIdFunction(irClass: IrClass, companion: IrClass, idProperty: IrProperty, type: HasTypeId) {
        val function = IR_FACTORY.buildFun {
            this.name = Name.identifier("get${type.name.capitalize()}TypeId")
            this.visibility = DescriptorVisibilities.PUBLIC
            this.modality = Modality.OPEN
            this.returnType = MetaData.context.irBuiltIns.intType
            this.startOffset = irClass.startOffset
            this.endOffset = irClass.endOffset
        }.apply {
            dispatchReceiverParameter = irClass.thisReceiver!!.copyTo(this)
            overriddenSymbols = type.getTypeIdFun.symbols
            parent = irClass
        }


        val builder = DeclarationIrBuilder(MetaData.context, function.symbol, function.startOffset, function.endOffset)

        val getIdCall = builder.irCall(idProperty.getter!!)
            .apply {
                dispatchReceiver = builder.irGetObject(companion.symbol)
                this.type = MetaData.context.irBuiltIns.intType
            }

        function.body = builder.irBlockBody {
            +irReturn(
                getIdCall
            )
        }

        functionTransformer.transformTo(function, irClass, type)
    }


}