package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.ir.utils.merge
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

class IrTransformer : IrElementTransformerVoidWithContext() {

    private var currentStep = Step.FIRST_TRANSFORM

    private val elements = mutableListOf<IrTransformerElement>()

    private lateinit var currentIrElement: IrElement

    private val lazyTransformations = mutableMapOf<IrElement, () -> IrElement>()

    private val lazyAccepts = mutableMapOf<Step, MutableList<() -> Unit>>()

    private var currentTransformation: IrElement? = null

    val current = Current()


    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
        currentIrElement = declaration
        return when (currentStep) {
            Step.FIRST_TRANSFORM -> {
                currentTransformation = null
                elements.forEach { it.visitProperty(declaration) }
                val transformed = currentTransformation as IrProperty?
                val res = super.visitPropertyNew(declaration)
                transformed ?: res
            }
            Step.LAZY_TRANSFORM -> {
                val res = super.visitPropertyNew(declaration)
                lazyTransformations[declaration]?.invoke() as? IrProperty ?: res
            }
        }
    }

    override fun visitClassNew(declaration: IrClass): IrStatement {
        currentIrElement = declaration
        return when (currentStep) {
            Step.FIRST_TRANSFORM -> {
                currentTransformation = null
                elements.forEach { it.visitClass(declaration) }
                val transformed = currentTransformation as IrClass?
                val res = super.visitClassNew(declaration)
                transformed ?: res
            }
            Step.LAZY_TRANSFORM -> {
                val res = super.visitClassNew(declaration)
                lazyTransformations[declaration]?.invoke() as? IrClass ?: res
            }
        }
    }

    override fun visitCall(expression: IrCall): IrExpression {
        currentIrElement = expression
        return when (currentStep) {
            Step.FIRST_TRANSFORM -> {
                currentTransformation = null
                elements.forEach { it.visitCall(expression) }
                val transformed = currentTransformation as IrCall?
                val res = super.visitCall(expression)
                transformed ?: res
            }
            Step.LAZY_TRANSFORM -> {
                val res = super.visitCall(expression)
                lazyTransformations[expression]?.invoke() as? IrCall ?: res
            }
        }
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        currentIrElement = declaration
        return when (currentStep) {
            Step.FIRST_TRANSFORM -> {
                currentTransformation = null
                elements.forEach { it.visitFunction(declaration) }
                val transformed = currentTransformation as IrFunction?
                val res = super.visitFunctionNew(declaration)
                transformed ?: res
            }
            Step.LAZY_TRANSFORM -> {
                val res = super.visitFunctionNew(declaration)
                lazyTransformations[declaration]?.invoke() as? IrFunction ?: res
            }
        }
    }

    override fun visitFieldNew(declaration: IrField): IrStatement {
        currentIrElement = declaration
        return when (currentStep) {
            Step.FIRST_TRANSFORM -> {
                currentTransformation = null
                elements.forEach { it.visitField(declaration) }
                val transformed = currentTransformation as IrField?
                val res = super.visitFieldNew(declaration)
                transformed ?: res
            }
            Step.LAZY_TRANSFORM -> {
                val res = super.visitFieldNew(declaration)
                lazyTransformations[declaration]?.invoke() as? IrField ?: res
            }
        }
    }

    fun transformCurrent(to: IrElement) {
        if (currentTransformation != null) throw IllegalStateException()
        currentTransformation = to
    }

    fun transformCurrentLazy(to: () -> IrElement) = transformLazy(currentIrElement, to)

    fun transformCurrentLazy(to: IrElement) = transformLazy(currentIrElement, to)

    fun transformLazy(from: IrElement, to: IrElement) = transformLazy(from) { to }

    fun transformLazy(from: IrElement, to: () -> IrElement) {
        lazyTransformations[from] = to
    }

    fun acceptLazy(afterStep: Step = Step.FIRST_TRANSFORM, accept: () -> Unit) {
        lazyAccepts.merge(afterStep, accept)
    }

    fun register(irTransformerElement: IrTransformerElement) {
        elements.add(irTransformerElement)
        irTransformerElement.mainTransformer = this
    }

    fun register(irTransformerElements: List<IrTransformerElement>) =
        irTransformerElements.forEach { register(it) }

    fun register(vararg irTransformerElements: IrTransformerElement) =
        irTransformerElements.forEach { register(it) }


    fun transform(moduleFragment: IrModuleFragment) {
        moduleFragment.transform(this, null)
        lazyAccepts[Step.FIRST_TRANSFORM]?.forEach { it.invoke() }
        if (lazyTransformations.isNotEmpty()) {
            currentStep = Step.LAZY_TRANSFORM
            moduleFragment.transform(this, null)
        }
        lazyAccepts[Step.LAZY_TRANSFORM]?.forEach { it.invoke() }
    }


    enum class Step {

        FIRST_TRANSFORM,
        LAZY_TRANSFORM;

        companion object {
            val firstStep get() = FIRST_TRANSFORM
            val lastStep get() = LAZY_TRANSFORM
        }
    }


    inner class Current {

        val currentFile get() = this@IrTransformer.currentFile
        val currentScript get() = this@IrTransformer.currentScript
        val currentClass get() = this@IrTransformer.currentClass?.irElement as IrClass?
        val currentFunction get() = this@IrTransformer.currentFunction?.irElement as IrFunction?
        val currentProperty get() = this@IrTransformer.currentProperty
        val currentAnonymousInitializer get() = this@IrTransformer.currentAnonymousInitializer
        val currentValueParameter get() = this@IrTransformer.currentValueParameter
        val currentScope get() = this@IrTransformer.currentScope
        val parentScope get() = this@IrTransformer.parentScope
        val allScopes get() = this@IrTransformer.allScopes
        val currentDeclarationParent get() = this@IrTransformer.currentDeclarationParent

    }
}