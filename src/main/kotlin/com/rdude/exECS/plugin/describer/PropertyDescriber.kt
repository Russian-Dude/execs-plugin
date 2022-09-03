package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.overrides
import org.jetbrains.kotlin.name.FqName

open class PropertyDescriber(val fqNameString: String, val declaredInClass: ClassDescriber?) : Describer {

    val propertyName: String by lazy { fqNameString.substringAfterLast(".") }

    val symbol by lazy { MetaData.context.referenceProperties(FqName(fqNameString)).single() }

    val irProperty by lazy { symbol.owner }

    open val irType by lazy { irProperty.getter!!.returnType }

    val isVar by lazy { irProperty.isVar }

    fun isSuperFor(irProperty: IrProperty) = irProperty.overrides(this.irProperty)

}