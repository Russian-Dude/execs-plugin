package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.name.FqName

object Kotlin : Reference {

    val applyFun by lazy {
        MetaData.context.referenceFunctions(FqName("kotlin.apply"))
            .single { it.owner.valueParameters.size == 1
                    && it.owner.valueParameters[0].type.isFunction()
                    && it.owner.typeParameters.size == 1
                    && it.owner.isInline }
    }

}