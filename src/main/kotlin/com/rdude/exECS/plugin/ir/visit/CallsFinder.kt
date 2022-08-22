package com.rdude.exECS.plugin.ir.visit

import com.rdude.exECS.plugin.describer.MethodDescriber
import com.rdude.exECS.plugin.ir.utils.isCallTo
import com.rdude.exECS.plugin.ir.utils.merge
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class CallsFinder {

    private val classesVisitor = ClassesVisitor()
    private val functionsVisitor = FunctionsVisitor()
    private val callsVisitor = CallsVisitor()

    fun find(
        moduleFragment: IrModuleFragment,
        representations: Collection<MethodDescriber>,
        inClasses: Collection<IrClass> = mutableSetOf<IrClass>().apply { moduleFragment.accept(classesVisitor, this) }
    ): List<CallData> {
        callsVisitor.lookingForMethods = representations
        val result = mutableListOf<CallData>()
        for (cl in inClasses) {
            callsVisitor.currentClass = cl
            val functions = mutableListOf<IrFunction>()
            cl.accept(functionsVisitor, functions)
            for (function in functions) {
                val calls = mutableMapOf<MethodDescriber, MutableList<IrCall>>()
                function.accept(callsVisitor, calls)
                if (calls.isEmpty()) continue
                val classFunction: IrFunction = findClassFunction(function, cl) ?: continue
                for ((methodDescriber, set) in calls) {
                    set.forEach { call ->
                        result += CallData(call, cl, function, classFunction, methodDescriber)
                    }
                }
            }
        }
        return result.distinct()
    }

    private fun findClassFunction(current: IrElement?, cl: IrClass): IrFunction? =
        current?.let {
            if (current is IrFunction && current.dispatchReceiverParameter?.type?.getClass() == cl) current
            else if (current is IrDeclaration) findClassFunction(current.parent, cl)
            else null
        }


    data class CallData(
        val call: IrCall,
        val insideClass: IrClass,
        val insideFunction: IrFunction,
        val insideClassFunction: IrFunction,
        val methodDescriber: MethodDescriber
    )


    private inner class CallsVisitor : IrElementVisitor<Unit, MutableMap<MethodDescriber, MutableList<IrCall>>> {

        lateinit var lookingForMethods: Collection<MethodDescriber>
        lateinit var currentClass: IrClass

        override fun visitElement(element: IrElement, data: MutableMap<MethodDescriber, MutableList<IrCall>>) {
            element.acceptChildren(this, data)
        }

        override fun visitCall(expression: IrCall, data: MutableMap<MethodDescriber, MutableList<IrCall>>) {
            super.visitCall(expression, data)
            lookingForMethods.forEach {
                if (expression.isCallTo(it)) {
                    data.merge(it, expression)
                }
            }
        }
    }

    private inner class FunctionsVisitor : IrElementVisitor<Unit, MutableList<IrFunction>> {

        override fun visitElement(element: IrElement, data: MutableList<IrFunction>) {
            element.acceptChildren(this, data)
        }

        override fun visitFunction(declaration: IrFunction, data: MutableList<IrFunction>) {
            super.visitFunction(declaration, data)
            data += declaration
        }
    }

    private inner class ClassesVisitor : IrElementVisitor<Unit, MutableSet<IrClass>> {

        override fun visitElement(element: IrElement, data: MutableSet<IrClass>) {
            element.acceptChildren(this, data)
        }

        override fun visitClass(declaration: IrClass, data: MutableSet<IrClass>) {
            super.visitClass(declaration, data)
            data += declaration
        }
    }

}