package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.UniqueComponent
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createAndAddPropertyWithBackingField
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.declarations.IrClass

class GeneratedUniqueComponentEntityIdPropertyAdder {

    fun addOrTransformEntityIdPropertyIfNeeded(irClass: IrClass) {

        val builder = DeclarationIrBuilder(MetaData.context, irClass.symbol, irClass.startOffset, irClass.endOffset)

        irClass.createAndAddPropertyWithBackingField(
            name = UniqueComponent.entityIdProperty.owner.name.asString(),
            type = MetaData.context.irBuiltIns.intType,
            isVar = true,
            isFinal = false,
            isLateInit = false,
            overridden = listOf(UniqueComponent.entityIdProperty),
            initializer = builder.irExprBody(builder.irInt(-1)),
            annotations = listOf(Kotlin.TransientAnnotation.constructorCall())
        )
    }

}