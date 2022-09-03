package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

abstract class ClassDescriber : Describer {

    abstract val fqNameString: String

    val packageName: String by lazy { fqNameString.substringBeforeLast(".", "") }

    val className: String by lazy { fqNameString.substringAfterLast(".") }

    val symbol by lazy { MetaData.context.referenceClass(FqName(fqNameString))!! }

    val irType by lazy { symbol.defaultType }

    val classId by lazy { ClassId(FqName(packageName), Name.identifier(className)) }

    fun irTypeWith(vararg args: IrType) = symbol.typeWith(*args)

    fun irTypeWith(args: List<IrType>) = symbol.typeWith(args)

    fun isSuperFor(irClass: IrClass): Boolean = irClass.defaultType.isSubtypeOfClass(symbol)

}