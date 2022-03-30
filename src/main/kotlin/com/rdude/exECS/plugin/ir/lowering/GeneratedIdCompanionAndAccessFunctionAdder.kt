package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.ir.transform.GetIdFakeToCompanionBasedFunctionTransformer
import com.rdude.exECS.plugin.ir.utils.IR_FACTORY
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createCompanionObject
import com.rdude.exECS.plugin.ir.utils.createPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.reference.Event
import com.rdude.exECS.plugin.ir.utils.reference.HasId
import org.jetbrains.kotlin.backend.common.ir.copyTo
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.Name

class GeneratedIdCompanionAndAccessFunctionAdder(private val existingCompanions: MutableMap<IrClass, IrClass>) {

    private val functionTransformer = GetIdFakeToCompanionBasedFunctionTransformer()

    fun addTo(irClasses: Collection<IrClass>, type: HasId) {
        for (cl in irClasses) {
            val companion = existingCompanions.getOrPut(cl) { cl.createCompanionObject() }
            val property = addIdPropertyTo(companion, cl, type)
            addGetIdFunction(cl, companion, property, type)
        }
    }

    private fun addIdPropertyTo(toCompanion: IrClass, inClass: IrClass, type: HasId): IrProperty {
        return toCompanion.createPropertyWithBackingField(
            name = "execs_generated_${type.idPropertyNamePrefix.toLowerCase()}_id_property_for_${inClass.kotlinFqName.asString().replace(".", "_")}",
            type = MetaData.context.irBuiltIns.intType,
            isVar = true,
            isLateInit = false
        )
    }


    private fun addGetIdFunction(irClass: IrClass, companion: IrClass, idProperty: IrProperty, type: HasId) {
        val function = IR_FACTORY.buildFun {
            this.name = Name.identifier("get${type.idPropertyNamePrefix.toUpperCase()}TypeId")
            this.visibility = DescriptorVisibilities.PUBLIC
            this.modality = Modality.OPEN
            this.returnType = MetaData.context.irBuiltIns.intType
            this.startOffset = irClass.startOffset
            this.endOffset = irClass.endOffset
        }

        function.dispatchReceiverParameter = irClass.thisReceiver!!.copyTo(function)
        function.overriddenSymbols = listOf(type.getTypeIdFun)

        val builder = DeclarationIrBuilder(MetaData.context, function.symbol, function.startOffset, function.endOffset)

        val getIdCall = builder.irCall(idProperty.getter!!)
        getIdCall.dispatchReceiver = builder.irGet(companion.thisReceiver!!)
        getIdCall.type = MetaData.context.irBuiltIns.intType
        function.parent = irClass

        function.body = builder.irBlockBody {
            +irReturn(
                getIdCall
            )
        }

        functionTransformer.transformTo(function, irClass, type)
        irClass.transform(functionTransformer, null)
    }


}