package com.rdude.exECS.plugin.ir.visit

import com.rdude.exECS.plugin.ir.utils.merge
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.getAllSuperclasses
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class ClassesFinder(private val classesFqNames: Collection<String>) : IrElementVisitor<Unit, MutableMap<String, MutableList<IrClass>>> {

    override fun visitElement(element: IrElement, data: MutableMap<String, MutableList<IrClass>>) {
        element.acceptChildren(this, data)
    }

    override fun visitClass(declaration: IrClass, data: MutableMap<String, MutableList<IrClass>>) {
        super.visitClass(declaration, data)
        for (fqName in classesFqNames) {
            if (declaration.getAllSuperclasses().any { it.kotlinFqName.asString() == fqName }) {
                data.merge(fqName, declaration)
            }
        }
    }
}