package com.rdude.exECS.plugin.ir.visit

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.properties

class PoolsMapper {

    private val map = mutableMapOf<IrType, PoolPropertyInfo>()

    operator fun get(type: IrType): PoolPropertyInfo? {
        val stored = map[type]
        if (stored != null) return stored

        val companionObject = type.classOrNull?.owner?.companionObject() ?: return null

        val classFqName = type.classFqName?.asString() ?: return null

        val property = companionObject.properties
            .find { it.name.asString() == "execs_generated_pool_for_${classFqName.replace(".", "_")}" }
            ?: return null

        val poolPropertyInfo = PoolPropertyInfo(property, companionObject)
        map[type] = poolPropertyInfo
        return poolPropertyInfo
    }

    fun store(type: IrType, property: IrProperty, companion: IrClass) {
        if (map[type] == null) {
            map[type] = PoolPropertyInfo(property, companion)
        }
    }

    class PoolPropertyInfo(val property: IrProperty, val companion: IrClass)

}