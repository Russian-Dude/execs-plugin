package com.rdude.exECS.plugin.ir.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrCall

object MetaData {
    lateinit var context: IrPluginContext
}

val INT_TYPE by lazy { MetaData.context.irBuiltIns.intType }
val IR_FACTORY by lazy { MetaData.context.irFactory }

interface Representation<T : IrElement>

interface SimpleRepresentation<T : IrElement> : Representation<T>, (T) -> Boolean

interface FakeOverrideFunctionRepresentation : Representation<IrCall>, (IrCall, IrClass) -> Boolean

infix fun <T : IrElement> T.represents(data: SimpleRepresentation<T>) = data.invoke(this)

fun IrCall.represents(representation: FakeOverrideFunctionRepresentation, inClass: IrClass) =
    representation.invoke(this, inClass)


infix fun <T : IrElement> T.notRepresents(data: SimpleRepresentation<T>) = !represents(data)

fun <K, V> MutableMap<K, MutableList<V>>.merge(key: K, value: V) =
    this.compute(key) { _, list -> list?.apply { add(value) } ?: mutableListOf(value) }

fun <K, V> MutableMap<K, MutableSet<V>>.merge(key: K, value: V) =
    this.compute(key) { _, set -> set?.apply { add(value) } ?: mutableSetOf(value) }

