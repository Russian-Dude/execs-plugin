package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.FakeOverrideFunctionRepresentation
import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

object EntityWrapper : Reference {

    val irType by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.entity.EntityWrapper"))!!.defaultType }

    private fun getGetComponentFunctions(fakeOverrideInClass: IrClass): List<IrSimpleFunction> {
        val classFqName = fakeOverrideInClass.kotlinFqName.asString()
        return listOf(
            // wrapper.getComponent<T>()
            MetaData.context.referenceFunctions(FqName("$classFqName.getComponent"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                },
            // wrapper.getComponent(T::class)
            MetaData.context.referenceFunctions(FqName("$classFqName.getComponent"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.size == 1
                            && it.owner.valueParameters[0].type.isKClass()
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                },
            // wrapper[T::class]
            MetaData.context.referenceFunctions(FqName("$classFqName.get"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.size == 1
                            && it.owner.valueParameters[0].type.isKClass()
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                },
            // wrapper<T>()
            MetaData.context.referenceFunctions(FqName("$classFqName.invoke"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                }
        )
            .flatten()
            .map { it.owner }
    }

    private val fakeOverrideGetComponentFunctions = mutableMapOf<IrClass, List<IrSimpleFunction>>()

    val getComponentFun = object : FakeOverrideFunctionRepresentation {
        override fun invoke(irCall: IrCall, irClass: IrClass): Boolean {
            return fakeOverrideGetComponentFunctions.getOrPut(irClass) { getGetComponentFunctions(irClass) }
                .contains(irCall.symbol.owner)
        }
    }

    private fun getHasComponentFunctions(fakeOverrideInClass: IrClass): List<IrSimpleFunction> {
        val classFqName = fakeOverrideInClass.kotlinFqName.asString()
        return listOf(
            // wrapper.hasComponent<T>()
            MetaData.context.referenceFunctions(FqName("$classFqName.hasComponent"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                },
            // wrapper.hasComponent(T::class)
            MetaData.context.referenceFunctions(FqName("$classFqName.hasComponent"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.isEmpty()
                            && it.owner.valueParameters[0].type.isKClass()
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                }
        )
            .flatten()
            .map { it.owner }
    }

    private val fakeOverrideHasComponentFunctions = mutableMapOf<IrClass, List<IrSimpleFunction>>()

    val hasComponentFun = object : FakeOverrideFunctionRepresentation {
        override fun invoke(irCall: IrCall, irClass: IrClass): Boolean {
            return fakeOverrideHasComponentFunctions.getOrPut(irClass) { getHasComponentFunctions(irClass) }
                .contains(irCall.symbol.owner)
        }
    }

    private fun getRemoveComponentFunctions(fakeOverrideInClass: IrClass): List<IrSimpleFunction> {
        val classFqName = fakeOverrideInClass.kotlinFqName.asString()
        return listOf(
            // wrapper.removeComponent<T>()
            MetaData.context.referenceFunctions(FqName("$classFqName.removeComponent"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                },
            // wrapper.removeComponent(T::class)
            MetaData.context.referenceFunctions(FqName("$classFqName.removeComponent"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.isEmpty()
                            && it.owner.valueParameters[0].type.isKClass()
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                },
            // wrapper -= T::class
            MetaData.context.referenceFunctions(FqName("$classFqName.minusAssign"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.isEmpty()
                            && it.owner.valueParameters[0].type.isKClass()
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                }
        )
            .flatten()
            .map { it.owner }
    }

    private val fakeOverrideRemoveComponentFunctions = mutableMapOf<IrClass, List<IrSimpleFunction>>()

    val removeComponentFun = object : FakeOverrideFunctionRepresentation {
        override fun invoke(irCall: IrCall, irClass: IrClass): Boolean {
            return fakeOverrideRemoveComponentFunctions.getOrPut(irClass) { getRemoveComponentFunctions(irClass) }
                .contains(irCall.symbol.owner)
        }
    }

    private fun getAddComponentFunctions(fakeOverrideInClass: IrClass): List<IrSimpleFunction> {
        val classFqName = fakeOverrideInClass.kotlinFqName.asString()
        return listOf(
            // wrapper.addComponent(component)
            MetaData.context.referenceFunctions(FqName("$classFqName.addComponent"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.isEmpty()
                            && it.owner.valueParameters[0].type.isSubtypeOfClass(Component.irType!!.classOrNull!!)
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                },
            // wrapper += component
            MetaData.context.referenceFunctions(FqName("$classFqName.plusAssign"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.isEmpty()
                            && it.owner.valueParameters[0].type.isSubtypeOfClass(Component.irType!!.classOrNull!!)
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                }
        )
            .flatten()
            .map { it.owner }
    }

    private val fakeOverrideAddComponentFunctions = mutableMapOf<IrClass, List<IrSimpleFunction>>()

    val addComponentFun = object : FakeOverrideFunctionRepresentation {
        override fun invoke(irCall: IrCall, irClass: IrClass): Boolean {
            return fakeOverrideAddComponentFunctions.getOrPut(irClass) { getAddComponentFunctions(irClass) }
                .contains(irCall.symbol.owner)
        }
    }

    private fun getAddPoolableComponentFunctions(fakeOverrideInClass: IrClass): List<IrSimpleFunction> {
        val classFqName = fakeOverrideInClass.kotlinFqName.asString()
        return listOf(
            // wrapper.addComponent<T>()
            MetaData.context.referenceFunctions(FqName("$classFqName.addComponent"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                            //&& it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType.classOrNull!!)
                            //&& it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType.classOrNull!!)
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                },
            // wrapper.addComponent<T> { ... }
            MetaData.context.referenceFunctions(FqName("$classFqName.addComponent"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.valueParameters[0].type.isFunction()
                            && it.owner.typeParameters.size == 1
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.irType!!.classOrNull!!)
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType!!.classOrNull!!)
                            && it.owner.extensionReceiverParameter?.type == irType
                            && it.owner.dispatchReceiverParameter?.type == System.irType
                }
        )
            .flatten()
            .map { it.owner }
    }

    private val fakeOverrideAddPoolableComponentFunctions = mutableMapOf<IrClass, List<IrSimpleFunction>>()

    val addPoolableComponentFun = object : FakeOverrideFunctionRepresentation {
        override fun invoke(irCall: IrCall, irClass: IrClass): Boolean {
            return fakeOverrideAddPoolableComponentFunctions.getOrPut(irClass) { getAddPoolableComponentFunctions(irClass) }
                .contains(irCall.symbol.owner)
        }
    }

    val getEntityIdProperty by lazy {
        MetaData.context.referenceProperties(FqName("com.rdude.exECS.entity.EntityWrapper.entityID")).single()
    }
}