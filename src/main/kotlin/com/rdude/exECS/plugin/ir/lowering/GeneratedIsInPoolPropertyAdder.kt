package com.rdude.exECS.plugin.ir.lowering

import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.Poolable
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.createAndAddPropertyWithBackingField
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.declarations.IrClass

class GeneratedIsInPoolPropertyAdder {

    fun addOrTransformIsInPoolPropertyIfNeeded(irClass: IrClass) {

        val builder = DeclarationIrBuilder(MetaData.context, irClass.symbol, irClass.startOffset, irClass.endOffset)

        irClass.createAndAddPropertyWithBackingField(
            name = Poolable.isInPoolProperty.owner.name.asString(),
            type = MetaData.context.irBuiltIns.booleanType,
            isVar = true,
            isFinal = false,
            isLateInit = false,
            overridden = listOf(Poolable.isInPoolProperty),
            initializer = builder.irExprBody(builder.irBoolean(false)),
            annotations = listOf(
                Kotlin.TransientAnnotation.constructorCall()
            )
        )
    }

}