package com.rdude.exECS.plugin.ir.transform

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty

class PropertyTransformer : IrElementTransformerVoidWithContext() {

    private lateinit var currentTransformFrom: IrProperty
    private lateinit var currentTransformTo: IrProperty

    fun transformProperty(from: IrProperty, to: IrProperty, inClass: IrClass) {
        currentTransformFrom = from
        currentTransformTo = to
        inClass.transform(this, null)
    }

    override fun visitPropertyNew(declaration: IrProperty): IrStatement =
        if (currentTransformFrom == declaration) currentTransformTo
        else super.visitPropertyNew(declaration)

}