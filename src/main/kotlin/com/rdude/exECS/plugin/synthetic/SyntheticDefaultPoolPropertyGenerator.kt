package com.rdude.exECS.plugin.synthetic

import com.rdude.exECS.plugin.describer.ExEcsAnnotations
import com.rdude.exECS.plugin.describer.Pool
import com.rdude.exECS.plugin.describer.Poolable
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptorImpl
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyGetterDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.typeUtil.isEnum

class SyntheticDefaultPoolPropertyGenerator : SyntheticGenerator() {

    private val generatedNames = mutableListOf<Name>()

    override fun isCompanionNeeded(thisDescriptor: ClassDescriptor): Boolean =
        thisDescriptor.modality != Modality.ABSTRACT
                && thisDescriptor.kind != ClassKind.OBJECT
                && thisDescriptor.kind != ClassKind.ENUM_ENTRY
                && thisDescriptor.getAllSuperClassifiers().any { it.fqNameSafe.asString() == Poolable.fqNameString }


    override fun getSyntheticPropertiesNames(thisDescriptor: ClassDescriptor): List<Name> {
        val containingDeclaration = thisDescriptor.containingDeclaration as? ClassDescriptor ?: return emptyList()
        return if (
            thisDescriptor.isCompanionObject
            && containingDeclaration.getAllSuperClassifiers()
                .any { it.fqNameSafe.asString() == Poolable.fqNameString }
            && !containingDeclaration.defaultType.isEnum()
            && !containingDeclaration.isInner
            && containingDeclaration.modality != Modality.ABSTRACT
            && containingDeclaration.constructors.any { it.valueParameters.isEmpty() || it.valueParameters.all { p -> p.hasDefaultValue() } }
        ) {
            val generatedName = Name.identifier(Poolable.generatedDefaultPoolNameFor(containingDeclaration.fqNameSafe))
            generatedNames.add(generatedName)
            listOf(generatedName)
        } else emptyList()
    }

    override fun generateSyntheticProperties(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: ArrayList<PropertyDescriptor>,
        result: MutableSet<PropertyDescriptor>
    ) {
        if (!generatedNames.contains(name)) return

        val containingDeclaration = thisDescriptor.containingDeclaration as ClassDescriptor

        val poolClassDescriptor = thisDescriptor.module.findClassAcrossModuleDependencies(Pool.classId)!!

        val propertyPoolType = KotlinTypeFactory.simpleNotNullType(
            Annotations.EMPTY,
            poolClassDescriptor,
            listOf(TypeProjectionImpl(containingDeclaration.defaultType))
        )

        val generatedDefaultPoolAnnotationClassDescriptor =
            thisDescriptor.module.findClassAcrossModuleDependencies(ExEcsAnnotations.GeneratedDefaultPoolProperty.classId)!!

        val generatedDefaultPoolAnnotationType = KotlinTypeFactory.simpleNotNullType(
            Annotations.EMPTY,
            generatedDefaultPoolAnnotationClassDescriptor,
            emptyList()
        )

        val defaultPoolPropertyAnnotations = Annotations.create(
            listOf(
                AnnotationDescriptorImpl(
                    generatedDefaultPoolAnnotationType,
                    mutableMapOf(Name.identifier("type") to KClassValue.create(containingDeclaration.defaultType)),
                    thisDescriptor.source
                )
            )
        )

        val property = PropertyDescriptorImpl.create(
            thisDescriptor,
            defaultPoolPropertyAnnotations,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            false,
            Name.identifier(Poolable.generatedDefaultPoolNameFor(containingDeclaration.fqNameSafe)),
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
            thisDescriptor.thisAsReceiverParameter,
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
            thisDescriptor.source
        )

        property.initialize(
            getter,
            null
        )

        getter.initialize(propertyPoolType)

        SynthesizedDeclarations.store(property)

        result.add(property)
    }
}