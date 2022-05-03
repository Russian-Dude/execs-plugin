package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.Representation
import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.utils.createAndAddPropertyWithBackingField
import com.rdude.exECS.plugin.ir.utils.reference.ComponentMapper
import com.rdude.exECS.plugin.ir.utils.reference.EntityWrapper
import com.rdude.exECS.plugin.ir.utils.reference.Pool
import com.rdude.exECS.plugin.ir.utils.reference.SingletonEntity
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import com.rdude.exECS.plugin.ir.visit.PoolsMapper
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.name.FqName

class EntityCallsToComponentMapperCallsTransformer(val poolsMapper: PoolsMapper) :
    IrElementTransformerVoidWithContext() {

    private enum class EntityType { WRAPPER, SINGLETON }

    private val addedProperties: MutableMap<IrClass, MutableMap<IrType, IrProperty>> = HashMap()

    private lateinit var currentTransformingCall: IrCall
    private lateinit var currentTransformingFunction: IrFunction
    private lateinit var currentInsideClassTransformingFunction: IrFunction
    private lateinit var currentTransformingClass: IrClass
    private lateinit var currentTransformingCallRepresentation: Representation<IrCall>

    fun transform(callData: CallsFinder.CallData) {
        this.currentTransformingClass = callData.insideClass
        this.currentTransformingCall = callData.call
        this.currentTransformingFunction = callData.insideFunction
        this.currentInsideClassTransformingFunction = callData.insideClassFunction
        this.currentTransformingCallRepresentation = callData.representationOf

        currentTransformingFunction.transform(this, null)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val expression = super.visitCall(expression) as IrCall
        if (expression != currentTransformingCall) return expression

        return when (currentTransformingCallRepresentation) {
            EntityWrapper.getComponentFun -> transformGetComponent(expression, EntityType.WRAPPER)
            EntityWrapper.hasComponentFun -> transformHasComponent(expression, EntityType.WRAPPER)
            EntityWrapper.removeComponentFun -> transformRemoveComponent(expression, EntityType.WRAPPER)
            EntityWrapper.addComponentFun -> transformAddComponent(expression, EntityType.WRAPPER)
            EntityWrapper.addPoolableComponentFun -> transformAddPoolableComponent(expression, EntityType.WRAPPER)

            SingletonEntity.getComponentFun -> transformGetComponent(expression, EntityType.SINGLETON)
            SingletonEntity.hasComponentFun -> transformHasComponent(expression, EntityType.SINGLETON)
            SingletonEntity.removeComponentFun -> transformRemoveComponent(expression, EntityType.SINGLETON)
            SingletonEntity.addComponentFun -> transformAddComponent(expression, EntityType.SINGLETON)
            SingletonEntity.addPoolableComponentFun -> transformAddPoolableComponent(expression, EntityType.SINGLETON)
            else -> throw IllegalStateException("Unknown ir call representation $currentTransformingCallRepresentation")
        }
    }

    private fun transformGetComponent(expression: IrCall, entityType: EntityType): IrExpression {
        val type = expression.getTypeArgument(0)!!
        val componentMapperProperty = addPropertyIfNeeded(currentTransformingClass, type)

        val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)

        getComponentMapperCall.dispatchReceiver = builder
            .irGet(currentInsideClassTransformingFunction.dispatchReceiverParameter!!)

        val getEntityIdCall = when (entityType) {
            EntityType.WRAPPER -> builder.irCall(EntityWrapper.getEntityIdProperty.owner.getter!!)
            EntityType.SINGLETON -> builder.irCall(SingletonEntity.entityIdProperty.owner.getter!!)
        }

        getEntityIdCall.dispatchReceiver = when(entityType) {
            EntityType.WRAPPER -> expression.extensionReceiver!!
            EntityType.SINGLETON -> expression.dispatchReceiver!!
        }

        val resultCall = builder.irCall(ComponentMapper.getComponentFun)
        resultCall.dispatchReceiver = getComponentMapperCall
        resultCall.putValueArgument(0, getEntityIdCall)
        resultCall.type = expression.type

        return resultCall
    }

    private fun transformHasComponent(expression: IrCall, entityType: EntityType): IrExpression {
        val type =
            if (expression.typeArgumentsCount == 1) expression.getTypeArgument(0)!!
            else (expression.getValueArgument(0)!! as IrClassReference).classType
        val componentMapperProperty = addPropertyIfNeeded(currentTransformingClass, type)

        val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
        getComponentMapperCall.dispatchReceiver = builder
            .irGet(currentInsideClassTransformingFunction.dispatchReceiverParameter!!)

        val getEntityIdCall = when (entityType) {
            EntityType.WRAPPER -> builder.irCall(EntityWrapper.getEntityIdProperty.owner.getter!!)
            EntityType.SINGLETON -> builder.irCall(SingletonEntity.entityIdProperty.owner.getter!!)
        }

        getEntityIdCall.dispatchReceiver = when(entityType) {
            EntityType.WRAPPER -> expression.extensionReceiver!!
            EntityType.SINGLETON -> expression.dispatchReceiver!!
        }

        val resultCall = builder.irCall(ComponentMapper.hasComponentFun)
        resultCall.dispatchReceiver = getComponentMapperCall
        resultCall.putValueArgument(0, getEntityIdCall)
        resultCall.type = expression.type

        return resultCall
    }

    private fun transformRemoveComponent(expression: IrCall, entityType: EntityType): IrExpression {
        val type =
            if (expression.typeArgumentsCount == 1) expression.getTypeArgument(0)!!
            else (expression.getValueArgument(0)!! as IrClassReference).classType
        val componentMapperProperty = addPropertyIfNeeded(currentTransformingClass, type)

        val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
        getComponentMapperCall.dispatchReceiver = builder
            .irGet(currentInsideClassTransformingFunction.dispatchReceiverParameter!!)

        val getEntityIdCall = when (entityType) {
            EntityType.WRAPPER -> builder.irCall(EntityWrapper.getEntityIdProperty.owner.getter!!)
            EntityType.SINGLETON -> builder.irCall(SingletonEntity.entityIdProperty.owner.getter!!)
        }

        getEntityIdCall.dispatchReceiver = when(entityType) {
            EntityType.WRAPPER -> expression.extensionReceiver!!
            EntityType.SINGLETON -> expression.dispatchReceiver!!
        }

        val resultCall = builder.irCall(ComponentMapper.removeComponentFun)
        resultCall.dispatchReceiver = getComponentMapperCall
        resultCall.putValueArgument(0, getEntityIdCall)
        resultCall.type = expression.type

        return resultCall
    }

    private fun transformAddComponent(expression: IrCall, entityType: EntityType): IrExpression {
        val type = expression.getValueArgument(0)!!.type

        val componentMapperProperty = addPropertyIfNeeded(currentTransformingClass, type)

        val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
        getComponentMapperCall.dispatchReceiver = builder
            .irGet(currentInsideClassTransformingFunction.dispatchReceiverParameter!!)

        val getEntityIdCall = when (entityType) {
            EntityType.WRAPPER -> builder.irCall(EntityWrapper.getEntityIdProperty.owner.getter!!)
            EntityType.SINGLETON -> builder.irCall(SingletonEntity.entityIdProperty.owner.getter!!)
        }

        getEntityIdCall.dispatchReceiver = when(entityType) {
            EntityType.WRAPPER -> expression.extensionReceiver!!
            EntityType.SINGLETON -> expression.dispatchReceiver!!
        }

        val resultCall = builder.irCall(ComponentMapper.addComponentFun)
        resultCall.dispatchReceiver = getComponentMapperCall
        resultCall.putValueArgument(0, getEntityIdCall)
        resultCall.putValueArgument(1, expression.getValueArgument(0))
        resultCall.type = expression.type

        return resultCall
    }

    private fun transformAddPoolableComponent(expression: IrCall, entityType: EntityType): IrExpression {
        val applyFunction =
            if (expression.valueArgumentsCount == 1) expression.getValueArgument(0)!!
            else null

        val type = expression.getTypeArgument(0)!!

        val componentMapperProperty = addPropertyIfNeeded(currentTransformingClass, type)

        val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
        getComponentMapperCall.dispatchReceiver = builder
            .irGet(currentInsideClassTransformingFunction.dispatchReceiverParameter!!)

        val getEntityIdCall = when (entityType) {
            EntityType.WRAPPER -> builder.irCall(EntityWrapper.getEntityIdProperty.owner.getter!!)
            EntityType.SINGLETON -> builder.irCall(SingletonEntity.entityIdProperty.owner.getter!!)
        }

        getEntityIdCall.dispatchReceiver = when(entityType) {
            EntityType.WRAPPER -> expression.extensionReceiver!!
            EntityType.SINGLETON -> expression.dispatchReceiver!!
        }

        val poolPropertyInfo = poolsMapper[type] ?: return expression

        val poolBuilder = builderOf(poolPropertyInfo.property)

        val getPoolCall = builder.irCall(poolPropertyInfo.property.getter!!)
        getPoolCall.dispatchReceiver = poolBuilder.irGetObject(poolPropertyInfo.companion.symbol)
        getPoolCall.type = Pool.irTypeWith(type)

        val obtainComponentCall = poolBuilder.irCall(Pool.obtainFun)
        obtainComponentCall.dispatchReceiver = getPoolCall
        obtainComponentCall.type = type

        val resultCall =
            if (applyFunction != null) builder.irCall(ComponentMapper.addComponentWithApplyFun)
            else builder.irCall(ComponentMapper.addComponentFun)

        resultCall.dispatchReceiver = getComponentMapperCall
        resultCall.putValueArgument(0, getEntityIdCall)
        resultCall.putValueArgument(1, obtainComponentCall)
        if (applyFunction != null) {
            resultCall.putValueArgument(2, expression.getValueArgument(0))
        }

        resultCall.type = expression.type

        return resultCall
    }

    private fun addPropertyIfNeeded(irClass: IrClass, irType: IrType): IrProperty {
        val alreadyAdded = addedProperties[irClass]?.get(irType)
        if (alreadyAdded != null) return alreadyAdded

        val typeArgumentString = irType.classFqName!!.asString().replace(".", "_")
        val idPropertyName = "generated_component_mapper_for_$typeArgumentString"

        val thisPropertyType = MetaData.context.referenceClass(FqName("com.rdude.exECS.component.ComponentMapper"))!!
            .typeWith(irType)

        val resultProperty = irClass.createAndAddPropertyWithBackingField(
            name = idPropertyName,
            type = thisPropertyType,
            isVar = true,
            isLateInit = true
        )

        addedProperties.putIfAbsent(irClass, HashMap())
        addedProperties[irClass]!![irType] = resultProperty
        return resultProperty
    }
}