package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.name.FqName

object Entity : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.entity.Entity"

    val entityIdProperty by lazy {
        MetaData.context.referenceProperties(FqName("$fqNameString.id")).single()
    }
}