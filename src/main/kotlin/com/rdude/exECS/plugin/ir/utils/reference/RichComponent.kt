package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.FqName

object RichComponent {

    val classSymbol by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.component.RichComponent"))!! }

    val irType by lazy { classSymbol.defaultType }

    val entityIdProperty by lazy {
        MetaData.context.referenceProperties(FqName("com.rdude.exECS.component.RichComponent.entityId"))
            .single()
    }

}