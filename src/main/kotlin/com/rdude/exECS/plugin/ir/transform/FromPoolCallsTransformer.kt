package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.ExEcsAnnotations
import com.rdude.exECS.plugin.describer.ExEcsExternal
import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.Pool
import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.utils.isCallTo
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.properties

class FromPoolCallsTransformer : IrTransformerElement() {

    override fun visitCall(call: IrCall) {
        if (!call.isCallTo(ExEcsExternal.fromPoolFun)) return

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
                    this.type = call.type
                }

            // fromPool<T>()
            return@transformCurrentLazy if (call.valueArgumentsCount == 0) {
                obtainCall
            }
            // fromPool(T::class)
            else if (call.valueArgumentsCount == 1 && call.getValueArgument(0)!!.type.isKClass()) {
                obtainCall
            }
            // fromPool<T> { ... }
            else if (call.valueArgumentsCount == 1 && call.getValueArgument(0)!!.type.isFunction()) {
                val applyFunction = call.getValueArgument(0)
                val applyCall = builder.irCall(Kotlin.applyFun.single())
                applyCall.extensionReceiver = obtainCall
                applyCall.putValueArgument(0, applyFunction)
                applyCall.putTypeArgument(0, type)
                applyCall
            }
            // fromPool(T::class) { ... }
            else if (call.valueArgumentsCount == 2
                && call.getValueArgument(0)!!.type.isKClass()
                && call.getValueArgument(1)!!.type.isFunction()
            ) {
                val applyFunction = call.getValueArgument(1)
                val applyCall = builder.irCall(Kotlin.applyFun.single())
                applyCall.extensionReceiver = obtainCall
                applyCall.putValueArgument(0, applyFunction)
                applyCall.putTypeArgument(0, type)
                applyCall
            } else throw IllegalStateException("Can not transform fromPool call")
        }
    }
}