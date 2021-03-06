package com.rdude.exECS.plugin.ir.visit

import com.rdude.exECS.plugin.ir.utils.*
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
        representations: Collection<Representation<IrCall>>,
        inClasses: Collection<IrClass> = mutableSetOf<IrClass>().apply { moduleFragment.accept(classesVisitor, this) }
    ): List<CallData> {
        callsVisitor.lookingForRepresentations = representations
        val result = mutableListOf<CallData>()
        for (cl in inClasses) {
            callsVisitor.currentClass = cl
            val functions = mutableListOf<IrFunction>()
            cl.accept(functionsVisitor, functions)
            for (function in functions) {
                val calls = mutableMapOf<Representation<IrCall>, MutableList<IrCall>>()
                function.accept(callsVisitor, calls)
                if (calls.isEmpty()) continue
                val classFunction: IrFunction = findClassFunction(function, cl) ?: continue
                for ((representation, set) in calls) {
                    set.forEach { call ->
                        result += CallData(call, cl, function, classFunction, representation)
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
        val representationOf: Representation<IrCall>
    )


    private inner class CallsVisitor : IrElementVisitor<Unit, MutableMap<Representation<IrCall>, MutableList<IrCall>>> {

        lateinit var lookingForRepresentations: Collection<Representation<IrCall>>
        lateinit var currentClass: IrClass

        override fun visitElement(element: IrElement, data: MutableMap<Representation<IrCall>, MutableList<IrCall>>) {
            element.acceptChildren(this, data)
        }

        override fun visitCall(expression: IrCall, data: MutableMap<Representation<IrCall>, MutableList<IrCall>>) {
            super.visitCall(expression, data)
            lookingForRepresentations.forEach {
                if (it is SimpleRepresentation<IrCall>) {
                    if (expression represents it) {
                        data.merge(it, expression)
                    }
                } else if (it is FakeOverrideFunctionRepresentation
                    && expression.dispatchReceiver != null
                    && expression.dispatchReceiver!!.type.getClass() != null
                ) {
                    if (expression.represents(it, expression.dispatchReceiver!!.type.getClass()!!)) {
                        data.merge(it, expression)
                    }
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