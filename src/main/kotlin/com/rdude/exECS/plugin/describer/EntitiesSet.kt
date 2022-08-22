package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.name.FqName

object EntitiesSet : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.utils.collections.EntitiesSet"

    val constructor by lazy {
        MetaData.context.referenceConstructors(FqName(fqNameString))
            .single { it.owner.isPrimary }
    }

}