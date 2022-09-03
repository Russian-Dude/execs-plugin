package com.rdude.exECS.plugin.describer

object ObservableComponent : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.component.ObservableComponent"

    val worldProperty by lazy { PropertyDescriber("$fqNameString.world", this) }
}