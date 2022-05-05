package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.ir.utils.reference.Component
import com.rdude.exECS.plugin.ir.utils.reference.Poolable
import com.rdude.exECS.plugin.ir.utils.reference.PoolableComponent
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.defaultType

class PoolableToPoolableComponentTransformer {

    fun transformIfNeeded(irClass: IrClass): Boolean {
        return if (irClass.defaultType.isSubtypeOfClass(Component.classSymbol)
            && irClass.defaultType.isSubtypeOfClass(Poolable.classSymbol)
            && !irClass.defaultType.isSubtypeOfClass(PoolableComponent.classSymbol)
        ) {
            (irClass.superTypes as MutableList).add(PoolableComponent.irType)
            true
        } else false
    }

}