package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.ir.transform.ComponentInsideEntitiesPropertyOverriderTransformer
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.reference.Component
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.declarations.IrClass

class GeneratedComponentInsideEntitiesPropertyAdder {

    private val propertyTransformer = ComponentInsideEntitiesPropertyOverriderTransformer()

    fun addTo(irClasses: Collection<IrClass>) {
        for (cl in irClasses) {
            val builder =
                DeclarationIrBuilder(MetaData.context, cl.symbol, cl.startOffset, cl.endOffset)
            val insideEntitiesProperty = createPropertyWithBackingField(
                inClass = cl,
                name = "insideEntities",
                type = MetaData.context.irBuiltIns.intType,
                isVar = true,
                isFinal = false,
                isLateInit = false,
                overridden = listOf(Component.insideEntitiesProperty),
                initializer = builder.irExprBody(builder.irInt(0))
            )
            propertyTransformer.transformTo(insideEntitiesProperty, cl)
        }
    }

}