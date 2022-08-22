package com.rdude.exECS.plugin.ir.check

import com.rdude.exECS.plugin.describer.ClassDescriber
import com.rdude.exECS.plugin.describer.RichComponent
import com.rdude.exECS.plugin.describer.UniqueComponent
import com.rdude.exECS.plugin.exception.ExEcsCompilerException
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.kotlinFqName

class UniqueAndRichComponentInterceptionChecker : CorrectnessChecker {

    override fun checkAndThrowIfNotCorrect(classes: Map<ClassDescriber, MutableList<IrClass>>) {

        val uniqueComponentClasses = classes[UniqueComponent] ?: return
        val richComponentClasses = classes[RichComponent] ?: return

        uniqueComponentClasses.forEach { uniqueClass ->
            richComponentClasses.forEach { richClass ->
                if (uniqueClass == richClass) {
                    throw ExEcsCompilerException("Component should not implement both RichComponent and UniqueComponent. " +
                            "Problem in ${uniqueClass.kotlinFqName.asString()}")
                }
            }
        }
    }
}