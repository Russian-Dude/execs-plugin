package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.FakeOverrideFunctionRepresentation
import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

object System : Reference {

    val irType by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.system.System"))!!.defaultType }

    private fun getQueuePoolableEventFunctions(fakeOverrideInClass: IrClass): List<IrSimpleFunction> {
        val classFqName = fakeOverrideInClass.kotlinFqName.asString()
        return listOf(
            // queueEvent<T>()
            MetaData.context.referenceFunctions(FqName("$classFqName.queueEvent"))
                .filter {
                    it.owner.valueParameters.isEmpty()
                            && it.owner.typeParameters.size == 1
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType.classOrNull!!)
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Event.irType.classOrNull!!)
                },
            // queueEvent<T> { ... }
            MetaData.context.referenceFunctions(FqName("$classFqName.queueEvent"))
                .filter {
                    it.owner.valueParameters.size == 1
                            && it.owner.typeParameters.size == 1
                            && it.owner.valueParameters[0].type.isFunction()
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType.classOrNull!!)
                            && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Event.irType.classOrNull!!)
                }
        )
            .flatten()
            .map { it.owner }
    }

    private val fakeOverrideQueuePoolableEventFunctions = mutableMapOf<IrClass, List<IrSimpleFunction>>()

    val queuePoolableEventFun = object : FakeOverrideFunctionRepresentation {
        override fun invoke(irCall: IrCall, irClass: IrClass): Boolean {
            return fakeOverrideQueuePoolableEventFunctions.getOrPut(irClass) { getQueuePoolableEventFunctions(irClass) }
                .contains(irCall.symbol.owner)
        }
    }


    val queueEventFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.system.System.queueEvent"))
            .single {
                it.owner.valueParameters.size == 1
                        && it.owner.valueParameters[0].type.isSubtypeOfClass(Event.irType.classOrNull!!)
            }
    }

}