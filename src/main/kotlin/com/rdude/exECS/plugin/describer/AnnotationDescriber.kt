package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.name.FqName

abstract class AnnotationDescriber(override val fqNameString: String) : ClassDescriber() {

    private val builder by lazy { DeclarationIrBuilder(MetaData.context, symbol) }

    val constructor by lazy { MetaData.context.referenceConstructors(FqName(fqNameString)).single() }

    fun constructorCall(vararg args: Any?): IrConstructorCall {
        val irCall = builder.irCall(constructor)
        args.forEachIndexed { index, arg ->
            val convertedArg = when(arg) {
                is IrType -> {
                    val cl = arg.getClass()!!
                    IrClassReferenceImpl(cl.startOffset, cl.endOffset, MetaData.context.irBuiltIns.kClassClass.defaultType, cl.symbol, arg)
                }
                is IrExpression -> arg
                is Int -> builder.irInt(arg)
                is String -> builder.irString(arg)
                null -> builder.irNull()
                else -> throw NotImplementedError("Call to annotation constructor with $arg argument is not implemented in AnnotationDescriber")
            }
            irCall.putValueArgument(index, convertedArg)
        }
        return irCall
    }

}