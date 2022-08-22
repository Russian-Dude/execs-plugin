package com.rdude.exECS.plugin.ir.check

import com.rdude.exECS.plugin.describer.ClassDescriber
import org.jetbrains.kotlin.ir.declarations.IrClass

interface CorrectnessChecker {

    fun checkAndThrowIfNotCorrect(classes: Map<ClassDescriber, MutableList<IrClass>>)

}