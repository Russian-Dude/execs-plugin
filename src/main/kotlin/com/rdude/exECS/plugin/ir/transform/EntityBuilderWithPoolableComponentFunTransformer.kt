package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.EntityBuilder
import com.rdude.exECS.plugin.describer.ExEcsAnnotations
import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.Pool
import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.utils.isCallTo
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.properties

class EntityBuilderWithPoolableComponentFunTransformer : IrTransformerElement() {

    override fun visitCall(call: IrCall) {
        if (
            !call.isCallTo(EntityBuilder.withPoolableComponentFun)
            && !call.isCallTo(EntityBuilder.withPoolableComponentWithApplyFun)
            && !call.isCallTo(EntityBuilder.entityBlueprintWithPoolableComponentFun)
            && !call.isCallTo(EntityBuilder.entityBluepriintWithPoolableComponentWithApplyFun)
        ) return

        transformCurrentLazy {

            val type = call.getTypeArgument(0)!!
            val poolableClass = type.getClass()!!

            val companionClass = poolableClass.companionObject() ?: return@transformCurrentLazy call
            val poolProperty = companionClass.properties
                .find { it.hasAnnotation(ExEcsAnnotations.GeneratedDefaultPoolProperty.symbol) }
                ?: return@transformCurrentLazy call

            val builder = builderOf(poolProperty)

            val getPoolCall = builder.irCall(poolProperty.getter!!)
                .apply {
                    dispatchReceiver = builder.irGetObject(companionClass.symbol)
                    this.type = Pool.irTypeWith(type)
                }

            val obtainCall = builder.irCall(Pool.obtainFun.single())
                .apply {
                    this.dispatchReceiver = getPoolCall
                    this.type = type
                }

            val applyArg =
                // withComponent<T> { ... }
                if (call.valueArgumentsCount == 1 && call.getValueArgument(0)!!.type.isFunction()) call.getValueArgument(0)
                // withComponent(T::class) { ... }
                else if (call.valueArgumentsCount == 2 && call.getValueArgument(1)!!.type.isFunction()) call.getValueArgument(1)
                else null

            val applyCall =
                if (applyArg == null) null
                else {
                    val applyCall = builder.irCall(Kotlin.applyFun.single())
                    applyCall.extensionReceiver = obtainCall
                    applyCall.putValueArgument(0, applyArg)
                    applyCall.putTypeArgument(0, type)
                    applyCall.type = type
                    applyCall
                }

            val resultBuilder = builderOf(EntityBuilder.irType.getClass()!!)

            resultBuilder.irCall(
                if (call.extensionReceiver != null) EntityBuilder.entityBlueprintWithComponentFun.single()
                else EntityBuilder.withComponentFun.single()
            ).apply {
                this.dispatchReceiver = call.dispatchReceiver
                this.extensionReceiver = call.extensionReceiver
                this.type = call.type
                putValueArgument(0, applyCall ?: obtainCall)
                if (call.extensionReceiver != null) putTypeArgument(0, call.extensionReceiver!!.type)
            }
        }
    }
}