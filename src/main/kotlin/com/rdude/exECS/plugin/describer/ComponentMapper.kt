package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.name.FqName

object ComponentMapper : ClassDescriber() {

    override val fqNameString = "com.rdude.exECS.component.ComponentMapper"

    val getComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.get"))
            .single { it.owner.valueParameters.size == 1 && it.owner.valueParameters[0].type.isInt() }
            .toMethodDescriber()
    }

    val hasComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.hasComponent"))
            .single { it.owner.valueParameters.size == 1 && it.owner.valueParameters[0].type.isInt() }
            .toMethodDescriber()
    }

    val removeComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.removeComponent"))
            .single { symbol -> symbol.owner.valueParameters.size == 1 && symbol.owner.valueParameters[0].type.isInt() }
            .toMethodDescriber()
    }

    val addComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.addComponent"))
            .single { symbol ->
                symbol.owner.valueParameters.size == 2
                        && symbol.owner.valueParameters[0].type.isInt()
                        && symbol.owner.valueParameters[1].type.isSubtypeOfClass(Component.irType.classOrNull!!)
            }
            .toMethodDescriber()
    }
}