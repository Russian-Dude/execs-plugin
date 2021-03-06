package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.FqName

object Component : HasId, Reference {

    val classSymbol by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.component.Component"))!! }
    override val irType by lazy { classSymbol.defaultType }

    override val getTypeIdFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.component.Component.getComponentTypeId"))
            .single { it.owner.returnType == MetaData.context.irBuiltIns.intType && it.owner.valueParameters.isEmpty() && it.owner.typeParameters.isEmpty() }
    }

    val getIdProperty by lazy {
        MetaData.context.referenceProperties(FqName("com.rdude.exECS.component.Component.componentId"))
            .single()
    }

    val insideEntitiesProperty by lazy {
        MetaData.context.referenceProperties(FqName("com.rdude.exECS.component.Component.insideEntities"))
            .single()
    }

    override val idPropertyNamePrefix: String = "Component"
}