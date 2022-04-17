package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.FqName

object Poolable : Reference {

    val irType by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.pool.Poolable"))!!.defaultType }

}