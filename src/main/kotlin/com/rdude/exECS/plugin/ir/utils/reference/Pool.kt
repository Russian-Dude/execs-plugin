package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

object Pool : Reference {

    val irType by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.pool.Pool"))!!.defaultType }

    val companion by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.pool.Pool"))!!.owner.companionObject()!! }

    val invokeFun by lazy {
        MetaData.context.referenceFunctions(FqName("${companion.kotlinFqName.asString()}.invoke"))
            .single { it.owner.valueParameters.size == 1
                    && it.owner.valueParameters[0].type.isFunction()
                    && it.owner.typeParameters.size == 1
                    && it.owner.isOperator }
    }

    val obtainFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.pool.Pool.obtain"))
            .single { it.owner.valueParameters.isEmpty()
                    && it.owner.typeParameters.isEmpty() }
    }

    fun irTypeWith(irType: IrType) =
        MetaData.context.referenceClass(FqName("com.rdude.exECS.pool.Pool"))!!.typeWith(irType)

}