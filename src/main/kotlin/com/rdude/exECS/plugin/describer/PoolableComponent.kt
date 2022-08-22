package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.name.FqName

object PoolableComponent : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.component.PoolableComponent"

    val insideEntitiesProperty by lazy {
        MetaData.context.referenceProperties(FqName("$fqNameString.insideEntities"))
            .single()
    }

}