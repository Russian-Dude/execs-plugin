package com.rdude.exECS.plugin.describer

import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.toMethodDescriber
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.name.FqName

object EntityBuilder : ClassDescriber() {

    override val fqNameString: String = "com.rdude.exECS.entity.EntityBuilder"

    val withComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.withComponent"))
            .single { symbol ->
                symbol.owner.extensionReceiverParameter == null
                        && symbol.owner.valueParameters.size == 1 && symbol.owner.valueParameters[0].type.isSubtypeOfClass(Component.symbol)
            }
            .toMethodDescriber()
    }

    val withPoolableComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.withComponent"))
            .filter { symbol ->
                symbol.owner.extensionReceiverParameter == null
                        && symbol.owner.typeParameters.size == 1
                        && symbol.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.symbol)
                        && symbol.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.symbol)
                        && (
                        symbol.owner.valueParameters.isEmpty() || (symbol.owner.valueParameters.size == 1 && symbol.owner.valueParameters[0].type.isKClass())
                        )
            }
            .toMethodDescriber()
    }

    val withPoolableComponentWithApplyFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.withComponent"))
            .filter { symbol ->
                symbol.owner.extensionReceiverParameter == null
                        && symbol.owner.typeParameters.size == 1
                        && symbol.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.symbol)
                        && symbol.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.symbol)
                        && (
                        (symbol.owner.valueParameters.size == 1 && symbol.owner.valueParameters[0].type.isFunction())
                                || (symbol.owner.valueParameters.size == 2 && symbol.owner.valueParameters[0].type.isKClass() && symbol.owner.valueParameters[1].type.isFunction())
                        )
            }
            .toMethodDescriber()
    }

    val entityBlueprintWithComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.withComponent"))
            .single { symbol ->
                symbol.owner.extensionReceiverParameter?.type?.isSubtypeOfClass(EntityBlueprint.symbol) == true
                        && symbol.owner.valueParameters.size == 1 && symbol.owner.valueParameters[0].type.isSubtypeOfClass(Component.symbol)
            }
            .toMethodDescriber()
    }

    val entityBlueprintWithPoolableComponentFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.withComponent"))
            .filter { symbol ->
                symbol.owner.extensionReceiverParameter?.type?.isSubtypeOfClass(EntityBlueprint.symbol) == true
                        && symbol.owner.typeParameters.size == 1
                        && symbol.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.symbol)
                        && symbol.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.symbol)
                        && (
                        symbol.owner.valueParameters.isEmpty() || (symbol.owner.valueParameters.size == 1 && symbol.owner.valueParameters[0].type.isKClass())
                        )
            }
            .toMethodDescriber()
    }

    val entityBluepriintWithPoolableComponentWithApplyFun by lazy {
        MetaData.context.referenceFunctions(FqName("$fqNameString.withComponent"))
            .filter { symbol ->
                symbol.owner.extensionReceiverParameter?.type?.isSubtypeOfClass(EntityBlueprint.symbol) == true
                        && symbol.owner.typeParameters.size == 1
                        && symbol.owner.typeParameters[0].defaultType.isSubtypeOfClass(Poolable.symbol)
                        && symbol.owner.typeParameters[0].defaultType.isSubtypeOfClass(Component.symbol)
                        && (
                        (symbol.owner.valueParameters.size == 1 && symbol.owner.valueParameters[0].type.isFunction())
                                || (symbol.owner.valueParameters.size == 2 && symbol.owner.valueParameters[0].type.isKClass() && symbol.owner.valueParameters[1].type.isFunction())
                        )
            }
            .toMethodDescriber()
    }

}