package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.name.FqName

class MethodDescriber(val symbols: List<IrSimpleFunctionSymbol>) : Describer {

    fun single() = symbols.single()


    companion object {

        operator fun invoke(fqName: String, apply: Builder.() -> Unit): MethodDescriber {
            val builder = Builder(fqName)
            builder.apply()
            return builder.create()
        }

    }



    class Builder(private val fqName: String) {

        private var typeParams = mutableListOf<(IrType) -> Boolean>()

        private var valueParams = mutableListOf<(IrType) -> Boolean>()

        private var returnType: List<IrType>? = null

        var extensionReceiverType: IrType? = null

        var dispatchReceiverType: IrType? = null

        fun withTypeParam(vararg paramTypes: IrType, extraPredicate: (IrType) -> Boolean = { true }) {
            typeParams.add { type ->
                extraPredicate.invoke(type) &&
                        (paramTypes.isEmpty() || paramTypes.any {
                            val cl = it.classOrNull
                            cl != null && type.isSubtypeOfClass(cl)
                        })
            }
        }

        fun withValueParam(vararg paramTypes: IrType, extraPredicate: (IrType) -> Boolean = { true }) {
            valueParams.add { type ->
                extraPredicate.invoke(type) &&
                        (paramTypes.isEmpty() || paramTypes.any {
                            val cl = it.classOrNull
                            cl != null && type.isSubtypeOfClass(cl)
                        })
            }
        }

        fun returnType(vararg paramTypes: IrType) {
            returnType = paramTypes.toList()
        }

        fun create() = MetaData.context.referenceFunctions(FqName(fqName))
            .filter {
                checkTypeParameters(it.owner.typeParameters)
                        && checkValueParameters(it.owner.valueParameters)
                        && checkReturnType(it.owner.returnType)
                        && checkDispatchReceiverType(it.owner.dispatchReceiverParameter?.type)
                        && checkExtensionReceiverType(it.owner.extensionReceiverParameter?.type)
            }.toMethodDescriber()

        private fun checkTypeParameters(toCheck: List<IrTypeParameter>): Boolean {
            if (typeParams.size != toCheck.size) return false
            for (i in 0 until typeParams.size) {
                if (!typeParams[i].invoke(toCheck[i].defaultType)) return false
            }
            return true
        }

        private fun checkValueParameters(toCheck: List<IrValueParameter>): Boolean {
            if (valueParams.size != toCheck.size) return false
            for (i in 0 until valueParams.size) {
                if (!valueParams[i].invoke(toCheck[i].type)) return false
            }
            return true
        }

        private fun checkReturnType(toCheck: IrType): Boolean {
            if (returnType == null) {
                return toCheck.isUnit()
            }
            return returnType!!.all { toCheck.isSubtypeOfClass(it.classOrNull!!) }
        }

        private fun checkDispatchReceiverType(toCheck: IrType?): Boolean {
            if (dispatchReceiverType == toCheck) return true
            if (dispatchReceiverType == null) return false
            return toCheck!!.isSubtypeOfClass(dispatchReceiverType!!.classOrNull!!)
        }

        private fun checkExtensionReceiverType(toCheck: IrType?): Boolean {
            if (extensionReceiverType == toCheck) return true
            if (extensionReceiverType == null) return false
            return toCheck!!.isSubtypeOfClass(extensionReceiverType!!.classOrNull!!)
        }

    }

}