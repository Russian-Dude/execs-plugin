package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.describer.HasTypeId
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class GetTypeIdFakeToCompanionBasedFunctionTransformer : IrElementTransformerVoidWithContext() {

    private lateinit var currentTransformTo: IrSimpleFunction
    private lateinit var currentType: HasTypeId

    fun transformTo(function: IrSimpleFunction, inClass: IrClass, type: HasTypeId) {
        currentTransformTo = function
        currentType = type
        inClass.transform(this, null)
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        return if (declaration.origin == IrDeclarationOrigin.FAKE_OVERRIDE
            && declaration.name.asString() == "get${currentType.name.capitalize()}TypeId"
            && declaration.returnType == MetaData.context.irBuiltIns.intType
            && declaration.dispatchReceiverParameter?.type == currentType.irType
        ) {
            return currentTransformTo
        } else {
            super.visitSimpleFunction(declaration)
        }
    }


}