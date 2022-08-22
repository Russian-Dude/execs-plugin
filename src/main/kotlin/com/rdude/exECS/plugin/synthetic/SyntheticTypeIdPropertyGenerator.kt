package com.rdude.exECS.plugin.synthetic

import com.rdude.exECS.plugin.describer.ClassDescriber
import com.rdude.exECS.plugin.describer.Component
import com.rdude.exECS.plugin.describer.Event
import com.rdude.exECS.plugin.describer.HasTypeId
import com.rdude.exECS.plugin.ir.utils.merge
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.FieldDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyGetterDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

class SyntheticTypeIdPropertyGenerator : SyntheticGenerator() {

    val generateIn = mutableMapOf<ClassDescriptor, MutableList<ClassDescriber>>()

    override fun isCompanionNeeded(thisDescriptor: ClassDescriptor): Boolean =
        thisDescriptor.modality != Modality.ABSTRACT
                && thisDescriptor.kind != ClassKind.OBJECT
                && thisDescriptor.getSuperInterfaces().any {
            it.fqNameSafe.asString() == Component.fqNameString || it.fqNameSafe.asString() == Event.fqNameString
        }

    override fun getSyntheticPropertiesNames(thisDescriptor: ClassDescriptor): List<Name> {

        if (!thisDescriptor.isCompanionObject && thisDescriptor.kind != ClassKind.OBJECT) return emptyList()

        val result = mutableListOf<Name>()

        val cl =
            if (thisDescriptor.isCompanionObject) thisDescriptor.containingDeclaration as? ClassDescriptor ?: return emptyList()
            else if (thisDescriptor.kind == ClassKind.OBJECT) thisDescriptor
            else return emptyList()

        if (cl.getSuperInterfaces().any { it.fqNameSafe.asString() == Component.fqNameString }) {
            generateIn.merge(thisDescriptor, Component)
            val typeName = cl.fqNameSafe.asString().replace(".", "_")
            result.add(Name.identifier("execs_generated_component_type_id_property_for_$typeName"))
        }

        if (cl.getSuperInterfaces().any { it.fqNameSafe.asString() == Event.fqNameString }) {
            generateIn.merge(thisDescriptor, Event)
            val typeName = cl.fqNameSafe.asString().replace(".", "_")
            result.add(Name.identifier("execs_generated_event_type_id_property_for_$typeName"))
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

        val describers = generateIn[thisDescriptor] ?: return

        describers.forEach { describer ->
            describer as HasTypeId
            val clName =
                if (thisDescriptor.isCompanionObject) thisDescriptor.containingDeclaration.fqNameSafe.asString()
                else thisDescriptor.fqNameSafe.asString()
            val typeName = clName.replace(".", "_")

            val property = PropertyDescriptorImpl.create(
                thisDescriptor,
                Annotations.EMPTY,
                Modality.FINAL,
                DescriptorVisibilities.PUBLIC,
                false,
                Name.identifier("execs_generated_${describer.name.toLowerCase()}_type_id_property_for_$typeName"),
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

}