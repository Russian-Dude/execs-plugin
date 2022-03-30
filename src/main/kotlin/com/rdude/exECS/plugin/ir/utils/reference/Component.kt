package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.FqName

object Component : HasId {

    override val irType by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.component.Component"))!!.defaultType }

    override val getTypeIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.component.Component.getComponentTypeId"))
            .single { it.owner.returnType == MetaData.context.irBuiltIns.intType && it.owner.valueParameters.isEmpty() && it.owner.typeParameters.isEmpty() }
    }

    override val idPropertyNamePrefix: String = "Component"
}