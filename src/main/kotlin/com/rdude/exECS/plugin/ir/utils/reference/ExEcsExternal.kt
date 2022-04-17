package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.SimpleRepresentation
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.name.FqName

object ExEcsExternal {

    private val fromPoolFunctions = listOf(
            // fromPool<T>()
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.pool.fromPool"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                }
                .single(),
            // fromPool(T::class)
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.pool.fromPool"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.size == 1
                            && it.owner.valueParameters[0].type.isKClass()
                }
                .single()
        )
            .map { it.owner }


    val fromPoolFun = object : SimpleRepresentation<IrCall> {
        override fun invoke(irCall: IrCall): Boolean {
            return fromPoolFunctions.contains(irCall.symbol.owner)
        }
    }

}