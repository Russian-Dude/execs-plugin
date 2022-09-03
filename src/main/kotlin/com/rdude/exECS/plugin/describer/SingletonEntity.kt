package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.name.FqName

object SingletonEntity : ClassDescriber() {

    override val fqNameString: String = "com.rdude.exECS.entity.SingletonEntity"

    val getComponentFun by lazy {
        listOf(
            // getComponent<T>()
            MetaData.context.referenceFunctions(FqName("$fqNameString.getComponent"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                },
            // getComponent(T::class)
            MetaData.context.referenceFunctions(FqName("$fqNameString.getComponent"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.size == 1
                            && it.owner.valueParameters[0].type.isKClass()
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                },
            // [T::class]
            MetaData.context.referenceFunctions(FqName("$fqNameString.get"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.size == 1
                            && it.owner.valueParameters[0].type.isKClass()
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                },
            // <T>()
            MetaData.context.referenceFunctions(FqName("$fqNameString.invoke"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                }
        )
            .flatten().toMethodDescriber()
    }


    val hasComponentFun by lazy {
        listOf(
            // hasComponent<T>()
            MetaData.context.referenceFunctions(FqName("$fqNameString.hasComponent"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                },
            // hasComponent(T::class)
            MetaData.context.referenceFunctions(FqName("$fqNameString.hasComponent"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.size == 1
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.valueParameters[0].type.isKClass()
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                }
        )
            .flatten().toMethodDescriber()
    }


    val removeComponentFun by lazy {
        listOf(
            // removeComponent<T>()
            MetaData.context.referenceFunctions(FqName("$fqNameString.removeComponent"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                },
            // removeComponent(T::class)
            MetaData.context.referenceFunctions(FqName("$fqNameString.removeComponent"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.size == 1
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.valueParameters[0].type.isKClass()
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                },
            // singleton -= T::class
            MetaData.context.referenceFunctions(FqName("$fqNameString.minusAssign"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.size == 1
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.valueParameters[0].type.isKClass()
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                }
        )
            .flatten().toMethodDescriber()
    }


    val addComponentFun by lazy {
        listOf(
            // addComponent(component)
            MetaData.context.referenceFunctions(FqName("$fqNameString.addComponent"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.isEmpty()
                            && it.owner.valueParameters[0].type.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                },
            // singleton += component
            MetaData.context.referenceFunctions(FqName("$fqNameString.plusAssign"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.isEmpty()
                            && it.owner.valueParameters[0].type.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                }
        )
            .flatten().toMethodDescriber()
    }

    val addComponentSimpleFun by lazy {
        // addComponent(component)
        MetaData.context.referenceFunctions(FqName("$fqNameString.addComponent"))
            .filter {
                it.owner.valueParameters.size == 1
                        && it.owner.typeParameters.isEmpty()
                        && it.owner.valueParameters[0].type.isSubtypeOfClass(Component.irType.classOrNull!!)
                        && it.owner.dispatchReceiverParameter?.type == irType
                        && it.owner.extensionReceiverParameter == null
            }.toMethodDescriber()
    }


    val addPoolableComponentFun by lazy {
        listOf(
            // addComponent<T>()
            MetaData.context.referenceFunctions(FqName("$fqNameString.addComponent"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType.classOrNull!!)
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                },
            // addComponent<T> { ... }
            MetaData.context.referenceFunctions(FqName("$fqNameString.addComponent"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.valueParameters[0].type.isFunction()
                            && it.owner.typeParameters.size == 1
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType.classOrNull!!)
                            && it.owner.dispatchReceiverParameter?.type == irType
                            && it.owner.extensionReceiverParameter == null
                }
        )
            .flatten().toMethodDescriber()
    }


    val entityIdProperty by lazy { PropertyDescriber("$fqNameString.entityID", this) }
}