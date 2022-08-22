package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.name.FqName

object RichComponent : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.component.RichComponent"

    val entitiesIdsProperty by lazy {
        MetaData.context.referenceProperties(FqName("$fqNameString.insideEntitiesSet"))
            .single()
    }

}