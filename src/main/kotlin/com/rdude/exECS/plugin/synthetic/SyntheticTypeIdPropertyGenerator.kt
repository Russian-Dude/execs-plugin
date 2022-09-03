package com.rdude.exECS.plugin.synthetic

import com.rdude.exECS.plugin.describer.*
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptorImpl
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.FieldDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyGetterDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.KotlinTypeFactory

class SyntheticTypeIdPropertyGenerator : SyntheticGenerator() {

    val generatedNames = mutableMapOf<Name, ClassDescriber>()

    override fun isCompanionNeeded(thisDescriptor: ClassDescriptor): Boolean =
        thisDescriptor.modality != Modality.ABSTRACT
                && thisDescriptor.kind != ClassKind.OBJECT
                && thisDescriptor.kind != ClassKind.ENUM_ENTRY
                && thisDescriptor.getAllSuperClassifiers().any {
            it.fqNameSafe.asString() == Component.fqNameString || it.fqNameSafe.asString() == Event.fqNameString
        }

    override fun getSyntheticPropertiesNames(thisDescriptor: ClassDescriptor): List<Name> {

        if (!thisDescriptor.isCompanionObject && thisDescriptor.kind != ClassKind.OBJECT) return emptyList()

        val result = mutableListOf<Name>()

        val cl =
            if (thisDescriptor.isCompanionObject) thisDescriptor.containingDeclaration as? ClassDescriptor ?: return emptyList()
            else if (thisDescriptor.kind == ClassKind.OBJECT) thisDescriptor
            else return emptyList()

        if (cl.getAllSuperClassifiers().any { it.fqNameSafe.asString() == Component.fqNameString }) {
            val generatedName = Name.identifier(Component.typeIdPropertyNameFor(cl.fqNameSafe))
            generatedNames[generatedName] = Component
            result.add(generatedName)
        }

        if (cl.getAllSuperClassifiers().any { it.fqNameSafe.asString() == Event.fqNameString }) {
            val generatedName = Name.identifier(Event.typeIdPropertyNameFor(cl.fqNameSafe))
            generatedNames[generatedName] = Event
            result.add(generatedName)
        }

        return result
    }

    override fun generateSyntheticProperties(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: ArrayList<PropertyDescriptor>,
        result: MutableSet<PropertyDescriptor>
    ) {

        val describer = generatedNames[name] as? HasTypeId ?: return

        val cl =
            if (thisDescriptor.isCompanionObject) thisDescriptor.containingDeclaration as ClassDescriptor
            else thisDescriptor

        val generatedTypeIdAnnotationClassDescriptor =
            thisDescriptor.module.findClassAcrossModuleDependencies(ExEcsAnnotations.GeneratedTypeIdProperty.classId)!!

        val generatedTypeIdAnnotationType = KotlinTypeFactory.simpleNotNullType(
            Annotations.EMPTY,
            generatedTypeIdAnnotationClassDescriptor,
            emptyList()
        )

        val describerCl = thisDescriptor.module.findClassAcrossModuleDependencies((describer as ClassDescriber).classId)!!

        val typeIdPropertyAnnotations = Annotations.create(
            listOf(
                AnnotationDescriptorImpl(
                    generatedTypeIdAnnotationType,
                    mutableMapOf(
                        Name.identifier("superType") to KClassValue.create(describerCl.defaultType),
                        Name.identifier("type") to KClassValue.create(cl.defaultType)
                    ),
                    thisDescriptor.source
                )
            )
        )

        val property = PropertyDescriptorImpl.create(
            thisDescriptor,
            typeIdPropertyAnnotations,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            false,
            Name.identifier(describer.typeIdPropertyNameFor(cl.fqNameSafe)),
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
            thisDescriptor.builtIns.intType,
            listOf(),
            thisDescriptor.thisAsReceiverParameter,
            null
        )

        val getter = PropertyGetterDescriptorImpl(
            property,
            Annotations.EMPTY,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            false,
            false,
            false,
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            null,
            thisDescriptor.source
        )

        val field = FieldDescriptorImpl(
            Annotations.EMPTY, property
        )

        getter.initialize(thisDescriptor.builtIns.intType)

        property.initialize(
            getter,
            null,
            field,
            null
        )

        SynthesizedDeclarations.store(property)

        result.add(property)
    }

}