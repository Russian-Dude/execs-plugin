package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.debugMessage
import com.rdude.exECS.plugin.ir.lowering.ComponentMapperPropertyAdder
import com.rdude.exECS.plugin.ir.utils.reference.ComponentMapper
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.Representation
import com.rdude.exECS.plugin.ir.utils.reference.EntityWrapper
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.dump

class EntityWrapperToComponentMapperCallsTransformer : IrElementTransformerVoidWithContext() {

    private val componentMapperPropertyAdder = ComponentMapperPropertyAdder()

    private lateinit var currentTransformingCall: IrCall
    private lateinit var currentTransformingFunction: IrFunction
    private lateinit var currentTransformingClass: IrClass
    private lateinit var currentTransformingCallRepresentation: Representation<IrCall>

    fun transform(callData: CallsFinder.CallData) {
        this.currentTransformingClass = callData.insideClass
        this.currentTransformingCall = callData.call
        this.currentTransformingFunction = callData.insideFunction
        this.currentTransformingCallRepresentation = callData.representationOf

        currentTransformingFunction.transform(this, null)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression != currentTransformingCall) return expression

        return when (currentTransformingCallRepresentation) {
            EntityWrapper.getComponentFun -> transformGetComponent(expression)
            EntityWrapper.hasComponentFun -> transformHasComponent(expression)
            EntityWrapper.removeComponentFun -> transformRemoveComponent(expression)
            EntityWrapper.addComponentFun -> transformAddComponent(expression)
            else -> throw IllegalStateException("Unknown ir call representation $currentTransformingCallRepresentation")
        }
    }

    private fun transformGetComponent(expression: IrCall): IrExpression {
        val type = expression.getTypeArgument(0)!!
        val componentMapperProperty = componentMapperPropertyAdder.addIfAbsent(currentTransformingClass, type)

        val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
        getComponentMapperCall.dispatchReceiver = builder.irGet(currentTransformingFunction.dispatchReceiverParameter!!)

        val getEntityIdCall = builder.irCall(EntityWrapper.getEntityIdProperty.owner.getter!!)
        getEntityIdCall.dispatchReceiver = expression.dispatchReceiver!!

        val resultCall = builder.irCall(ComponentMapper.getComponentFun)
        resultCall.dispatchReceiver = getComponentMapperCall
        resultCall.putValueArgument(0, getEntityIdCall)
        resultCall.type = expression.type

        return resultCall
    }

    private fun transformHasComponent(expression: IrCall): IrExpression {
        val type =
            if (expression.typeArgumentsCount == 1) expression.getTypeArgument(0)!!
            else (expression.getValueArgument(0)!! as IrClassReference).classType
        val componentMapperProperty = componentMapperPropertyAdder.addIfAbsent(currentTransformingClass, type)

        val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
        getComponentMapperCall.dispatchReceiver = builder.irGet(currentTransformingFunction.dispatchReceiverParameter!!)

        val getEntityIdCall = builder.irCall(EntityWrapper.getEntityIdProperty.owner.getter!!)
        getEntityIdCall.dispatchReceiver = expression.dispatchReceiver!!

        val resultCall = builder.irCall(ComponentMapper.hasComponentFun)
        resultCall.dispatchReceiver = getComponentMapperCall
        resultCall.putValueArgument(0, getEntityIdCall)
        resultCall.type = expression.type

        return resultCall
    }

    private fun transformRemoveComponent(expression: IrCall): IrExpression {
        val type =
            if (expression.typeArgumentsCount == 1) expression.getTypeArgument(0)!!
            else (expression.getValueArgument(0)!! as IrClassReference).classType
        val componentMapperProperty = componentMapperPropertyAdder.addIfAbsent(currentTransformingClass, type)

        val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
        getComponentMapperCall.dispatchReceiver = builder.irGet(currentTransformingFunction.dispatchReceiverParameter!!)

        val getEntityIdCall = builder.irCall(EntityWrapper.getEntityIdProperty.owner.getter!!)
        getEntityIdCall.dispatchReceiver = expression.dispatchReceiver!!

        val resultCall = builder.irCall(ComponentMapper.removeComponentFun)
        resultCall.dispatchReceiver = getComponentMapperCall
        resultCall.putValueArgument(0, getEntityIdCall)
        resultCall.type = expression.type

        return resultCall
    }

    private fun transformAddComponent(expression: IrCall): IrExpression {
        val type = expression.getValueArgument(0)!!.type

        val componentMapperProperty = componentMapperPropertyAdder.addIfAbsent(currentTransformingClass, type)

        val builder = DeclarationIrBuilder(MetaData.context, componentMapperProperty.symbol)

        val getComponentMapperCall = builder.irCall(componentMapperProperty.getter!!)
        getComponentMapperCall.dispatchReceiver = builder.irGet(currentTransformingFunction.dispatchReceiverParameter!!)

        val getEntityIdCall = builder.irCall(EntityWrapper.getEntityIdProperty.owner.getter!!)
        getEntityIdCall.dispatchReceiver = expression.dispatchReceiver!!

        val resultCall = builder.irCall(ComponentMapper.addComponentFun)
        resultCall.dispatchReceiver = getComponentMapperCall
        resultCall.putValueArgument(0, getEntityIdCall)
        resultCall.putValueArgument(1, expression.getValueArgument(0))
        resultCall.type = expression.type

        return resultCall
    }
}