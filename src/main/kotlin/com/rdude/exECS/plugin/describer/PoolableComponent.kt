package com.rdude.exECS.plugin.describer

object PoolableComponent : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.component.PoolableComponent"

    val insideEntitiesProperty by lazy { PropertyDescriber("$fqNameString.insideEntities", this) }

}