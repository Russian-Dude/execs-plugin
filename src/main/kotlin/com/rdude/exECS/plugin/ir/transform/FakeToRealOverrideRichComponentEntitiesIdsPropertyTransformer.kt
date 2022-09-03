package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.EntitiesSet
import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.RichComponent
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.isOverride
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.declarations.IrProperty

class FakeToRealOverrideRichComponentEntitiesIdsPropertyTransformer : IrTransformerElement() {

    override fun visitProperty(property: IrProperty) {
        if (
            !property.isFakeOverride
            || currentClass?.modality == Modality.ABSTRACT
            || !property.isOverride(RichComponent.entitiesIdsProperty)
        ) return

        val inClass = currentClass ?: return

        val builder = DeclarationIrBuilder(MetaData.context, inClass.symbol, inClass.startOffset, inClass.endOffset)

        val transformedProperty = currentClass!!.createPropertyWithBackingField(
            name = RichComponent.entitiesIdsProperty.propertyName,
            type = RichComponent.entitiesIdsProperty.irType,
            isVar = RichComponent.entitiesIdsProperty.isVar,
            isFinal = false,
            isLateInit = false,
            visibility = DescriptorVisibilities.PUBLIC,
            annotations = listOf(Kotlin.TransientAnnotation.constructorCall()),
            overridden = listOf(RichComponent.entitiesIdsProperty.symbol),
            initializer = builder.irExprBody(builder.irCallConstructor(EntitiesSet.constructor, listOf()))
        )

        transformCurrent(transformedProperty)
    }

}