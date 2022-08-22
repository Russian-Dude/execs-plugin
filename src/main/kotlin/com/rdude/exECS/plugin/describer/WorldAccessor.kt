package com.rdude.exECS.plugin.describer


import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.name.FqName

object WorldAccessor : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.world.WorldAccessor"

    object Entity {

        val getComponentFun by lazy {
            listOf(
                MetaData.context.referenceFunctions(FqName("$fqNameString.getComponent"))
                    .filter {
                        it.owner.extensionReceiverParameter?.type == com.rdude.exECS.plugin.describer.Entity.irType
                                && it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
                    },
                MetaData.context.referenceFunctions(FqName("$fqNameString.get"))
                    .filter {
                        it.owner.extensionReceiverParameter?.type == com.rdude.exECS.plugin.describer.Entity.irType
                                && it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
                    },
                MetaData.context.referenceFunctions(FqName("$fqNameString.invoke"))
                    .filter {
                        it.owner.extensionReceiverParameter?.type == com.rdude.exECS.plugin.describer.Entity.irType
                                && it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
                    }
            ).flatten().toMethodDescriber()
        }


        val hasComponentFun by lazy {
            listOf(
                MetaData.context.referenceFunctions(FqName("$fqNameString.hasComponent"))
                    .filter {
                        it.owner.extensionReceiverParameter?.type == com.rdude.exECS.plugin.describer.Entity.irType
                                && it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
                    },
                MetaData.context.referenceFunctions(FqName("$fqNameString.contains"))
                    .filter {
                        it.owner.extensionReceiverParameter?.type == com.rdude.exECS.plugin.describer.Entity.irType
                                && it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
                    }
            ).flatten().toMethodDescriber()
        }


        val removeComponentFun by lazy {
            listOf(
                MetaData.context.referenceFunctions(FqName("$fqNameString.removeComponent"))
                    .filter {
                        it.owner.extensionReceiverParameter?.type == com.rdude.exECS.plugin.describer.Entity.irType
                                && it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
                    },
                MetaData.context.referenceFunctions(FqName("$fqNameString.minusAssign"))
                    .filter {
                        it.owner.extensionReceiverParameter?.type == com.rdude.exECS.plugin.describer.Entity.irType
                                && it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
                    }
            ).flatten().toMethodDescriber()
        }


        val addComponentFun by lazy {
            listOf(
                MetaData.context.referenceFunctions(FqName("$fqNameString.addComponent"))
                    .filter {
                        it.owner.extensionReceiverParameter?.type == com.rdude.exECS.plugin.describer.Entity.irType
                                && it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
                                && it.owner.typeParameters.isEmpty()
                    },
                MetaData.context.referenceFunctions(FqName("$fqNameString.plusAssign"))
                    .filter {
                        it.owner.extensionReceiverParameter?.type == com.rdude.exECS.plugin.describer.Entity.irType
                                && it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
                                && it.owner.typeParameters.isEmpty()
                    }
            ).flatten().toMethodDescriber()
        }


        val addPoolableComponentFun by lazy {
            MetaData.context.referenceFunctions(FqName("$fqNameString.addComponent"))
                .filter {
                    it.owner.extensionReceiverParameter?.type == com.rdude.exECS.plugin.describer.Entity.irType
                            && it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
                            && it.owner.typeParameters.size == 1
                }.toMethodDescriber()
        }
    }

    val getSingletonFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.getSingletonEntity"))
            .filter {
                it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
            }.toMethodDescriber()
    }

    val getSystemFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.getSystem"))
            .filter {
                it.owner.dispatchReceiverParameter?.type == WorldAccessor.irType
            }.toMethodDescriber()
    }

    val queuePoolableEventFun by lazy {
        MethodDescriber("$fqNameString.queueEvent") {
            dispatchReceiverType = irType
            withTypeParam(Poolable.irType, Event.irType)
        }
    }

    val queuePoolableEventWithApplyFun by lazy {
        MethodDescriber("$fqNameString.queueEvent") {
            dispatchReceiverType = irType
            withTypeParam(Poolable.irType, Event.irType)
            withValueParam { it.isFunction() }
        }
    }

    val queuePoolableEventWithPriorityFun by lazy {
        MethodDescriber("$fqNameString.queueEvent") {
            dispatchReceiverType = irType
            withTypeParam(Poolable.irType, Event.irType)
            withValueParam(EventPriority.irType)
        }
    }

    val queuePoolableEventWithPriorityAndApplyFun by lazy {
        MethodDescriber("$fqNameString.queueEvent") {
            dispatchReceiverType = irType
            withTypeParam(Poolable.irType, Event.irType)
            withValueParam(EventPriority.irType)
            withValueParam { it.isFunction() }
        }
    }

    val queueEventFun by lazy {
        MethodDescriber("$fqNameString.queueEvent") {
            dispatchReceiverType = irType
            withValueParam(Event.irType)
            withValueParam(EventPriority.irType)
        }
    }

}