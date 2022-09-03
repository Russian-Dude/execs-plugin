package com.rdude.exECS.plugin.describer

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

object Poolable : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.pool.Poolable"

    val poolProperty by lazy { PropertyDescriber("$fqNameString.pool", this) }

    val isInPoolProperty by lazy { PropertyDescriber("$fqNameString.isInPool", this) }

    fun generatedDefaultPoolNameFor(irClass: IrClass) = generatedDefaultPoolNameFor(irClass.kotlinFqName)

    fun generatedDefaultPoolNameFor(irType: IrType) = generatedDefaultPoolNameFor(irType.classFqName!!)

    fun generatedDefaultPoolNameFor(fqName: FqName) = generatedDefaultPoolNameFor(fqName.asString())

    fun generatedDefaultPoolNameFor(fqName: String) =
        "execs_generated_pool_for_${fqName.replace(".", "_")}"

}