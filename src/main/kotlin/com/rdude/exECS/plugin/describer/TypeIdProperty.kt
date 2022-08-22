package com.rdude.exECS.plugin.describer

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty

class TypeIdProperty(val property: IrProperty, val companion: IrClass)