package com.rdude.exECS.plugin.ir.debug

import com.rdude.exECS.plugin.debugMessage
import com.rdude.exECS.plugin.describer.ExEcsAnnotations
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

class DebugVisitor : IrElementVisitorVoid {

    override fun visitElement(element: IrElement) {
        element.acceptChildren(this, null)
    }

    override fun visitClass(declaration: IrClass) {
        super.visitClass(declaration)
        if (declaration.hasAnnotation(ExEcsAnnotations.DebugIR.symbol)) {
            debugMessage("DUMP OF ${declaration.kotlinFqName.asString()}:\r\n${declaration.dump()}")
            debugMessage("KOTLIN LIKE DUMP OF ${declaration.kotlinFqName.asString()}: \r\n${declaration.dumpKotlinLike()}")
        }
    }

    override fun visitProperty(declaration: IrProperty) {
        super.visitProperty(declaration)
        if (declaration.hasAnnotation(ExEcsAnnotations.DebugIR.symbol)) {
            debugMessage("DUMP OF ${(declaration.fqNameWhenAvailable?.asString() ?: declaration.name.asString())}:\r\n${declaration.dump()}")
            debugMessage("KOTLIN LIKE DUMP OF ${(declaration.fqNameWhenAvailable?.asString() ?: declaration.name.asString())}: \r\n${declaration.dumpKotlinLike()}")
        }
    }

    override fun visitFunction(declaration: IrFunction) {
        super.visitFunction(declaration)
        if (declaration.hasAnnotation(ExEcsAnnotations.DebugIR.symbol)) {
            debugMessage("DUMP OF ${(declaration.fqNameWhenAvailable?.asString() ?: declaration.name.asString())}:\r\n${declaration.dump()}")
            debugMessage("KOTLIN LIKE DUMP OF ${(declaration.fqNameWhenAvailable?.asString() ?: declaration.name.asString())}: \r\n${declaration.dumpKotlinLike()}")
        }
    }
}