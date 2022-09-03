package com.rdude.exECS.plugin.describer

object UniqueComponent : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.component.UniqueComponent"

    val entityIdProperty by lazy { PropertyDescriber("$fqNameString.entityId", this) }

}