package com.rdude.exECS.plugin.ir.visit

import com.rdude.exECS.plugin.debugMessage
import com.rdude.exECS.plugin.describer.Describer
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrRawFunctionReference
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

/** Maps describers to declarations: ClassDescribers to IrClasses, MethodDescribers to IrCalls to these methods.*/
class DescribersMapper(moduleFragment: IrModuleFragment, lookingFor: List<Describer>) {

    private val map = mutableMapOf<Describer, List<IrDeclaration>>()

    private val stringBuilder = StringBuilder()

    init {
        moduleFragment.accept(Visitor(), null)
        debugMessage("Check declarations:${stringBuilder}")
    }

    private inner class Visitor : IrElementVisitorVoid {

        override fun visitElement(element: IrElement) {
            element.acceptChildren(this, null)
        }

        override fun visitFunction(declaration: IrFunction) {
            super.visitFunction(declaration)
        }

        override fun visitFunctionReference(expression: IrFunctionReference) {
            super.visitFunctionReference(expression)
        }

        override fun visitRawFunctionReference(expression: IrRawFunctionReference) {
            super.visitRawFunctionReference(expression)
        }

        override fun visitSimpleFunction(declaration: IrSimpleFunction) {
            super.visitSimpleFunction(declaration)
        }
    }


}