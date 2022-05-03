package com.rdude.exECS.plugin.synthetic

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyGetterDescriptorImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.typeUtil.isEnum

// currently, generates pool synthetic properties for poolables
class ExEcsSyntheticResolveExtension : SyntheticResolveExtension {

    private val generateIn = mutableListOf<ClassDescriptor>()

    override fun getSyntheticCompanionObjectNameIfNeeded(thisDescriptor: ClassDescriptor): Name? {
        val superResult = super.getSyntheticCompanionObjectNameIfNeeded(thisDescriptor)
        return if (superResult == null
            && thisDescriptor.getSuperInterfaces()
                .any { it.fqNameSafe.asString() == "com.rdude.exECS.pool.Poolable" }
        ) Name.identifier("Companion") else null
    }

    override fun getSyntheticPropertiesNames(thisDescriptor: ClassDescriptor): List<Name> {
        val containingDeclaration = thisDescriptor.containingDeclaration as? ClassDescriptor ?: return emptyList()
        return if (
            thisDescriptor.isCompanionObject && containingDeclaration.getSuperInterfaces()
                .any { it.fqNameSafe.asString() == "com.rdude.exECS.pool.Poolable" }
            && !containingDeclaration.defaultType.isEnum()
            && !containingDeclaration.isInner
            && containingDeclaration.modality != Modality.ABSTRACT
        ) {
            generateIn.add(thisDescriptor)
            val typeName = containingDeclaration.fqNameSafe.asString().replace(".", "_")
            listOf(Name.identifier("execs_generated_pool_for_$typeName"))
        } else emptyList()
    }

    override fun generateSyntheticProperties(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: ArrayList<PropertyDescriptor>,
        result: MutableSet<PropertyDescriptor>
    ) {
        if (!generateIn.contains(thisDescriptor)) return

        val containingDeclaration = thisDescriptor.containingDeclaration as ClassDescriptor
        val typeName = containingDeclaration.fqNameSafe.asString().replace(".", "_")

        val poolClass = thisDescriptor.module.findClassAcrossModuleDependencies(
            ClassId(
                FqName("com.rdude.exECS.pool"),
                Name.identifier("Pool")
            )
        )!!

        val propertyPoolType = KotlinTypeFactory.simpleNotNullType(
            Annotations.EMPTY,
            poolClass,
            listOf(TypeProjectionImpl(containingDeclaration.defaultType))
        )

        val property = PropertyDescriptorImpl.create(
            thisDescriptor,
            Annotations.EMPTY,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            false,
            Name.identifier("execs_generated_pool_for_$typeName"),
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            thisDescriptor.source,
            false,
            false,
            false,
            false,
            false,
            false
        )

        property.setType(
            propertyPoolType,
            listOf(),
            null,
            null
        )

        val getter = PropertyGetterDescriptorImpl(
            property,
            Annotations.EMPTY,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            true,
            false,
            false,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            null,
            //property.source
            thisDescriptor.source
        )

        property.initialize(
            getter,
            null
        )

        getter.initialize(propertyPoolType)

        result.add(property)
    }
}