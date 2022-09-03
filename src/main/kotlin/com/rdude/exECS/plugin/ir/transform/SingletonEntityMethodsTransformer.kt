package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.*
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.utils.isCallTo
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.util.*

class SingletonEntityMethodsTransformer : IrTransformerElement() {

    override fun visitCall(call: IrCall) {
        if (call.isCallTo(SingletonEntity.getComponentFun)) {
            transformCall(
                expression = call,
                redirectTo = ExEcsGeneratedCallsObject.getComponentFromSingletonEntityByIdFun,
                typeArgRequired = true
            )
        }
        else if (call.isCallTo(SingletonEntity.hasComponentFun)) {
            transformCall(
                expression = call,
                redirectTo = ExEcsGeneratedCallsObject.hasComponentFromSingletonEntityByIdFun,
                typeArgRequired = false
            )
        }
        else if (call.isCallTo(SingletonEntity.removeComponentFun)) {
            transformCall(
                expression = call,
                redirectTo = ExEcsGeneratedCallsObject.removeComponentFromSingletonEntityByComponentTypeIdFun,
                typeArgRequired = false
            )
        }
        else if (call.isCallTo(SingletonEntity.addPoolableComponentFun)) {
            transformAddPoolableComponent(call)
        }
    }


    private fun transformCall(
        expression: IrCall,
        redirectTo: MethodDescriber,
        typeArgRequired: Boolean
    ) {

        val componentType = expression.getTypeArgument(0)!!
        val componentClass = componentType.getClass()!!
        if (componentClass.isInterface || componentClass.modality == Modality.ABSTRACT) return

        transformCurrentLazy {

            val builder = builderOf(ExEcsGeneratedCallsObject.irType.getClass()!!)

            builder.irCall(redirectTo.single())
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


    }


    private fun transformAddPoolableComponent(expression: IrCall) {

        val componentType = expression.getTypeArgument(0)!!

        val componentClass = componentType.getClass()!!

        if (componentClass.isInterface || componentClass.modality == Modality.ABSTRACT) return

        transformCurrentLazy {

            val companionClass = componentClass.companionObject() ?: return@transformCurrentLazy expression

            val poolProperty = companionClass.properties
                .find { it.hasAnnotation(ExEcsAnnotations.GeneratedDefaultPoolProperty.symbol) }
                ?: return@transformCurrentLazy expression

            val builder = builderOf(ExEcsGeneratedCallsObject.irType.getClass()!!)

            val poolBuilder = builderOf(poolProperty)

            val getPoolCall = builder.irCall(poolProperty.getter!!)
                .apply {
                    dispatchReceiver = poolBuilder.irGetObject(companionClass.symbol)
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

            return@transformCurrentLazy resultCall
        }
    }
}