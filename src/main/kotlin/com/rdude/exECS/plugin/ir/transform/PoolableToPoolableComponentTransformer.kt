package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.Component
import com.rdude.exECS.plugin.describer.Poolable
import com.rdude.exECS.plugin.describer.PoolableComponent
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.defaultType

class PoolableToPoolableComponentTransformer {

    fun transformIfNeeded(irClass: IrClass): Boolean {
        return if (irClass.defaultType.isSubtypeOfClass(Component.symbol)
            && irClass.defaultType.isSubtypeOfClass(Poolable.symbol)
            && !irClass.defaultType.isSubtypeOfClass(PoolableComponent.symbol)
        ) {
            (irClass.superTypes as MutableList).add(PoolableComponent.irType)
            true
        } else false
    }

}