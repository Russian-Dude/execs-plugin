package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.Component
import com.rdude.exECS.plugin.describer.Poolable
import com.rdude.exECS.plugin.describer.PoolableComponent
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.defaultType

class PoolableToPoolableComponentTransformer : IrTransformerElement() {

    override fun visitClass(cl: IrClass) {
        if (cl.defaultType.isSubtypeOfClass(Component.symbol)
            && cl.defaultType.isSubtypeOfClass(Poolable.symbol)
            && !cl.defaultType.isSubtypeOfClass(PoolableComponent.symbol)
        ) {
            (cl.superTypes as MutableList).add(PoolableComponent.irType)
        }
    }

}