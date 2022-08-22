package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.name.FqName

object Poolable : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.pool.Poolable"

    val poolProperty by lazy {
        MetaData.context.referenceProperties(FqName("$fqNameString.pool"))
            .single()
    }

    val isInPoolProperty by lazy {
        MetaData.context.referenceProperties(FqName("$fqNameString.isInPool"))
            .single()
    }

}