package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.SimpleRepresentation
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.name.FqName

object ExEcsExternal {

    private val fromPoolFunctions = listOf(
        // fromPool<T>()
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.pool.fromPool")).single {
            it.owner.valueParameters.isEmpty()
                    && it.owner.typeParameters.size == 1
        },
        // fromPool(T::class)
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.pool.fromPool")).single {
            it.owner.valueParameters.size == 1
                    && it.owner.typeParameters.size == 1
                    && it.owner.valueParameters[0].type.isKClass()
        },
        // fromPool<T> { ... }
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.pool.fromPool")).single {
            it.owner.valueParameters.size == 1
                    && it.owner.valueParameters[0].type.isFunction()
                    && it.owner.typeParameters.size == 1
        },
        // fromPool(T::class) { ... }
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.pool.fromPool")).single {
            it.owner.valueParameters.size == 2
                    && it.owner.typeParameters.size == 1
                    && it.owner.valueParameters[0].type.isKClass()
                    && it.owner.valueParameters[1].type.isFunction()
        }
    )
        .map { it.owner }


    val fromPoolFun = object : SimpleRepresentation<IrCall> {
        override fun invoke(irCall: IrCall): Boolean {
            return fromPoolFunctions.contains(irCall.symbol.owner)
        }
    }

}