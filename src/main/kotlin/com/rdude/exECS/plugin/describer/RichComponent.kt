package com.rdude.exECS.plugin.describer

object RichComponent : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.component.RichComponent"

    val entitiesIdsProperty by lazy { PropertyDescriber("$fqNameString.insideEntitiesSet", this) }

}