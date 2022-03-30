package com.rdude.exECS.plugin.ir.utils.reference

import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType

interface HasId {

    val irType: IrType

    val getTypeIdFun: IrSimpleFunctionSymbol

    val idPropertyNamePrefix: String

}