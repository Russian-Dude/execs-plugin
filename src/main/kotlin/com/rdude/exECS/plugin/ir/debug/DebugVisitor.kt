package com.rdude.exECS.plugin.ir.debug

import com.rdude.exECS.plugin.debugMessage
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

class DebugVisitor : IrElementVisitorVoid {

    override fun visitElement(element: IrElement) {
        element.acceptChildren(this, null)
    }

    override fun visitClass(declaration: IrClass) {
        super.visitClass(declaration)
        if (declaration.kotlinFqName.asString() == "com.rdude.ecsbenchmarks.TestIrSystem") {
            debugMessage("DUMP OF TEST IR SYSTEM:\r\n${declaration.dump()}")
        }
    }
}