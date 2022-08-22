package com.rdude.exECS.plugin.synthetic

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.KotlinType

abstract class SyntheticGenerator {

    abstract fun isCompanionNeeded(thisDescriptor: ClassDescriptor): Boolean

    open fun getSyntheticPropertiesNames(thisDescriptor: ClassDescriptor): List<Name> = emptyList()

    open fun addSyntheticSupertypes(thisDescriptor: ClassDescriptor, supertypes: MutableList<KotlinType>) {  }

    open fun generateSyntheticProperties(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: ArrayList<PropertyDescriptor>,
        result: MutableSet<PropertyDescriptor>
    ) { }

    open fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>
    ) { }

    open fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> = emptyList()

}