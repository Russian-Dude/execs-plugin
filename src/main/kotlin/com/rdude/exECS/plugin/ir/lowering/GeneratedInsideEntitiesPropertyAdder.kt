package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.ir.transform.PropertyTransformer
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createAndAddPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.createPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.reference.PoolableComponent
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.properties

class GeneratedInsideEntitiesPropertyAdder(private val propertyTransformer: PropertyTransformer) {

    fun addInsideEntitiesPropertyIfNeeded(irClass: IrClass) {

        val existingProperty =
            irClass.properties.find { it.name.asString() == PoolableComponent.insideEntitiesProperty.owner.name.asString() }

        // if property does not exist
        if (existingProperty == null) {
            val builder = DeclarationIrBuilder(MetaData.context, irClass.symbol, irClass.startOffset, irClass.endOffset)

            irClass.createAndAddPropertyWithBackingField(
                name = PoolableComponent.insideEntitiesProperty.owner.name.asString(),
                type = MetaData.context.irBuiltIns.intType,
                isVar = true,
                isFinal = false,
                isLateInit = false,
                overridden = listOf(PoolableComponent.insideEntitiesProperty),
                initializer = builder.irExprBody(builder.irInt(0))
            )
        }

        // if property is not overridden by user
        else if (existingProperty.isFakeOverride) {
            val builder = DeclarationIrBuilder(MetaData.context, irClass.symbol, irClass.startOffset, irClass.endOffset)

            val property = createPropertyWithBackingField(
                inClass = irClass,
                name = PoolableComponent.insideEntitiesProperty.owner.name.asString(),
                type = MetaData.context.irBuiltIns.intType,
                isVar = true,
                isFinal = false,
                isLateInit = false,
                overridden = listOf(PoolableComponent.insideEntitiesProperty),
                initializer = builder.irExprBody(builder.irInt(0))
            )

            propertyTransformer.transformProperty(existingProperty, property, irClass)
        }
    }

}