package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.*
import com.rdude.exECS.plugin.ir.utils.*
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.properties

class WorldAccessorCallsTransformer : IrTransformerElement() {

    private val addedProperties: MutableMap<IrClass, MutableMap<IrType, IrProperty>> = HashMap()

    override fun visitCall(call: IrCall) {
        if (call.isCallTo(WorldAccessor.Entity.getComponentFun)) transformEntityGetComponent(call)
        else if (call.isCallTo(WorldAccessor.Entity.hasComponentFun)) transformEntityHasComponent(call)
        else if (call.isCallTo(WorldAccessor.Entity.removeComponentFun)) transformEntityRemoveComponent(call)
        else if (call.isCallTo(WorldAccessor.Entity.addComponentFun)) transformEntityAddComponent(call)
        else if (call.isCallTo(WorldAccessor.Entity.addPoolableComponentFun)) transformEntityAddPoolableComponent(call)
        else if (call.isCallTo(WorldAccessor.getSingletonFun)) transformGetSingletonEntity(call)
        else if (call.isCallTo(WorldAccessor.getSystemFun)) transformGetSystem(call)
    }


    private fun transformEntityGetComponent(expression: IrCall) {

        val currentWorldAccessorClass = getCurrentWorldAccessorClass(currentClass) ?: return
        val currentWorldAccessorDispatchReceiver =
            getCurrentWorldAccessorDispatchReceiverValue(currentWorldAccessorClass, currentFunction)

        acceptLazy {

            val componentType = expression.getTypeArgument(0)!!

            if (componentType.getClass()?.modality == Modality.ABSTRACT) return@acceptLazy

            val componentMapperProperty = addComponentMapperPropertyIfNeeded(currentWorldAccessorClass, componentType)

            val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

            val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
                .apply {
                    dispatchReceiver = builder.irGet(currentWorldAccessorDispatchReceiver)
                }

            val getEntityIdCall = builder.irCall(Entity.entityIdProperty.irProperty.getter!!)
                .apply {
                    dispatchReceiver = expression.extensionReceiver!!
                }

            val resultCall = builder.irCall(ComponentMapper.getComponentFun.single())
                .apply {
                    dispatchReceiver = getComponentMapperCall
                    type = expression.type
                    putValueArgument(0, getEntityIdCall)
                }

            transformLazy(expression, resultCall)
        }
    }


    private fun transformEntityHasComponent(expression: IrCall) {

        val currentWorldAccessorClass = getCurrentWorldAccessorClass(currentClass) ?: return
        val currentWorldAccessorDispatchReceiver =
            getCurrentWorldAccessorDispatchReceiverValue(currentWorldAccessorClass, currentFunction)

        acceptLazy {

            val componentType =
                if (expression.typeArgumentsCount == 1) expression.getTypeArgument(0)!!
                else (expression.getValueArgument(0)!! as IrClassReference).classType

            if (componentType.getClass()?.modality == Modality.ABSTRACT) return@acceptLazy


            val componentMapperProperty = addComponentMapperPropertyIfNeeded(currentWorldAccessorClass, componentType)

            val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

            val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
                .apply {
                    dispatchReceiver = builder.irGet(currentWorldAccessorDispatchReceiver)
                }

            val getEntityIdCall = builder.irCall(Entity.entityIdProperty.irProperty.getter!!)
                .apply {
                    dispatchReceiver = expression.extensionReceiver!!
                }

            val resultCall = builder.irCall(ComponentMapper.hasComponentFun.single())
                .apply {
                    dispatchReceiver = getComponentMapperCall
                    putValueArgument(0, getEntityIdCall)
                    type = expression.type
                }

            transformLazy(expression, resultCall)
        }
    }


    private fun transformEntityRemoveComponent(expression: IrCall) {

        val currentWorldAccessorClass = getCurrentWorldAccessorClass(currentClass) ?: return
        val currentWorldAccessorDispatchReceiver =
            getCurrentWorldAccessorDispatchReceiverValue(currentWorldAccessorClass, currentFunction)

        acceptLazy {

            val componentType =
                if (expression.typeArgumentsCount == 1) expression.getTypeArgument(0)!!
                else (expression.getValueArgument(0)!! as IrClassReference).classType

            if (componentType.getClass()?.modality == Modality.ABSTRACT) return@acceptLazy

            val componentMapperProperty = addComponentMapperPropertyIfNeeded(currentWorldAccessorClass, componentType)

            val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

            val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
                .apply {
                    dispatchReceiver = builder.irGet(currentWorldAccessorDispatchReceiver)
                }

            val getEntityIdCall = builder.irCall(Entity.entityIdProperty.irProperty.getter!!)
                .apply {
                    dispatchReceiver = expression.extensionReceiver!!
                }

            val resultCall = builder.irCall(ComponentMapper.removeComponentFun.single())
                .apply {
                    dispatchReceiver = getComponentMapperCall
                    putValueArgument(0, getEntityIdCall)
                    type = expression.type
                }

            transformLazy(expression, resultCall)
        }
    }


