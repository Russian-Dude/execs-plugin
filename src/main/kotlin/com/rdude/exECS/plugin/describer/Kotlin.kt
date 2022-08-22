package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.name.FqName

object Kotlin {

    val applyFun by lazy {
        MetaData.context.referenceFunctions(FqName("kotlin.apply"))
            .single {
                it.owner.valueParameters.size == 1
                        && it.owner.valueParameters[0].type.isFunction()
                        && it.owner.typeParameters.size == 1
                        && it.owner.isInline
            }
            .toMethodDescriber()
    }

    object JvmFieldAnnotation : AnnotationDescriber("kotlin.jvm.JvmField")

    object TransientAnnotation : AnnotationDescriber("kotlin.jvm.Transient")

    object JvmStaticAnnotation : AnnotationDescriber("kotlin.jvm.JvmStatic")

}