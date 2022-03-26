package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.Representation
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.name.FqName

object EntityWrapper {

    val irType by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.entity.EntityWrapper"))?.defaultType }

    private val getComponentFunctions by lazy {
        listOf(
            // wrapper.getComponent<T>()
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.entity.EntityWrapper.getComponent"))
                .filter {
                    it.owner.valueParameters.isEmpty() && it.owner.typeParameters.size == 1
                },
            // wrapper.getComponent(T::class)
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.entity.EntityWrapper.getComponent"))
                .filter { it.owner.valueParameters.size == 1 && it.owner.typeParameters.size == 1 && it.owner.valueParameters[0].type.isKClass() },
            // wrapper[T::class]
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.entity.EntityWrapper.get"))
                .filter { it.owner.valueParameters.size == 1 && it.owner.typeParameters.size == 1 && it.owner.valueParameters[0].type.isKClass() }
        )
            .flatten()
            .map { it.owner }
    }

    val getComponentFun: Representation<IrCall> = object : Representation<IrCall> {
        override fun invoke(call: IrCall): Boolean {
            return call.dispatchReceiver?.type == irType && getComponentFunctions.contains(call.symbol.owner)
                    && (call.valueArgumentsCount == 0 || call.getValueArgument(0) is IrClassReference)
        }
    }

    private val hasComponentFunctions by lazy {
        listOf(
            // wrapper.hasComponent<T>()
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.entity.EntityWrapper.hasComponent"))
                .filter { it.owner.valueParameters.isEmpty() && it.owner.typeParameters.size == 1 },
            // wrapper.hasComponent(T::class)
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.entity.EntityWrapper.hasComponent"))
                .filter { it.owner.valueParameters.size == 1 && it.owner.typeParameters.isEmpty() && it.owner.valueParameters[0].type.isKClass() }
        )
            .flatten()
            .map { it.owner }
    }

    val hasComponentFun: Representation<IrCall> = object : Representation<IrCall> {
        override fun invoke(call: IrCall): Boolean {
            return call.dispatchReceiver?.type == irType && hasComponentFunctions.contains(call.symbol.owner)
                    && (call.valueArgumentsCount == 0 || call.getValueArgument(0) is IrClassReference)
        }
    }

    private val removeComponentFunctions by lazy {
        listOf(
            // wrapper.removeComponent<T>()
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.entity.EntityWrapper.removeComponent"))
                .filter { it.owner.valueParameters.isEmpty() && it.owner.typeParameters.size == 1 },
            // wrapper.removeComponent(T::class)
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.entity.EntityWrapper.removeComponent"))
                .filter { it.owner.valueParameters.size == 1 && it.owner.typeParameters.isEmpty() && it.owner.valueParameters[0].type.isKClass() },
            // wrapper -= T::class
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.entity.EntityWrapper.minusAssign"))
                .filter { it.owner.valueParameters.size == 1 && it.owner.typeParameters.isEmpty() && it.owner.valueParameters[0].type.isKClass() }
        )
            .flatten()
            .map { it.owner }
    }

    val removeComponentFun: Representation<IrCall> = object : Representation<IrCall> {
        override fun invoke(call: IrCall): Boolean {
            return call.dispatchReceiver?.type == irType && removeComponentFunctions.contains(call.symbol.owner)
                    && (call.valueArgumentsCount == 0 || call.getValueArgument(0) is IrClassReference)
        }
    }

    private val addComponentFunctions by lazy {
        listOf(
            // wrapper.addComponent(component)
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.entity.EntityWrapper.addComponent"))
                .filter { it.owner.valueParameters.size == 1 && it.owner.typeParameters.isEmpty() && it.owner.valueParameters[0].type.isSubtypeOfClass(Component.irType!!.classOrNull!!) },
            // wrapper += component
            MetaData.context.referenceFunctions(FqName("com.rdude.exECS.entity.EntityWrapper.plusAssign"))
                .filter { it.owner.valueParameters.size == 1 && it.owner.typeParameters.isEmpty() && it.owner.valueParameters[0].type.isSubtypeOfClass(Component.irType!!.classOrNull!!) }
        )
            .flatten()
            .map { it.owner }
    }

    val addComponentFun: Representation<IrCall> = object : Representation<IrCall> {
        override fun invoke(call: IrCall): Boolean {
            return call.dispatchReceiver?.type == irType && addComponentFunctions.contains(call.symbol.owner)
        }
    }

    val getEntityIdProperty by lazy {
        MetaData.context.referenceProperties(FqName("com.rdude.exECS.entity.EntityWrapper.entityID")).single()
    }
}