package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.name.FqName

object ComponentMapper : Reference {

    val classSymbol by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.component.ComponentMapper"))!! }

    val irType by lazy { classSymbol.defaultType }

    val getComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.component.ComponentMapper.get"))
            .single { it.owner.valueParameters.size == 1 && it.owner.valueParameters[0].type.isInt() }
    }

    val hasComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.component.ComponentMapper.hasComponent"))
            .single { it.owner.valueParameters.size == 1 && it.owner.valueParameters[0].type.isInt() }
    }

    val removeComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.component.ComponentMapper.removeComponent"))
            .single { symbol -> symbol.owner.valueParameters.size == 1 && symbol.owner.valueParameters[0].type.isInt() }
    }

    val addComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.component.ComponentMapper.addComponent"))
            .single { symbol -> symbol.owner.valueParameters.size == 2
                    && symbol.owner.valueParameters[0].type.isInt()
                    && symbol.owner.valueParameters[1].type.isSubtypeOfClass(Component.irType!!.classOrNull!!) }
    }

    val addComponentWithApplyFun by lazy {
        MetaData.context.referenceFunctions(FqName("com.rdude.exECS.component.ComponentMapper.addComponent"))
            .single { symbol -> symbol.owner.valueParameters.size == 3
                    && symbol.owner.valueParameters[0].type.isInt()
                    && symbol.owner.valueParameters[1].type.isSubtypeOfClass(Component.irType!!.classOrNull!!)
                    && symbol.owner.valueParameters[2].type.isFunction() }
    }
}