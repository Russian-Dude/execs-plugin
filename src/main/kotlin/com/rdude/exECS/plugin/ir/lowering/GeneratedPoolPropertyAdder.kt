package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.Pool
import com.rdude.exECS.plugin.describer.Poolable
import com.rdude.exECS.plugin.ir.transform.PropertyTransformer
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createAndAddPropertyWithBackingField
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.declarations.IrClass

class GeneratedPoolPropertyAdder {

    fun addOrTransformPoolPropertyIfNeeded(irClass: IrClass) {

        val builder = DeclarationIrBuilder(MetaData.context, irClass.symbol, irClass.startOffset, irClass.endOffset)

        irClass.createAndAddPropertyWithBackingField(
            name = Poolable.poolProperty.owner.name.asString(),
            type = Pool.irType,
            isVar = true,
            isFinal = false,
            isLateInit = false,
            overridden = listOf(Poolable.poolProperty),
            initializer = builder.irExprBody(builder.irNull()),
            annotations = listOf(
                Kotlin.TransientAnnotation.constructorCall()
            )
        )
    }

}