package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.FqName

object IdFactory {

    val irType by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.utils.collections.IdFactory"))!!.defaultType }

    val obtainFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.utils.collections.IdFactory.obtain"))
            .single { it.owner.valueParameters.isEmpty()
                    && it.owner.typeParameters.isEmpty()
                    && it.owner.returnType == MetaData.context.irBuiltIns.intType }
    }

    val constructor by lazy {
        MetaData.context.referenceConstructors(FqName("com.rdude.exECS.utils.collections.IdFactory"))
            .single { it.owner.valueParameters.isEmpty() && it.owner.typeParameters.isEmpty() }
    }
}