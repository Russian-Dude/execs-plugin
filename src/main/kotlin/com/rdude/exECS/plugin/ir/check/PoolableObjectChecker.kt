package com.rdude.exECS.plugin.ir.check

import com.rdude.exECS.plugin.describer.Poolable
import com.rdude.exECS.plugin.exception.ExEcsCompilerException
import com.rdude.exECS.plugin.ir.transform.IrTransformerElement
import com.rdude.exECS.plugin.ir.utils.isSubclassOf
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.kotlinFqName

class PoolableObjectChecker : IrTransformerElement() {

    override fun visitClass(cl: IrClass) {
        if (cl.isObject && cl.isSubclassOf(Poolable)) {
            throw ExEcsCompilerException("Object can not be Poolable. Problem in ${cl.kotlinFqName.asString()}")
        }
    }
}