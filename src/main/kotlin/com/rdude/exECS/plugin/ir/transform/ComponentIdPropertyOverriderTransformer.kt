package com.rdude.exECS.plugin.ir.transform

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty

class ComponentIdPropertyOverriderTransformer : IrElementTransformerVoidWithContext() {

    private lateinit var currentTransformTo: IrProperty

    fun transformTo(function: IrProperty, inClass: IrClass) {
        currentTransformTo = function
        inClass.transform(this, null)
    }

    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
        return if (declaration.origin == IrDeclarationOrigin.FAKE_OVERRIDE
            && declaration.name.asString() == "componentId"
        ) {
            return currentTransformTo
        } else {
            super.visitPropertyNew(declaration)
        }
    }

}