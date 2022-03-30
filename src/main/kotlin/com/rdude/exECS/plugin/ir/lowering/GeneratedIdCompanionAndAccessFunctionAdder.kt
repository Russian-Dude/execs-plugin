package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.ir.transform.GetIdFakeToCompanionBasedFunctionTransformer
import com.rdude.exECS.plugin.ir.utils.IR_FACTORY
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createCompanionObject
import com.rdude.exECS.plugin.ir.utils.createPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.reference.Event
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
import org.jetbrains.kotlin.name.Name

class GeneratedIdCompanionAndAccessFunctionAdder(private val existingCompanions: MutableMap<IrClass, IrClass>) {

    private val functionTransformer = GetIdFakeToCompanionBasedFunctionTransformer()

    fun addTo(irClasses: Collection<IrClass>) {
        for (cl in irClasses) {
            val companion = existingCompanions.getOrPut(cl) { cl.createCompanionObject() }
            val property = addIdPropertyTo(companion)
            addGetIdFunction(cl, companion, property)
        }
    }

    private fun addIdPropertyTo(to: IrClass) =
        to.createPropertyWithBackingField(
            name = "execs_generated_id_property",
            type = MetaData.context.irBuiltIns.intType,
            isVar = true,
            isLateInit = false
        )

    private fun addGetIdFunction(irClass: IrClass, companion: IrClass, idProperty: IrProperty) {
        val function = IR_FACTORY.buildFun {
            this.name = Name.identifier("getTypeId")
            this.visibility = DescriptorVisibilities.PUBLIC
            this.modality = Modality.FINAL
            this.returnType = MetaData.context.irBuiltIns.intType
            this.startOffset = irClass.startOffset
            this.endOffset = irClass.endOffset
        }

        function.dispatchReceiverParameter = irClass.thisReceiver!!.copyTo(function)
        function.overriddenSymbols = listOf(Event.getTypeIdFun)

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

        functionTransformer.transformTo(function, irClass)
        irClass.transform(functionTransformer, null)
    }


}