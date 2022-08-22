package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.describer.EntitiesSet
import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.RichComponent
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createAndAddPropertyWithBackingField
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.declarations.IrClass

class GeneratedRichComponentEntitiesIdsPropertyAdder {

    fun addOrTransformEntityIdsPropertyIfNeeded(irClass: IrClass) {

        val builder = DeclarationIrBuilder(MetaData.context, irClass.symbol, irClass.startOffset, irClass.endOffset)

        irClass.createAndAddPropertyWithBackingField(
            name = RichComponent.entitiesIdsProperty.owner.name.asString(),
            type = EntitiesSet.irType,
            isVar = false,
            isFinal = false,
            isLateInit = false,
            overridden = listOf(RichComponent.entitiesIdsProperty),
            initializer = builder.irExprBody(builder.irCallConstructor(EntitiesSet.constructor, listOf())),
            annotations = listOf(Kotlin.TransientAnnotation.constructorCall())
        )
    }

}