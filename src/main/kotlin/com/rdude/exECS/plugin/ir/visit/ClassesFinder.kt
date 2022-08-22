package com.rdude.exECS.plugin.ir.visit

import com.rdude.exECS.plugin.describer.ClassDescriber
import com.rdude.exECS.plugin.ir.utils.merge
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class ClassesFinder(private val classDescribers: Collection<ClassDescriber>) : IrElementVisitor<Unit, MutableMap<ClassDescriber, MutableList<IrClass>>> {

    override fun visitElement(element: IrElement, data: MutableMap<ClassDescriber, MutableList<IrClass>>) {
        element.acceptChildren(this, data)
    }

    override fun visitClass(declaration: IrClass, data: MutableMap<ClassDescriber, MutableList<IrClass>>) {
        super.visitClass(declaration, data)
        for (classDescriber in classDescribers) {
            if (declaration.defaultType.isSubtypeOfClass(classDescriber.symbol)) {
                data.merge(classDescriber, declaration)
            }
        }
    }
}