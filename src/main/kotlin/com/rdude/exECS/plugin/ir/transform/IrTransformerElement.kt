package com.rdude.exECS.plugin.ir.transform

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall

abstract class IrTransformerElement {

    lateinit var mainTransformer: IrTransformer

    open fun visitProperty(property: IrProperty) {}

    open fun visitField(field: IrField) {}

    open fun visitClass(cl: IrClass) {}

    open fun visitCall(call: IrCall) {}

    open fun visitFunction(function: IrFunction) {}


    fun transformCurrent(to: IrElement) {
        mainTransformer.transformCurrent(to)
    }

    fun transformCurrentLazy(to: IrElement) {
        mainTransformer.transformCurrentLazy(to)
    }

    fun transformCurrentLazy(to: () -> IrElement) {
        mainTransformer.transformCurrentLazy(to)
    }

    fun transformLazy(from: IrElement, to: IrElement) {
        mainTransformer.transformLazy(from, to)
    }

    fun transformLazy(from: IrElement, to: () -> IrElement) {
        mainTransformer.transformLazy(from, to)
    }

    fun acceptLazy(afterStep: IrTransformer.Step = IrTransformer.Step.FIRST_TRANSFORM, accept: () -> Unit) {
        mainTransformer.acceptLazy(afterStep, accept)
    }

    val currentFile get() = mainTransformer.current.currentFile
    val currentScript get() = mainTransformer.current.currentScript
    val currentClass get() = mainTransformer.current.currentClass
    val currentFunction get() = mainTransformer.current.currentFunction
    val currentProperty get() = mainTransformer.current.currentProperty
    val currentAnonymousInitializer get() = mainTransformer.current.currentAnonymousInitializer
    val currentValueParameter get() = mainTransformer.current.currentValueParameter
    val currentScope get() = mainTransformer.current.currentScope
    val parentScope get() = mainTransformer.current.parentScope
    val allScopes get() = mainTransformer.current.allScopes
    val currentDeclarationParent get() = mainTransformer.current.currentDeclarationParent

}