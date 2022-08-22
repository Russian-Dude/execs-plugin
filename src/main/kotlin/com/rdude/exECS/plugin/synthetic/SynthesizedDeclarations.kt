package com.rdude.exECS.plugin.synthetic

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

object SynthesizedDeclarations {

    private val propertiesFqNames = mutableListOf<String>()

    private val methodFqNames = mutableListOf<String>()

    fun store(property: PropertyDescriptor) {
        propertiesFqNames.add(property.fqNameSafe.asString())
    }

    fun store(function: FunctionDescriptor) {
        methodFqNames.add(function.fqNameSafe.asString())
    }

    fun isSynthesized(property: IrProperty) = propertiesFqNames.contains(property.fqNameWhenAvailable?.asString())

    fun isSynthesized(function: IrFunction) = methodFqNames.contains(function.kotlinFqName.asString())

}