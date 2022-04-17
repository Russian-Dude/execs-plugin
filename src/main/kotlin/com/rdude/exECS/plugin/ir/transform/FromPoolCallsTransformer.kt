package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.utils.reference.Pool
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import com.rdude.exECS.plugin.ir.visit.PoolsMapper
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

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

        val resultCall = builder.irCall(Pool.obtainFun)
        resultCall.dispatchReceiver = getPoolCall
        resultCall.type = expression.type

        return resultCall
    }
}