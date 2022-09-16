package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.name.FqName

object EntityBlueprint : ClassDescriber() {

    override val fqNameString: String = "com.rdude.exECS.entity.EntityBlueprint"

    val invokeWithPoolableConfigFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.Companion.invoke"))
            .single { symbol ->
                symbol.owner.typeParameters.size == 1
                        && symbol.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.symbol)
                        && symbol.owner.typeParameters[0].defaultType.isSubtypeOfClass(EntityBlueprintConfiguration.symbol)
                        && symbol.owner.valueParameters.size == 1
                        && symbol.owner.valueParameters[0].type.isFunction()
            }
            .toMethodDescriber()
    }

    val invokeWithAnyConfigFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.Companion.invoke"))
            .single { symbol ->
                symbol.owner.typeParameters.size == 1
                        && symbol.owner.typeParameters[0].defaultType.isSubtypeOfClass(EntityBlueprintConfiguration.symbol)
                        && symbol.owner.valueParameters.size == 2
                        && symbol.owner.valueParameters[0].type.isFunction()
                        && symbol.owner.valueParameters[1].type.isFunction()
            }
            .toMethodDescriber()
    }

}