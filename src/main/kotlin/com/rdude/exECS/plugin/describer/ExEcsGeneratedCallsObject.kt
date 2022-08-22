package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.name.FqName

object ExEcsGeneratedCallsObject : ClassDescriber() {

    override val fqNameString: String = "com.rdude.exECS.plugin.ExEcsGeneratedCalls"

    val getComponentFromSingletonEntityByIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.getComponentFromSingletonEntityByComponentTypeId"))
            .toList()
            .toMethodDescriber()
    }

    val hasComponentFromSingletonEntityByIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.hasComponentFromSingletonEntityByComponentTypeId"))
            .toList()
            .toMethodDescriber()
    }

    val removeComponentFromSingletonEntityByComponentTypeIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.removeComponentFromSingletonEntityByComponentTypeId"))
            .toList()
            .toMethodDescriber()
    }

    val getComponentTypeIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.getComponentTypeId"))
            .toList()
            .toMethodDescriber()
    }

    val getEventTypeIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.getEventTypeId"))
            .toList()
            .toMethodDescriber()
    }

    val getSystemTypeIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.getSystemTypeId"))
            .toList()
            .toMethodDescriber()
    }

    val getSingletonEntityTypeIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.getSingletonEntityTypeId"))
            .toList()
            .toMethodDescriber()
    }
}