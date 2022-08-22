package com.rdude.exECS.plugin.ir.check

import com.rdude.exECS.plugin.describer.ClassDescriber
import com.rdude.exECS.plugin.describer.Poolable
import com.rdude.exECS.plugin.exception.ExEcsCompilerException
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.kotlinFqName

class PoolableObjectChecker : CorrectnessChecker {

    override fun checkAndThrowIfNotCorrect(classes: Map<ClassDescriber, MutableList<IrClass>>) {
        classes[Poolable]?.forEach {  poolableClass ->
            if (poolableClass.isObject) {
                throw ExEcsCompilerException("Object can not be Poolable. Problem in ${poolableClass.kotlinFqName.asString()}")
            }
        }
    }
}