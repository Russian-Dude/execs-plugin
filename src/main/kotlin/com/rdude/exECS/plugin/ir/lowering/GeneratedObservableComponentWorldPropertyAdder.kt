package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.ObservableComponent
import com.rdude.exECS.plugin.describer.World
import com.rdude.exECS.plugin.ir.transform.PropertyTransformer
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createAndAddPropertyWithBackingField
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.makeNullable

class GeneratedObservableComponentWorldPropertyAdder {

    fun addOrTransformWorldPropertyIfNeeded(irClass: IrClass) {

        val builder = DeclarationIrBuilder(MetaData.context, irClass.symbol, irClass.startOffset, irClass.endOffset)

        irClass.createAndAddPropertyWithBackingField(
            name = ObservableComponent.worldProperty.owner.name.asString(),
            type = World.irType.makeNullable(),
            isVar = true,
            isFinal = false,
            isLateInit = false,
            overridden = listOf(ObservableComponent.worldProperty),
            initializer = builder.irExprBody(builder.irNull()),
            annotations = listOf(Kotlin.TransientAnnotation.constructorCall())
        )
    }

}