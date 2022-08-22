package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.name.FqName

object World : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.world.World"

    val queuePoolableEventFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.queueEvent"))
            .filter {
                it.owner.typeParameters.size == 1
                        && it.owner.valueParameters.isEmpty()
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType.classOrNull!!)
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Event.irType.classOrNull!!)
            }.toMethodDescriber()
    }

    val queuePoolableEventWithApplyFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.queueEvent"))
            .filter {
                it.owner.typeParameters.size == 1
                        && it.owner.valueParameters.size == 1 && it.owner.valueParameters[0].type.isFunction()
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType.classOrNull!!)
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Event.irType.classOrNull!!)
            }.toMethodDescriber()
    }

    val queuePoolableEventWithPriorityFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.queueEvent"))
            .filter {
                it.owner.typeParameters.size == 1
                        && it.owner.valueParameters.size == 1 && it.owner.valueParameters[0].type == EventPriority.irType
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType.classOrNull!!)
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Event.irType.classOrNull!!)
            }.toMethodDescriber()
    }

    val queuePoolableEventWithPriorityAndApplyFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.queueEvent"))
            .filter {
                it.owner.typeParameters.size == 1
                        && it.owner.valueParameters.size == 2
                        && it.owner.valueParameters[0].type == EventPriority.irType
                        && it.owner.valueParameters[1].type.isFunction()
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType.classOrNull!!)
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Event.irType.classOrNull!!)
            }.toMethodDescriber()
    }

    val queueEventFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.queueEvent"))
            .single {
                it.owner.valueParameters.size == 2
                        && it.owner.valueParameters[0].type.isSubtypeOfClass(Event.irType.classOrNull!!)
                        && it.owner.valueParameters[1].type == EventPriority.irType
            }
            .toMethodDescriber()
    }

}