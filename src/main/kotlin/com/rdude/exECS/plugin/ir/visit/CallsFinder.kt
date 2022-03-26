package com.rdude.exECS.plugin.ir.visit

import com.rdude.exECS.plugin.ir.utils.Representation
import com.rdude.exECS.plugin.ir.utils.merge
import com.rdude.exECS.plugin.ir.utils.represents
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class CallsFinder {

    private val classesVisitor = ClassesVisitor()
    private val functionsVisitor = FunctionsVisitor()
    private val callsVisitor = CallsVisitor()

    fun find(
        moduleFragment: IrModuleFragment,
        representations: Collection<Representation<IrCall>>,
        inClasses: Collection<IrClass> = mutableSetOf<IrClass>().apply { moduleFragment.accept(classesVisitor, this) }
    ): MutableSet<CallData> {
        callsVisitor.lookingForRepresentations = representations
        val result = mutableSetOf<CallData>()
        for (cl in inClasses) {
            val functions: MutableSet<IrFunction> = HashSet()
            cl.accept(functionsVisitor, functions)
            for (function in functions) {
                val calls = mutableMapOf<Representation<IrCall>, MutableSet<IrCall>>()
                function.accept(callsVisitor, calls)
                for ((representation, set) in calls) {
                    set.forEach { call ->
                        result += CallData(call, cl, function, representation)
                    }
                }
            }
        }
        return result
    }


    class CallData(val call: IrCall, val insideClass: IrClass, val insideFunction: IrFunction, val representationOf: Representation<IrCall>)


    private inner class CallsVisitor : IrElementVisitor<Unit, MutableMap<Representation<IrCall>, MutableSet<IrCall>>> {

        lateinit var lookingForRepresentations: Collection<Representation<IrCall>>

        override fun visitElement(element: IrElement, data: MutableMap<Representation<IrCall>, MutableSet<IrCall>>) {
            element.acceptChildren(this, data)
        }

        override fun visitCall(expression: IrCall, data: MutableMap<Representation<IrCall>, MutableSet<IrCall>>) {
            super.visitCall(expression, data)
            lookingForRepresentations.forEach {
                if (expression represents it) {
                    data.merge(it, expression)
                }
            }
        }
    }

    private inner class FunctionsVisitor : IrElementVisitor<Unit, MutableSet<IrFunction>> {

        override fun visitElement(element: IrElement, data: MutableSet<IrFunction>) {
            element.acceptChildren(this, data)
        }

        override fun visitFunction(declaration: IrFunction, data: MutableSet<IrFunction>) {
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