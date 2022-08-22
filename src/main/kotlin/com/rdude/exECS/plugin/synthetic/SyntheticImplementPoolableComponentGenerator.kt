package com.rdude.exECS.plugin.synthetic

import com.rdude.exECS.plugin.describer.Component
import com.rdude.exECS.plugin.describer.Poolable
import com.rdude.exECS.plugin.describer.PoolableComponent
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf

class SyntheticImplementPoolableComponentGenerator : SyntheticGenerator() {

    override fun isCompanionNeeded(thisDescriptor: ClassDescriptor): Boolean = false

    override fun addSyntheticSupertypes(thisDescriptor: ClassDescriptor, supertypes: MutableList<KotlinType>) {
        val poolableType = thisDescriptor.module.findClassAcrossModuleDependencies(
            ClassId(
                FqName(Poolable.packageName),
                Name.identifier(Poolable.className)
            )
        )!!.defaultType
        val componentType = thisDescriptor.module.findClassAcrossModuleDependencies(
            ClassId(
                FqName(Component.packageName),
                Name.identifier(Component.className)
            )
        )!!.defaultType
        val poolableComponentType = thisDescriptor.module.findClassAcrossModuleDependencies(
            ClassId(
                FqName(PoolableComponent.packageName),
                Name.identifier(PoolableComponent.className)
            )
        )!!.defaultType

        if (
            supertypes.any { it == poolableType || it.isSubtypeOf(poolableType) }
            && supertypes.any { it == componentType || it.isSubtypeOf(componentType) }
            && supertypes.none { it == poolableComponentType || it.isSubtypeOf(poolableComponentType) }
        ) {
            supertypes.add(poolableComponentType)
        }
    }
}