package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.*
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import com.rdude.exECS.plugin.ir.visit.PoolsMapper
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.util.*

class SingletonEntityMethodsTransformer(private val existingTypeIdProperties: MutableMap<IrType, MutableMap<ClassDescriber, TypeIdProperty>>, val poolsMapper: PoolsMapper) :
    IrElementTransformerVoidWithContext() {

    private lateinit var currentCallData: CallsFinder.CallData

    fun transform(callData: CallsFinder.CallData) {
        currentCallData = callData
        callData.insideFunction.transform(this, null)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val superRes = super.visitCall(expression)
        if (currentCallData.call != expression) return superRes
        return when (currentCallData.methodDescriber) {
            SingletonEntity.getComponentFun -> transformCall(
                expression = expression,
                redirectTo = ExEcsGeneratedCallsObject.getComponentFromSingletonEntityByIdFun,
                typeArgRequired = true
            )
            SingletonEntity.hasComponentFun -> transformCall(
                expression = expression,
                redirectTo = ExEcsGeneratedCallsObject.hasComponentFromSingletonEntityByIdFun,
                typeArgRequired = false
            )
            SingletonEntity.removeComponentFun -> transformCall(
                expression = expression,
                redirectTo = ExEcsGeneratedCallsObject.removeComponentFromSingletonEntityByComponentTypeIdFun,
                typeArgRequired = false
            )
            SingletonEntity.addPoolableComponentFun -> transformAddPoolableComponent(expression)
            else -> throw NotImplementedError("Not implemented in ${this::class.simpleName}")
        }
    }


    private fun transformCall(
        expression: IrCall,
        redirectTo: MethodDescriber,
        typeArgRequired: Boolean
    ): IrCall {

        val componentType = expression.getTypeArgument(0)!!
        val componentClass = componentType.getClass()!!
        if (componentClass.isInterface || componentClass.modality == Modality.ABSTRACT) return expression
        val builder = builderOf(currentCallData.insideClass)

        return builder.irCall(redirectTo.single())
            .apply {
                type = expression.type
                dispatchReceiver = builder.irGetObject(ExEcsGeneratedCallsObject.symbol)

                if (typeArgRequired) {
                    putTypeArgument(0, expression.type)
                }

                putValueArgument(0, expression.dispatchReceiver)

                val cl =
                    if (componentClass.isObject) componentClass
                    else componentClass.companionObject()!!

                val typeIdProperty = cl.properties
                    .find { it.name.asString() == Component.typeIdPropertyNameFor(componentType) }!!

                val getPropertyCall = builder.irCall(typeIdProperty.getter!!, IrStatementOrigin.GET_PROPERTY)
                    .apply {
                        type = MetaData.context.irBuiltIns.intType
                        dispatchReceiver = builder.irGetObject(cl.symbol)
                    }

                putValueArgument(1, getPropertyCall)
            }
    }


    private fun transformAddPoolableComponent(expression: IrCall): IrCall {

        val componentType = expression.getTypeArgument(0)!!

        if (componentType.getClass()?.modality == Modality.ABSTRACT) return expression

        val builder = builderOf(currentCallData.insideClass)

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

        val resultCall = builder.irCall(SingletonEntity.addComponentSimpleFun.single())
            .apply {
                dispatchReceiver = expression.dispatchReceiver
                type = expression.type
            }

        // singleton.addComponent<T>()
        if (expression.valueArgumentsCount == 0) {
            resultCall.putValueArgument(0, obtainComponentCall)
        }
        // singleton.addComponent(T::class)
        else if (expression.valueArgumentsCount == 1 && expression.getValueArgument(0)!!.type.isKClass()) {
            resultCall.putValueArgument(0, obtainComponentCall)
        }
        // singleton.addComponent<T> { ... }
        else if (expression.valueArgumentsCount == 1 && expression.getValueArgument(0)!!.type.isFunction()) {
            val applyFunction = expression.getValueArgument(0)
            val applyCall = builder.irCall(Kotlin.applyFun.single())
                .apply {
                    extensionReceiver = obtainComponentCall
                    putValueArgument(0, applyFunction)
                    putTypeArgument(0, componentType)
                }
            resultCall.putValueArgument(0, applyCall)
        }
        // singleton.addComponent(T::class) { ... }
        else if (expression.valueArgumentsCount == 2
            && expression.getValueArgument(0)!!.type.isKClass()
            && expression.getValueArgument(1)!!.type.isFunction()
        ) {
            val applyFunction = expression.getValueArgument(1)
            val applyCall = builder.irCall(Kotlin.applyFun.single())
                .apply {
                    extensionReceiver = obtainComponentCall
                    putValueArgument(0, applyFunction)
                    putTypeArgument(0, componentType)
                }
            resultCall.putValueArgument(0, applyCall)
        }
        else throw IllegalStateException("Can not transform singleton add poolable component call")

        return resultCall
    }
}