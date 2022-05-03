package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.FqName

object Poolable : Reference {

    val classSymbol by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.pool.Poolable"))!! }

    val irType by lazy { classSymbol.defaultType }

}