    private fun transformEntityAddComponent(expression: IrCall) {

        val currentWorldAccessorClass = getCurrentWorldAccessorClass(currentClass) ?: return
        val currentWorldAccessorDispatchReceiver =
            getCurrentWorldAccessorDispatchReceiverValue(currentWorldAccessorClass, currentFunction)

        acceptLazy {

            val componentType = expression.getValueArgument(0)!!.type

            val componentMapperProperty = addComponentMapperPropertyIfNeeded(currentWorldAccessorClass, componentType)

            val builder = builderOf(componentMapperProperty)

            val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
                .apply {
                    dispatchReceiver = builder.irGet(currentWorldAccessorDispatchReceiver)
                }

            val getEntityIdCall = builder.irCall(Entity.entityIdProperty.irProperty.getter!!)
                .apply {
                    dispatchReceiver = expression.extensionReceiver!!
                }

            val resultCall = builder.irCall(ComponentMapper.addComponentFun.single())
                .apply {
                    dispatchReceiver = getComponentMapperCall
                    putValueArgument(0, getEntityIdCall)
                    putValueArgument(1, expression.getValueArgument(0))
                    type = expression.type
                }

            transformLazy(expression, resultCall)
        }
    }


    private fun transformEntityAddPoolableComponent(expression: IrCall) {

        val currentWorldAccessorClass = getCurrentWorldAccessorClass(currentClass) ?: return
        val currentWorldAccessorDispatchReceiver =
            getCurrentWorldAccessorDispatchReceiverValue(currentWorldAccessorClass, currentFunction)

        acceptLazy {

            val applyFunction =
                if (expression.valueArgumentsCount == 1) expression.getValueArgument(0)!!
                else null

            val componentType = expression.getTypeArgument(0)!!
            val componentClass = componentType.getClass()!!
            val componentCompanionClass = componentClass.companionObject() ?: return@acceptLazy
            val poolProperty = componentCompanionClass.properties
                .find { it.hasAnnotation(ExEcsAnnotations.GeneratedDefaultPoolProperty.symbol) }
                ?: return@acceptLazy

            if (componentType.getClass()?.modality == Modality.ABSTRACT) return@acceptLazy

            val componentMapperProperty = addComponentMapperPropertyIfNeeded(currentWorldAccessorClass, componentType)

            val builder = builderOf(componentMapperProperty)

            val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
                .apply {
                    dispatchReceiver = builder.irGet(currentWorldAccessorDispatchReceiver)
                }

            val getEntityIdCall = builder.irCall(Entity.entityIdProperty.irProperty.getter!!)
                .apply {
                    dispatchReceiver = expression.extensionReceiver!!
                }

            val poolBuilder = builderOf(poolProperty)

            val getPoolCall = poolBuilder.irCall(poolProperty.getter!!)
                .apply {
                    dispatchReceiver = poolBuilder.irGetObject(componentCompanionClass.symbol)
                    type = Pool.irTypeWith(componentType)
                }

            val obtainComponentCall = poolBuilder.irCall(Pool.obtainFun.single())
                .apply {
                    dispatchReceiver = getPoolCall
                    type = componentType
                }

            val resultCall = builder.irCall(ComponentMapper.addComponentFun.single())
                .apply {
                    type = expression.type
                    dispatchReceiver = getComponentMapperCall
                    putValueArgument(0, getEntityIdCall)
                }

            if (applyFunction == null) {
                resultCall.putValueArgument(1, obtainComponentCall)
            }
            else {
                val applyCall = builder.irCall(Kotlin.applyFun.single())
                    .apply {
                        extensionReceiver = obtainComponentCall
                        putValueArgument(0, applyFunction)
                        putTypeArgument(0, componentType)
                    }
                resultCall.putValueArgument(1, applyCall)
            }

            transformLazy(expression, resultCall)
        }
    }

    private fun transformGetSingletonEntity(expression: IrCall) {

        val currentWorldAccessorClass = getCurrentWorldAccessorClass(currentClass) ?: return
        val currentWorldAccessorDispatchReceiver =
            getCurrentWorldAccessorDispatchReceiverValue(currentWorldAccessorClass, currentFunction)

        acceptLazy {

            val singletonType = expression.getTypeArgument(0)!!

            if (singletonType.getClass()?.modality == Modality.ABSTRACT) return@acceptLazy

            val cachedSingletonProperty = addSingletonEntityPropertyIfNeeded(currentWorldAccessorClass, singletonType)

            val builder = builderOf(currentWorldAccessorClass)

            val resultCall = builder.irCall(cachedSingletonProperty.getter!!)
                .apply {
                    type = expression.type
                    dispatchReceiver = builder.irGet(currentWorldAccessorDispatchReceiver)
                }

            transformLazy(expression, resultCall)
        }
    }

