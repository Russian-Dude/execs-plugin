package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.*
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.utils.createAndAddPropertyWithBackingField
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import com.rdude.exECS.plugin.ir.visit.PoolsMapper
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.getClass

class WorldAccessorCallsTransformer(val poolsMapper: PoolsMapper) : IrElementTransformerVoidWithContext() {

    private val addedProperties: MutableMap<IrClass, MutableMap<IrType, IrProperty>> = HashMap()

    private lateinit var currentCallData: CallsFinder.CallData

    fun transform(callData: CallsFinder.CallData) {
        currentCallData = callData
        callData.insideFunction.transform(this, null)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val expression = super.visitCall(expression) as IrCall
        if (expression != currentCallData.call) return expression
        return when (currentCallData.methodDescriber) {
            WorldAccessor.Entity.getComponentFun -> transformEntityGetComponent(expression)
            WorldAccessor.Entity.hasComponentFun -> transformEntityHasComponent(expression)
            WorldAccessor.Entity.removeComponentFun -> transformEntityRemoveComponent(expression)
            WorldAccessor.Entity.addComponentFun -> transformEntityAddComponent(expression)
            WorldAccessor.Entity.addPoolableComponentFun -> transformEntityAddPoolableComponent(expression)
            WorldAccessor.getSingletonFun -> transformGetSingletonEntity(expression)
            WorldAccessor.getSystemFun -> transformGetSystem(expression)
            else -> throw NotImplementedError("Unknown ir call method describer ${currentCallData.methodDescriber}")
        }
    }


    private fun transformEntityGetComponent(expression: IrCall): IrExpression {
        val componentType = expression.getTypeArgument(0)!!

        if (componentType.getClass()?.modality == Modality.ABSTRACT) return expression

        val componentMapperProperty = addComponentMapperPropertyIfNeeded(currentCallData.insideClass, componentType)

        val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
            .apply {
                dispatchReceiver = builder.irGet(currentCallData.insideClassFunction.dispatchReceiverParameter!!)
            }

        val getEntityIdCall = builder.irCall(Entity.entityIdProperty.owner.getter!!)
            .apply {
                dispatchReceiver = expression.extensionReceiver!!
            }

        return builder.irCall(ComponentMapper.getComponentFun.single())
            .apply {
                dispatchReceiver = getComponentMapperCall
                type = expression.type
                putValueArgument(0, getEntityIdCall)
            }
    }


