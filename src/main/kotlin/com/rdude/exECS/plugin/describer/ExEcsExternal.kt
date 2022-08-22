package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.name.FqName

object ExEcsExternal {

    val fromPoolFun by lazy {
        listOf(
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
        ).toMethodDescriber()
    }

}