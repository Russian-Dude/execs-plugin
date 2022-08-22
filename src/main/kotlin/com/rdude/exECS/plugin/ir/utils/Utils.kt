package com.rdude.exECS.plugin.ir.utils

import com.rdude.exECS.plugin.describer.MethodDescriber
import com.rdude.exECS.plugin.synthetic.SynthesizedDeclarations
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.overrides

object MetaData {
    lateinit var context: IrPluginContext
}

val INT_TYPE by lazy { MetaData.context.irBuiltIns.intType }
val IR_FACTORY by lazy { MetaData.context.irFactory }

fun IrCall.isCallTo(method: MethodDescriber): Boolean =
    method.symbols.any { symbol.owner == it.owner || symbol.owner.overrides(it.owner) }

fun IrSimpleFunctionSymbol.toMethodDescriber() = MethodDescriber(listOf(this))

fun List<IrSimpleFunctionSymbol>.toMethodDescriber() = MethodDescriber(this)

fun <K, V> MutableMap<K, MutableList<V>>.merge(key: K, value: V) =
    this.compute(key) { _, list -> list?.apply { add(value) } ?: mutableListOf(value) }

fun <K, V> MutableMap<K, MutableSet<V>>.merge(key: K, value: V) =
    this.compute(key) { _, set -> set?.apply { add(value) } ?: mutableSetOf(value) }

fun builderOf(irDeclaration: IrDeclaration) =
    DeclarationIrBuilder(MetaData.context, irDeclaration.symbol, irDeclaration.startOffset, irDeclaration.endOffset)

fun IrProperty.isSynthesized() = SynthesizedDeclarations.isSynthesized(this)

fun IrFunction.isSynthesized() = SynthesizedDeclarations.isSynthesized(this)



