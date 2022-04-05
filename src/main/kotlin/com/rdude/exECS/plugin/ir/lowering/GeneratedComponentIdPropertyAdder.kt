package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.ir.transform.ComponentIdPropertyOverriderTransformer
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createCompanionObject
import com.rdude.exECS.plugin.ir.utils.createAndAddPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.createPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.reference.Component
import com.rdude.exECS.plugin.ir.utils.reference.IdFactory
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty

class GeneratedComponentIdPropertyAdder(private val existingCompanions: MutableMap<IrClass, IrClass>) {

    private val propertyTransformer = ComponentIdPropertyOverriderTransformer()

    fun addTo(irClasses: Collection<IrClass>) {
        for (cl in irClasses) {
            val companion = existingCompanions.getOrPut(cl) { cl.createCompanionObject() }
            val idFactoryProperty = addIdFactoryPropertyTo(companion)
            propertyTransformer.transformTo(createIdProperty(idFactoryProperty, companion, cl), cl)
        }
    }


    private fun addIdFactoryPropertyTo(toCompanion: IrClass): IrProperty {
        val builder =
            DeclarationIrBuilder(MetaData.context, toCompanion.symbol, toCompanion.startOffset, toCompanion.endOffset)

        return toCompanion.createAndAddPropertyWithBackingField(
            name = "execs_generated_component_id_factory",
            type = IdFactory.irType,
            isVar = false,
            isFinal = false,
            initializer = builder.irExprBody(builder.irCallConstructor(IdFactory.constructor, emptyList()))
        )
    }


    private fun createIdProperty(idFactoryProperty: IrProperty, companion: IrClass, inClass: IrClass): IrProperty {
        val builder =
            DeclarationIrBuilder(MetaData.context, inClass.symbol, inClass.startOffset, inClass.endOffset)

        val factoryGetter = builder.irCall(idFactoryProperty.getter!!)

        val obtainCall = builder.irCall(IdFactory.obtainFun)
        obtainCall.dispatchReceiver = factoryGetter

        return createPropertyWithBackingField(
            inClass = inClass,
            name = "componentId",
            type = MetaData.context.irBuiltIns.intType,
            isVar = false,
            isFinal = false,
            isLateInit = false,
            overridden = listOf(Component.getIdProperty),
            initializer = builder.irExprBody(obtainCall)
        )
    }

}