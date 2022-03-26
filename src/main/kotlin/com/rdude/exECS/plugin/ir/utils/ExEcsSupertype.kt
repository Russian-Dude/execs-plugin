package com.rdude.exECS.plugin.ir.utils

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

enum class ExEcsSupertype(val fqName: FqName) {

    SYSTEM (FqName("com.rdude.exECS.system.System")),
    COMPONENT (FqName("com.rdude.exECS.component.Component"));

}

fun IrClass.exEcsSupertype(): ExEcsSupertype? = ExEcsSupertype.values().singleOrNull { it.fqName == this.kotlinFqName }