package com.rdude.exECS.plugin.describer

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

interface HasTypeId {

    val irType: IrType

    val getTypeIdFun: MethodDescriber

    val name: String

    val getTypeIdMethodName get() = "get${name.capitalize()}TypeId"

    fun getTypeIdPropertyNamePrefix(): String

    fun typeIdPropertyNameFor(typeFqName: FqName): String =
        getTypeIdPropertyNamePrefix() + typeFqName.asString().replace(".", "_")

    fun typeIdPropertyNameFor(type: IrType): String = typeIdPropertyNameFor(type.classFqName!!)

    fun typeIdPropertyNameFor(cl: IrClass): String = typeIdPropertyNameFor(cl.kotlinFqName)

}