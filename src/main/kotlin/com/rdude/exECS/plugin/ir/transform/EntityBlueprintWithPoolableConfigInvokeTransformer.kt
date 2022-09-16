package com.rdude.exECS.plugin.ir.transform

import com.rdude.exECS.plugin.describer.EntityBlueprint
import com.rdude.exECS.plugin.describer.ExEcsAnnotations
import com.rdude.exECS.plugin.describer.Pool
import com.rdude.exECS.plugin.ir.utils.IR_FACTORY
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.builderOf
import com.rdude.exECS.plugin.ir.utils.isCallTo
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.Name

class EntityBlueprintWithPoolableConfigInvokeTransformer : IrTransformerElement() {

    private val callToProperty = mutableMapOf<IrCall, IrProperty>()

    override fun visitProperty(property: IrProperty) {
        val irCall = property.backingField?.initializer?.expression as? IrCall ?: return
        if (irCall.isCallTo(EntityBlueprint.invokeWithPoolableConfigFun)) {
            callToProperty[irCall] = property
        }
    }

    override fun visitCall(call: IrCall) {
        if (!call.isCallTo(EntityBlueprint.invokeWithPoolableConfigFun)) return
        val property = callToProperty[call] ?: return

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

            val function = IR_FACTORY.buildFun {
                this.startOffset = companionClass.startOffset
                this.endOffset = companionClass.endOffset
                this.returnType = type
                this.name = Name.identifier("<anonymous>")
                this.visibility = DescriptorVisibilities.LOCAL
                this.origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
            }
            function.body = DeclarationIrBuilder(
                MetaData.context,
                function.symbol,
                function.startOffset,
                function.endOffset
            ).irBlockBody {
                +irReturn(obtainCall)
            }

            function.parent = property.backingField!!

            val supplierExpression = IrFunctionExpressionImpl(
                startOffset = call.startOffset,
                endOffset = call.endOffset,
                type = MetaData.context.irBuiltIns.functionN(0).typeWith(call.getTypeArgument(0)!!),
                function = function,
                origin = IrStatementOrigin.LAMBDA
            )

            builder.irCall(EntityBlueprint.invokeWithAnyConfigFun.single()).apply {
                dispatchReceiver = call.dispatchReceiver
                putValueArgument(0, supplierExpression)
                putValueArgument(1, call.getValueArgument(0))
                putTypeArgument(0, type)
                this.type = call.type
            }
        }
    }
}