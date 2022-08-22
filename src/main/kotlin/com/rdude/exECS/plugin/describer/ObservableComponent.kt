package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.name.FqName

object ObservableComponent : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.component.ObservableComponent"

    val worldProperty by lazy {
        MetaData.context.referenceProperties(FqName("$fqNameString.world"))
            .single()
    }
}