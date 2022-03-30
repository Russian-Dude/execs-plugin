package com.rdude.exECS.plugin.ir.visit

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class CompanionsFinder {

    private val classesVisitor = ClassesVisitor()

    fun find(inClasses: Collection<IrClass>): MutableMap<IrClass, IrClass> {
        val result = mutableMapOf<IrClass, IrClass>()
        inClasses
            .filter { !it.isCompanion }
            .forEach {
                classesVisitor.currentClass = it
                it.acceptChildren(classesVisitor, result)
            }
        return result
    }

    private class ClassesVisitor : IrElementVisitor<Unit, MutableMap<IrClass, IrClass>> {

        lateinit var currentClass: IrClass

        override fun visitElement(element: IrElement, data: MutableMap<IrClass, IrClass>) {
            element.acceptChildren(this, data)
        }

        override fun visitClass(declaration: IrClass, data: MutableMap<IrClass, IrClass>) {
            super.visitClass(declaration, data)
            if (declaration.isCompanion) {
                data[currentClass] = declaration
            }
        }
    }


}