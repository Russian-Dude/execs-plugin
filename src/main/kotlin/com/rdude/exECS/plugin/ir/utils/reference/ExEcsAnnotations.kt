package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.name.FqName

object ExEcsAnnotations {

    val constructorForDefaultPool
            by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.pool.ConstructorForDefaultPool"))!! }

}