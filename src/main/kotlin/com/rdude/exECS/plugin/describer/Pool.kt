package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

object Pool : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.pool.Pool"

    val companion by lazy { MetaData.context.referenceClass(FqName(fqNameString))!!.owner.companionObject()!! }

    val invokeFun by lazy {
        MetaData.context.referenceFunctions(FqName("${companion.kotlinFqName.asString()}.invoke"))
            .single {
                it.owner.valueParameters.size == 1
                        && it.owner.valueParameters[0].type.isFunction()
                        && it.owner.typeParameters.size == 1
                        && it.owner.isOperator
            }
            .toMethodDescriber()
    }

    val obtainFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.obtain"))
            .single {
                it.owner.valueParameters.isEmpty()
                        && it.owner.typeParameters.isEmpty()
            }
            .toMethodDescriber()
    }

    fun irTypeWith(irType: IrType) =
        MetaData.context.referenceClass(FqName(fqNameString))!!.typeWith(irType)

}