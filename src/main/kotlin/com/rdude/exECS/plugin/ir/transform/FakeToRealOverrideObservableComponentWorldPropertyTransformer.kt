package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.ObservableComponent
import com.rdude.exECS.plugin.describer.World
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.isOverride
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.types.makeNullable

class FakeToRealOverrideObservableComponentWorldPropertyTransformer : IrTransformerElement() {

    override fun visitProperty(property: IrProperty) {
        if (
            !property.isFakeOverride
            || currentClass?.modality == Modality.ABSTRACT
            || !property.isOverride(ObservableComponent.worldProperty)
        ) return

        val inClass = currentClass ?: return

        val builder = DeclarationIrBuilder(MetaData.context, inClass.symbol, inClass.startOffset, inClass.endOffset)

        val transformedProperty = currentClass!!.createPropertyWithBackingField(
            name = ObservableComponent.worldProperty.propertyName,
            type = World.irType.makeNullable(),
            isVar = ObservableComponent.worldProperty.isVar,
            isFinal = false,
            isLateInit = false,
            visibility = DescriptorVisibilities.PUBLIC,
            annotations = listOf(Kotlin.TransientAnnotation.constructorCall()),
            overridden = listOf(ObservableComponent.worldProperty.symbol),
            initializer = builder.irExprBody(builder.irNull())
        )

        transformCurrent(transformedProperty)
    }

}