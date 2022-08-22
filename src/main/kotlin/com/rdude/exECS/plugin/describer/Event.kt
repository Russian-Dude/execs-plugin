package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.name.FqName

object Event : ClassDescriber(), HasTypeId {

    const val TYPE_ID_PROPERTY_NAME_PREFIX = "execs_generated_event_type_id_property_for_"

    override val fqNameString = "com.rdude.exECS.event.Event"

    override val name: String = "Event"

    override val getTypeIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.getEventTypeId"))
            .single {
                it.owner.returnType == MetaData.context.irBuiltIns.intType
                        && it.owner.valueParameters.isEmpty()
                        && it.owner.typeParameters.isEmpty()
            }
            .toMethodDescriber()
    }

    override fun getTypeIdPropertyNamePrefix(): String = TYPE_ID_PROPERTY_NAME_PREFIX
}