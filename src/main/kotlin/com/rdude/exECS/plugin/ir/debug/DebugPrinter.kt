package com.rdude.exECS.plugin.ir.debug

import com.rdude.exECS.plugin.debugMessage
import com.rdude.exECS.plugin.describer.ExEcsAnnotations
import com.rdude.exECS.plugin.ir.transform.IrTransformer
import com.rdude.exECS.plugin.ir.transform.IrTransformerElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.hasAnnotation

class DebugPrinter : IrTransformerElement() {

    override fun visitProperty(property: IrProperty) {
        if (property.hasAnnotation(ExEcsAnnotations.DebugIR.symbol)) {
            acceptLazy(afterStep = IrTransformer.Step.lastStep) {
                debugMessage("DUMP OF ${property.fqNameWhenAvailable?.asString()}:\r\n${property.dump()}")
                debugMessage("KOTLIN LIKE DUMP OF ${property.fqNameWhenAvailable?.asString()}: \r\n${property.dumpKotlinLike()}")
            }
        }
    }

    override fun visitClass(cl: IrClass) {
        if (cl.hasAnnotation(ExEcsAnnotations.DebugIR.symbol)) {
            acceptLazy(afterStep = IrTransformer.Step.lastStep) {
                debugMessage("DUMP OF ${cl.fqNameWhenAvailable?.asString()}:\r\n${cl.dump()}")
                debugMessage("KOTLIN LIKE DUMP OF ${cl.fqNameWhenAvailable?.asString()}: \r\n${cl.dumpKotlinLike()}")
            }
        }
    }

    override fun visitFunction(function: IrFunction) {
        if (function.hasAnnotation(ExEcsAnnotations.DebugIR.symbol)) {
            acceptLazy(afterStep = IrTransformer.Step.lastStep) {
                debugMessage("DUMP OF ${function.fqNameWhenAvailable?.asString()}:\r\n${function.dump()}")
                debugMessage("KOTLIN LIKE DUMP OF ${function.fqNameWhenAvailable?.asString()}: \r\n${function.dumpKotlinLike()}")
            }
        }
    }
}