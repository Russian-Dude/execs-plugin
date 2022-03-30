package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.reference.Event
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class GetIdFakeToCompanionBasedFunctionTransformer : IrElementTransformerVoidWithContext() {

    private lateinit var currentTransformTo: IrSimpleFunction

    fun transformTo(function: IrSimpleFunction, inClass: IrClass) {
        currentTransformTo = function
        inClass.transform(this, null)
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        return if (declaration.origin == IrDeclarationOrigin.FAKE_OVERRIDE
            && declaration.name.asString() == "getTypeId"
            && declaration.returnType == MetaData.context.irBuiltIns.intType
            && declaration.dispatchReceiverParameter?.type == Event.irType
        ) {
            return currentTransformTo
        } else {
            super.visitSimpleFunction(declaration)
        }
    }


}