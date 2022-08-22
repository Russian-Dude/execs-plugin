package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.name.FqName

object Component : ClassDescriber(), HasTypeId {

    const val TYPE_ID_PROPERTY_NAME_PREFIX = "execs_generated_component_type_id_property_for_"

    override val fqNameString = "com.rdude.exECS.component.Component"

    override val name: String = "Component"

    override val getTypeIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.getComponentTypeId"))
            .single {
                it.owner.returnType == MetaData.context.irBuiltIns.intType
                        && it.owner.valueParameters.isEmpty() && it.owner.typeParameters.isEmpty()
            }
            .toMethodDescriber()
    }

    override fun getTypeIdPropertyNamePrefix(): String = TYPE_ID_PROPERTY_NAME_PREFIX
}