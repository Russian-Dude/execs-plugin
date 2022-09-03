package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.PoolableComponent
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.isOverride
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.declarations.IrProperty

class FakeToRealOverridePoolableComponentInsideEntitiesPropertyTransformer : IrTransformerElement() {

    override fun visitProperty(property: IrProperty) {
        if (
            !property.isFakeOverride
            || currentClass?.modality == Modality.ABSTRACT
            || !property.isOverride(PoolableComponent.insideEntitiesProperty)
        ) return

        val inClass = currentClass ?: return

        val builder = DeclarationIrBuilder(MetaData.context, inClass.symbol, inClass.startOffset, inClass.endOffset)

        val transformedProperty = currentClass!!.createPropertyWithBackingField(
            name = PoolableComponent.insideEntitiesProperty.propertyName,
            type = PoolableComponent.insideEntitiesProperty.irType,
            isVar = PoolableComponent.insideEntitiesProperty.isVar,
            isFinal = false,
            isLateInit = false,
            visibility = DescriptorVisibilities.PUBLIC,
            annotations = listOf(Kotlin.TransientAnnotation.constructorCall()),
            overridden = listOf(PoolableComponent.insideEntitiesProperty.symbol),
            initializer = builder.irExprBody(builder.irInt(0))
        )

        transformCurrent(transformedProperty)
    }

}