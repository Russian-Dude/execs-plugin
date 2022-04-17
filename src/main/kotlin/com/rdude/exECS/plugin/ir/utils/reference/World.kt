package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.SimpleRepresentation
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.name.FqName

object World : Reference {

    val irType by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.world.World"))!!.defaultType }

    private val queuePoolableEventFunctions = listOf(
        // queueEvent<T>()
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.world.World.queueEvent"))
            .filter {
                it.owner.valueParameters.isEmpty()
                        && it.owner.typeParameters.size == 1
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType.classOrNull!!)
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Event.irType.classOrNull!!)
            }
            .single(),
        // queueEvent<T> { ... }
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.world.World.queueEvent"))
            .filter {
                it.owner.valueParameters.size == 1
                        && it.owner.typeParameters.size == 1
                        && it.owner.valueParameters[0].type.isFunction()
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.irType.classOrNull!!)
                        && it.owner.typeParameters[0].defaultType.isSubtypeOfClass(Event.irType.classOrNull!!)
            }
            .single()
    )
        .map { it.owner }

    val queuePoolableEventFun = object : SimpleRepresentation<IrCall> {
        override fun invoke(irCall: IrCall): Boolean {
            return queuePoolableEventFunctions.contains(irCall.symbol.owner)
        }
    }


    val queueEventFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.world.World.queueEvent"))
            .single {
                it.owner.valueParameters.size == 1
                        && it.owner.valueParameters[0].type.isSubtypeOfClass(Event.irType.classOrNull!!)
            }
    }

}