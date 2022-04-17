package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.utils.reference.Kotlin
import com.rdude.exECS.plugin.ir.utils.reference.Pool
import com.rdude.exECS.plugin.ir.utils.reference.System
import com.rdude.exECS.plugin.ir.utils.reference.World
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

    private lateinit var currentCall: IrCall

    fun transform(callData: CallsFinder.CallData) {
        currentCall = callData.call
        callData.insideFunction.transform(this, null)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val expression = super.visitCall(expression) as IrCall
        if (expression != currentCall) return expression

        val type = expression.getTypeArgument(0)!!

        val applyFunction =
            if (expression.valueArgumentsCount == 1) expression.getValueArgument(0)!!
            else null

        val poolPropertyInfo = poolsMapper[type] ?: return expression

        val builder = builderOf(poolPropertyInfo.property)

        val getPoolCall = builder.irCall(poolPropertyInfo.property.getter!!)
        getPoolCall.dispatchReceiver = builder.irGetObject(poolPropertyInfo.companion.symbol)
        getPoolCall.type = Pool.irTypeWith(type)

        val obtainComponentCall = builder.irCall(Pool.obtainFun)
        obtainComponentCall.dispatchReceiver = getPoolCall
        obtainComponentCall.type = type

        val applyCall = if (applyFunction == null) null else builder.irCall(Kotlin.applyFun)
        if (applyCall != null) {
            applyCall.extensionReceiver = obtainComponentCall
            applyCall.putValueArgument(0, applyFunction)
            applyCall.putTypeArgument(0, type)
            applyCall.type = type
        }

        val resultCall =
            if (expression.dispatchReceiver?.type?.isSubtypeOfClass(World.irType.classOrNull!!) == true) {
                builder.irCall(World.queueEventFun)
            } else if (expression.dispatchReceiver?.type?.isSubtypeOfClass(System.irType.classOrNull!!) == true) {
                builder.irCall(System.queueEventFun)
            } else throw IllegalStateException("Unknown queueEvent function call receiver")

        resultCall.dispatchReceiver = expression.dispatchReceiver
        resultCall.putValueArgument(0, applyCall ?: obtainComponentCall)
        resultCall.type = expression.type

        return resultCall
    }

}