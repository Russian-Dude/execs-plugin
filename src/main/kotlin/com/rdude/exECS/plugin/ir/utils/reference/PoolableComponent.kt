package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.FqName

object PoolableComponent {

    val classSymbol by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.component.PoolableComponent"))!! }

    val irType by lazy { classSymbol.defaultType }

    val insideEntitiesProperty by lazy {
        MetaData.context.referenceProperties(FqName("com.rdude.exECS.component.PoolableComponent.insideEntities"))
            .single()
    }

}