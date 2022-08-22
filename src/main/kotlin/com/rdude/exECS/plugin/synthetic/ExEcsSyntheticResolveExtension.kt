package com.rdude.exECS.plugin.synthetic

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.types.KotlinType

class ExEcsSyntheticResolveExtension : SyntheticResolveExtension {

    private val syntheticGenerators = listOf(
        SyntheticDefaultPoolPropertyGenerator(),
        SyntheticImplementPoolableComponentGenerator(),
        SyntheticTypeIdPropertyGenerator()
    )


    override fun getSyntheticCompanionObjectNameIfNeeded(thisDescriptor: ClassDescriptor): Name? {
        val superResult = super.getSyntheticCompanionObjectNameIfNeeded(thisDescriptor)
        if (superResult == null && syntheticGenerators.any { it.isCompanionNeeded(thisDescriptor) }) {
            return Name.identifier("Companion")
        }
        else return null
    }

    override fun getSyntheticPropertiesNames(thisDescriptor: ClassDescriptor): List<Name> =
        syntheticGenerators.flatMap { it.getSyntheticPropertiesNames(thisDescriptor) }


    override fun addSyntheticSupertypes(thisDescriptor: ClassDescriptor, supertypes: MutableList<KotlinType>) =
        syntheticGenerators.forEach { it.addSyntheticSupertypes(thisDescriptor, supertypes) }


    override fun generateSyntheticProperties(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: ArrayList<PropertyDescriptor>,
        result: MutableSet<PropertyDescriptor>
    ) = syntheticGenerators.forEach { it.generateSyntheticProperties(thisDescriptor, name, bindingContext, fromSupertypes, result) }


    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        syntheticGenerators.forEach {
            it.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
        }
    }

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> =
        syntheticGenerators.flatMap { it.getSyntheticFunctionNames(thisDescriptor) }
}