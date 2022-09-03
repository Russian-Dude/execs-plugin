package com.rdude.exECS.plugin.ir.check

import com.rdude.exECS.plugin.describer.RichComponent
import com.rdude.exECS.plugin.describer.UniqueComponent
import com.rdude.exECS.plugin.exception.ExEcsCompilerException
import com.rdude.exECS.plugin.ir.transform.IrTransformerElement
import com.rdude.exECS.plugin.ir.utils.isSubclassOf
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.kotlinFqName

class UniqueAndRichComponentInterceptionChecker : IrTransformerElement() {

    override fun visitClass(cl: IrClass) {
        if (cl.isSubclassOf(UniqueComponent) && cl.isSubclassOf(RichComponent)) {
            throw ExEcsCompilerException(
                "Component should not implement both RichComponent and UniqueComponent. " +
                        "Problem in ${cl.kotlinFqName.asString()}"
            )
        }
    }
}