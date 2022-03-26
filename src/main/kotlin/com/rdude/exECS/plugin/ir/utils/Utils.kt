package com.rdude.exECS.plugin.ir.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement

object MetaData {
    lateinit var context: IrPluginContext
}

val INT_TYPE by lazy { MetaData.context.irBuiltIns.intType }
val IR_FACTORY by lazy { MetaData.context.irFactory }

interface Representation<T : IrElement> : (T) -> Boolean

infix fun <T : IrElement> T.represents(data: Representation<T>) = data.invoke(this)

infix fun <T : IrElement> T.notRepresents(data: Representation<T>) = !represents(data)

fun <K, V> MutableMap<K, MutableList<V>>.merge(key: K, value: V) =
    this.compute(key) { _, list -> list?.apply { add(value) } ?: mutableListOf(value) }

fun <K, V> MutableMap<K, MutableSet<V>>.merge(key: K, value: V) =
    this.compute(key) { _, set -> set?.apply { add(value) } ?: mutableSetOf(value) }

