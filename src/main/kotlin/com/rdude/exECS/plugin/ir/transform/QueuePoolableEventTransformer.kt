package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.*
import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.utils.isCallTo
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.properties

class QueuePoolableEventTransformer : IrTransformerElement() {

    override fun visitCall(call: IrCall) {

        val methodDescriber =
            if (call.isCallTo(World.queuePoolableEventFun)) World.queuePoolableEventFun
            else if (call.isCallTo(World.queuePoolableEventWithPriorityFun)) World.queuePoolableEventWithPriorityFun
            else if (call.isCallTo(World.queuePoolableEventWithApplyFun)) World.queuePoolableEventWithApplyFun
            else if (call.isCallTo(World.queuePoolableEventWithPriorityAndApplyFun)) World.queuePoolableEventWithPriorityAndApplyFun
            else if (call.isCallTo(WorldAccessor.queuePoolableEventFun)) WorldAccessor.queuePoolableEventFun
            else if (call.isCallTo(WorldAccessor.queuePoolableEventWithPriorityFun)) WorldAccessor.queuePoolableEventWithPriorityFun
            else if (call.isCallTo(WorldAccessor.queuePoolableEventWithApplyFun)) WorldAccessor.queuePoolableEventWithApplyFun
            else if (call.isCallTo(WorldAccessor.queuePoolableEventWithPriorityAndApplyFun)) WorldAccessor.queuePoolableEventWithPriorityAndApplyFun
            else return

        transformCurrentLazy {

            val eventType = call.getTypeArgument(0)!!

            val poolableClass = eventType.getClass()!!

            val companionClass = poolableClass.companionObject() ?: return@transformCurrentLazy call
            val poolProperty = companionClass.properties
                .find { it.hasAnnotation(ExEcsAnnotations.GeneratedDefaultPoolProperty.symbol) }
                ?: return@transformCurrentLazy call

            val applyFunction = when (methodDescriber) {
                World.queuePoolableEventWithApplyFun, WorldAccessor.queuePoolableEventWithApplyFun -> call.getValueArgument(
                    0
                )!!
                World.queuePoolableEventWithPriorityAndApplyFun, WorldAccessor.queuePoolableEventWithPriorityAndApplyFun -> call.getValueArgument(
                    1
                )!!
                else -> null
            }

            val eventPriorityArg = when (methodDescriber) {
                World.queuePoolableEventWithPriorityFun,
                WorldAccessor.queuePoolableEventWithPriorityFun,
                World.queuePoolableEventWithPriorityAndApplyFun,
                WorldAccessor.queuePoolableEventWithPriorityAndApplyFun -> call.getValueArgument(0)!!
                else -> null
            }

            val builder = builderOf(poolProperty)

            val getPoolCall = builder.irCall(poolProperty.getter!!)
                .apply {
                    dispatchReceiver = builder.irGetObject(companionClass.symbol)
                    type = Pool.irTypeWith(eventType)
                }

            val obtainComponentCall = builder.irCall(Pool.obtainFun.single())
                .apply {
                    dispatchReceiver = getPoolCall
                    type = eventType
                }

            val applyCall = (if (applyFunction == null) null else builder.irCall(Kotlin.applyFun.single()))
                ?.apply {
                    extensionReceiver = obtainComponentCall
                    putValueArgument(0, applyFunction)
                    putTypeArgument(0, eventType)
                    type = eventType
                }

            val resultCall =
                if (call.dispatchReceiver?.type?.isSubtypeOfClass(World.irType.classOrNull!!) == true) {
                    builder.irCall(World.queueEventFun.single())
                }
                else if (call.dispatchReceiver?.type?.isSubtypeOfClass(WorldAccessor.irType.classOrNull!!) == true) {
                    builder.irCall(WorldAccessor.queueEventFun.single())
                }
                else throw IllegalStateException("Unknown queueEvent function call receiver")

            return@transformCurrentLazy resultCall
                .apply {
                    dispatchReceiver = call.dispatchReceiver
                    putValueArgument(0, applyCall ?: obtainComponentCall)
                    if (eventPriorityArg != null) putValueArgument(1, eventPriorityArg)
                    type = call.type
                }
        }
    }

}