package com.rdude.exECS.plugin.ir.check

import com.rdude.exECS.plugin.describer.ClassDescriber
import org.jetbrains.kotlin.ir.declarations.IrClass

class MainCorrectnessChecker : CorrectnessChecker {

    override fun checkAndThrowIfNotCorrect(classes: Map<ClassDescriber, MutableList<IrClass>>) {
        UniqueAndRichComponentInterceptionChecker().checkAndThrowIfNotCorrect(classes)
        PoolableObjectChecker().checkAndThrowIfNotCorrect(classes)
    }

}