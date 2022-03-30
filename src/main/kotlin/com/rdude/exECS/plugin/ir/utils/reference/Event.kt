package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.FqName

object Event : HasId {

    override val irType by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.event.Event"))!!.defaultType }

    override val getTypeIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.event.Event.getEventTypeId"))
            .single { it.owner.returnType == MetaData.context.irBuiltIns.intType && it.owner.valueParameters.isEmpty() && it.owner.typeParameters.isEmpty() }
    }

    override val idPropertyNamePrefix: String = "Event"
}