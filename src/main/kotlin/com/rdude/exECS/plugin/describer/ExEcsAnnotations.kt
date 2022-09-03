package com.rdude.exECS.plugin.describer

object ExEcsAnnotations {

    object ConstructorForDefaultPool : AnnotationDescriber("com.rdude.exECS.pool.ConstructorForDefaultPool")

    object CachedComponentMapperProperty : AnnotationDescriber("com.rdude.exECS.plugin.CachedComponentMapperProperty")

    object CachedSingletonEntityProperty : AnnotationDescriber("com.rdude.exECS.plugin.CachedSingletonEntityProperty")

    object CachedSystemProperty : AnnotationDescriber("com.rdude.exECS.plugin.CachedSystemProperty")

    object GeneratedTypeIdProperty : AnnotationDescriber("com.rdude.exECS.plugin.GeneratedTypeIdProperty")

    object GeneratedDefaultPoolProperty : AnnotationDescriber("com.rdude.exECS.plugin.GeneratedDefaultPoolProperty")

    object DebugIR : AnnotationDescriber("com.rdude.exECS.plugin.DebugIR")

}