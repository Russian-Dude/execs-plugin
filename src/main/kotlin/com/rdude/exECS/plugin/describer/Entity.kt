package com.rdude.exECS.plugin.describer

object Entity : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.entity.Entity"

    val entityIdProperty by lazy { PropertyDescriber("$fqNameString.id", this) }
}