    private fun transformGetSystem(expression: IrCall) {

        val currentWorldAccessorClass = getCurrentWorldAccessorClass(currentClass) ?: return
        val currentWorldAccessorDispatchReceiver =
            getCurrentWorldAccessorDispatchReceiverValue(currentWorldAccessorClass, currentFunction)

        acceptLazy {

            val systemType = expression.getTypeArgument(0)!!

            if (systemType.getClass()?.modality == Modality.ABSTRACT) return@acceptLazy

            val cachedSystemProperty = addSystemPropertyIfNeeded(currentWorldAccessorClass, systemType)

            val builder = builderOf(currentWorldAccessorClass)

            val resultCall = builder.irCall(cachedSystemProperty.getter!!)
                .apply {
                    type = expression.type
                    dispatchReceiver = builder.irGet(currentWorldAccessorDispatchReceiver)
                }

            transformLazy(expression, resultCall)
        }
    }


    private fun addComponentMapperPropertyIfNeeded(irClass: IrClass, irType: IrType): IrProperty {
        val alreadyAdded = addedProperties[irClass]?.get(irType)
        if (alreadyAdded != null) return alreadyAdded

        val typeArgumentString = irType.classFqName!!.asString().replace(".", "_")

        val resultProperty = irClass.createAndAddPropertyWithBackingField(
            name = "cached_component_mapper_for_$typeArgumentString",
            type = ComponentMapper.irTypeWith(irType),
            isVar = true,
            isLateInit = false,
            annotations = listOf(
                Kotlin.JvmFieldAnnotation.constructorCall(),
                Kotlin.TransientAnnotation.constructorCall(),
                ExEcsAnnotations.CachedComponentMapperProperty.constructorCall(irType)
            )
        )

        addedProperties.putIfAbsent(irClass, HashMap())
        addedProperties[irClass]!![irType] = resultProperty
        return resultProperty
    }


    private fun addSingletonEntityPropertyIfNeeded(irClass: IrClass, irType: IrType): IrProperty {
        val alreadyAdded = addedProperties[irClass]?.get(irType)
        if (alreadyAdded != null) return alreadyAdded

        val typeArgumentString = irType.classFqName!!.asString().replace(".", "_")

        val resultProperty = irClass.createAndAddPropertyWithBackingField(
            name = "cached_singleton_$typeArgumentString",
            type = irType,
            isVar = true,
            isLateInit = false,
            annotations = listOf(
                Kotlin.JvmFieldAnnotation.constructorCall(),
                Kotlin.TransientAnnotation.constructorCall(),
                ExEcsAnnotations.CachedSingletonEntityProperty.constructorCall(irType)
            )
        )

        addedProperties.putIfAbsent(irClass, HashMap())
        addedProperties[irClass]!![irType] = resultProperty
        return resultProperty
    }


    private fun addSystemPropertyIfNeeded(irClass: IrClass, irType: IrType): IrProperty {
        val alreadyAdded = addedProperties[irClass]?.get(irType)
        if (alreadyAdded != null) return alreadyAdded

        val typeArgumentString = irType.classFqName!!.asString().replace(".", "_")

        val resultProperty = irClass.createAndAddPropertyWithBackingField(
            name = "cached_system_$typeArgumentString",
            type = irType,
            isVar = true,
            isLateInit = false,
            annotations = listOf(
                Kotlin.JvmFieldAnnotation.constructorCall(),
                Kotlin.TransientAnnotation.constructorCall(),
                ExEcsAnnotations.CachedSystemProperty.constructorCall(irType)
            )
        )

        addedProperties.putIfAbsent(irClass, HashMap())
        addedProperties[irClass]!![irType] = resultProperty
        return resultProperty
    }


    private fun getCurrentWorldAccessorClass(startLookFrom: IrClass?): IrClass? {
        if (startLookFrom == null) return null
        else if (startLookFrom.isSubclassOf(WorldAccessor)) return startLookFrom
        val parent = startLookFrom.parent as? IrClass ?: return null
        if (parent.isInner) return getCurrentWorldAccessorClass(parent)
        return null
    }


    private fun getCurrentWorldAccessorDispatchReceiverValue(
        fromClass: IrClass,
        currentFun: IrFunction?
    ): IrValueParameter {
        return if (currentFun != null) {
            var dispatchReceiver = currentFun.dispatchReceiverParameter
            var parent: IrDeclarationParent? = currentFun.parent
            while (dispatchReceiver?.type?.getClass() != fromClass && parent != null) {
                dispatchReceiver = when (parent) {
                    is IrFunction -> parent.dispatchReceiverParameter
                    is IrClass -> parent.thisReceiver
                    else -> null
                }
                parent = (parent as? IrDeclaration)?.parent
            }
            if (dispatchReceiver?.type?.isSubtypeOfClass(WorldAccessor.symbol) == true) dispatchReceiver
            else {
                throw IllegalStateException("exEcs plugin can not find dispatch receiver parameter for ${fromClass.kotlinFqName.asString()}")
            }
        } else fromClass.thisReceiver!!
    }

}