package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.utils.reference.Kotlin
import com.rdude.exECS.plugin.ir.utils.reference.Pool
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import com.rdude.exECS.plugin.ir.visit.PoolsMapper
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.util.isFunction

class FromPoolCallsTransformer(private val poolsMapper: PoolsMapper) : IrElementTransformerVoidWithContext() {

    private lateinit var currentCall: IrCall

    fun transform(callData: CallsFinder.CallData) {
        currentCall = callData.call
        callData.insideFunction.transform(this, null)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val expression = super.visitCall(expression) as IrCall
        if (expression != currentCall) return expression

        val type = expression.getTypeArgument(0)!!

        val poolPropertyInfo = poolsMapper[type] ?: return expression

        val builder = builderOf(poolPropertyInfo.property)

        val getPoolCall = builder.irCall(poolPropertyInfo.property.getter!!)
        getPoolCall.dispatchReceiver = builder.irGetObject(poolPropertyInfo.companion.symbol)

        val obtainFun = builder.irCall(Pool.obtainFun)
        obtainFun.dispatchReceiver = getPoolCall
        obtainFun.type = expression.type

        val resultCall =
            // fromPool<T>()
            if (expression.valueArgumentsCount == 0) {
                obtainFun
            }
            // fromPool(T::class)
            else if (expression.valueArgumentsCount == 1 && expression.getValueArgument(0)!!.type.isKClass()) {
                obtainFun
            }
            // fromPool<T> { ... }
            else if (expression.valueArgumentsCount == 1 && expression.getValueArgument(0)!!.type.isFunction()) {
                val applyFunction = expression.getValueArgument(0)
                val applyCall = builder.irCall(Kotlin.applyFun)
                applyCall.extensionReceiver = obtainFun
                applyCall.putValueArgument(0, applyFunction)
                applyCall.putTypeArgument(0, type)
                applyCall
            }
            // fromPool(T::class) { ... }
            else if (expression.valueArgumentsCount == 2
                && expression.getValueArgument(0)!!.type.isKClass()
                && expression.getValueArgument(1)!!.type.isFunction()
            ) {
                val applyFunction = expression.getValueArgument(1)
                val applyCall = builder.irCall(Kotlin.applyFun)
                applyCall.extensionReceiver = obtainFun
                applyCall.putValueArgument(0, applyFunction)
                applyCall.putTypeArgument(0, type)
                applyCall
            }
            else throw IllegalStateException("Can not transform fromPool call")

/*        val resultCall = builder.irCall(Pool.obtainFun)
        resultCall.dispatchReceiver = getPoolCall
        resultCall.type = expression.type*/

        return resultCall
    }
}