    private fun transformEntityHasComponent(expression: IrCall): IrExpression {
        val componentType =
            if (expression.typeArgumentsCount == 1) expression.getTypeArgument(0)!!
            else (expression.getValueArgument(0)!! as IrClassReference).classType

        if (componentType.getClass()?.modality == Modality.ABSTRACT) return expression

        val componentMapperProperty = addComponentMapperPropertyIfNeeded(currentCallData.insideClass, componentType)

        val builder = DeclarationIrBuilder(MetaData.context, currentCallData.insideClass.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
            .apply {
                dispatchReceiver = builder.irGet(currentCallData.insideClassFunction.dispatchReceiverParameter!!)
            }

        val getEntityIdCall = builder.irCall(Entity.entityIdProperty.owner.getter!!)
            .apply {
                dispatchReceiver = expression.extensionReceiver!!
            }

        return builder.irCall(ComponentMapper.hasComponentFun.single())
            .apply {
                dispatchReceiver = getComponentMapperCall
                putValueArgument(0, getEntityIdCall)
                type = expression.type
            }
    }


    private fun transformEntityRemoveComponent(expression: IrCall): IrExpression {
        val componentType =
            if (expression.typeArgumentsCount == 1) expression.getTypeArgument(0)!!
            else (expression.getValueArgument(0)!! as IrClassReference).classType

        if (componentType.getClass()?.modality == Modality.ABSTRACT) return expression

        val componentMapperProperty = addComponentMapperPropertyIfNeeded(currentCallData.insideClass, componentType)

        val builder = DeclarationIrBuilder(MetaData.context, currentCallData.insideClass.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
            .apply {
                dispatchReceiver = builder.irGet(currentCallData.insideClassFunction.dispatchReceiverParameter!!)
            }

        val getEntityIdCall = builder.irCall(Entity.entityIdProperty.owner.getter!!)
            .apply {
                dispatchReceiver = expression.extensionReceiver!!
            }

        return builder.irCall(ComponentMapper.removeComponentFun.single())
            .apply {
                dispatchReceiver = getComponentMapperCall
                putValueArgument(0, getEntityIdCall)
                type = expression.type
            }
    }


    private fun transformEntityAddComponent(expression: IrCall): IrExpression {
        val componentType = expression.getValueArgument(0)!!.type

        val componentMapperProperty = addComponentMapperPropertyIfNeeded(currentCallData.insideClass, componentType)

        val builder = builderOf(currentCallData.insideClass)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
            .apply {
                dispatchReceiver = builder.irGet(currentCallData.insideClassFunction.dispatchReceiverParameter!!)
            }

        val getEntityIdCall = builder.irCall(Entity.entityIdProperty.owner.getter!!)
            .apply {
                dispatchReceiver = expression.extensionReceiver!!
            }

        return builder.irCall(ComponentMapper.addComponentFun.single())
            .apply {
                dispatchReceiver = getComponentMapperCall
                putValueArgument(0, getEntityIdCall)
                putValueArgument(1, expression.getValueArgument(0))
                type = expression.type
            }
    }


    private fun transformEntityAddPoolableComponent(expression: IrCall): IrExpression {
        val applyFunction =
            if (expression.valueArgumentsCount == 1) expression.getValueArgument(0)!!
            else null

        val componentType = expression.getTypeArgument(0)!!

        if (componentType.getClass()?.modality == Modality.ABSTRACT) return expression

        val componentMapperProperty = addComponentMapperPropertyIfNeeded(currentCallData.insideClass, componentType)

        val builder = builderOf(currentCallData.insideClass)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
            .apply {
                dispatchReceiver = builder.irGet(currentCallData.insideClassFunction.dispatchReceiverParameter!!)
            }

        val getEntityIdCall = builder.irCall(Entity.entityIdProperty.owner.getter!!)
            .apply {
                dispatchReceiver = expression.extensionReceiver!!
            }

        val poolPropertyInfo = poolsMapper[componentType] ?: return expression

        val poolBuilder = builderOf(poolPropertyInfo.property)

        val getPoolCall = builder.irCall(poolPropertyInfo.property.getter!!)
            .apply {
                dispatchReceiver = poolBuilder.irGetObject(poolPropertyInfo.companion.symbol)
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

        return resultCall
    }

    private fun transformGetSingletonEntity(expression: IrCall): IrExpression {
        val singletonType = expression.getTypeArgument(0)!!

        if (singletonType.getClass()?.modality == Modality.ABSTRACT) return expression

        val cachedSingletonProperty = addSingletonEntityPropertyIfNeeded(currentCallData.insideClass, singletonType)

        val builder = builderOf(currentCallData.insideClass)

        return builder.irCall(cachedSingletonProperty.getter!!)
            .apply {
                type = expression.type
                dispatchReceiver = builder.irGet(currentCallData.insideClassFunction.dispatchReceiverParameter!!)
            }
    }

    private fun transformGetSystem(expression: IrCall): IrExpression {
        val systemType = expression.getTypeArgument(0)!!

        if (systemType.getClass()?.modality == Modality.ABSTRACT) return expression

        val cachedSystemProperty = addSystemPropertyIfNeeded(currentCallData.insideClass, systemType)

        val builder = builderOf(currentCallData.insideClass)

        return builder.irCall(cachedSystemProperty.getter!!)
            .apply {
                type = expression.type
                dispatchReceiver = builder.irGet(currentCallData.insideClassFunction.dispatchReceiverParameter!!)
            }
    }



    private fun addComponentMapperPropertyIfNeeded(irClass: IrClass, irType: IrType): IrProperty {
        val alreadyAdded = addedProperties[irClass]?.get(irType)
        if (alreadyAdded != null) return alreadyAdded

        val typeArgumentString = irType.classFqName!!.asString().replace(".", "_")

        val resultProperty = irClass.createAndAddPropertyWithBackingField(
            name = "generated_component_mapper_for_$typeArgumentString",
            type = ComponentMapper.irTypeWith(irType),
            isVar = true,
            isLateInit = false,
            annotations = listOf(
                Kotlin.JvmFieldAnnotation.constructorCall(),
                Kotlin.TransientAnnotation.constructorCall(),
                ExEcsAnnotations.GeneratedComponentMapperProperty.constructorCall(irType)
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
            name = "generated_singleton_entity_property_for_$typeArgumentString",
            type = irType,
            isVar = true,
            isLateInit = false,
            annotations = listOf(
                Kotlin.JvmFieldAnnotation.constructorCall(),
                Kotlin.TransientAnnotation.constructorCall(),
                ExEcsAnnotations.GeneratedSingletonEntityProperty.constructorCall(irType)
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
            name = "generated_system_property_for_$typeArgumentString",
            type = irType,
            isVar = true,
            isLateInit = false,
            annotations = listOf(
                Kotlin.JvmFieldAnnotation.constructorCall(),
                Kotlin.TransientAnnotation.constructorCall(),
                ExEcsAnnotations.GeneratedSystemProperty.constructorCall(irType)
            )
        )

        addedProperties.putIfAbsent(irClass, HashMap())
        addedProperties[irClass]!![irType] = resultProperty
        return resultProperty
    }
}