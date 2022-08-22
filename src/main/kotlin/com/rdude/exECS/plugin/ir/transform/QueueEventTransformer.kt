package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.Kotlin
import com.rdude.exECS.plugin.describer.Pool
import com.rdude.exECS.plugin.describer.World
import com.rdude.exECS.plugin.describer.WorldAccessor
import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import com.rdude.exECS.plugin.ir.visit.PoolsMapper
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass

class QueueEventTransformer(private val poolsMapper: PoolsMapper) : IrElementTransformerVoidWithContext() {

    private lateinit var currentCallData: CallsFinder.CallData

    fun transform(callData: CallsFinder.CallData) {
        currentCallData = callData
        callData.insideFunction.transform(this, null)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val expression = super.visitCall(expression) as IrCall
        if (expression != currentCallData.call) return expression

        val eventType = expression.getTypeArgument(0)!!

        val applyFunction = when(currentCallData.methodDescriber) {
            World.queuePoolableEventWithApplyFun, WorldAccessor.queuePoolableEventWithApplyFun -> expression.getValueArgument(0)!!
            World.queuePoolableEventWithPriorityAndApplyFun, WorldAccessor.queuePoolableEventWithPriorityAndApplyFun -> expression.getValueArgument(1)!!
            else -> null
        }

        val eventPriorityArg = when(currentCallData.methodDescriber) {
            World.queuePoolableEventWithPriorityFun,
            WorldAccessor.queuePoolableEventWithPriorityFun,
            World.queuePoolableEventWithPriorityAndApplyFun,
            WorldAccessor.queuePoolableEventWithPriorityAndApplyFun -> expression.getValueArgument(0)!!
            else -> null
        }

        val poolPropertyInfo = poolsMapper[eventType] ?: return expression

        val builder = builderOf(poolPropertyInfo.property)

        val getPoolCall = builder.irCall(poolPropertyInfo.property.getter!!)
            .apply {
                dispatchReceiver = builder.irGetObject(poolPropertyInfo.companion.symbol)
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
            if (expression.dispatchReceiver?.type?.isSubtypeOfClass(World.irType.classOrNull!!) == true) {
                builder.irCall(World.queueEventFun.single())
            }
            else if (expression.dispatchReceiver?.type?.isSubtypeOfClass(WorldAccessor.irType.classOrNull!!) == true) {
                builder.irCall(WorldAccessor.queueEventFun.single())
            }
            else throw IllegalStateException("Unknown queueEvent function call receiver")

        return resultCall
            .apply {
                dispatchReceiver = expression.dispatchReceiver
                putValueArgument(0, applyCall ?: obtainComponentCall)
                if (eventPriorityArg != null) putValueArgument(1, eventPriorityArg)
                type = expression.type
            }
    }

}