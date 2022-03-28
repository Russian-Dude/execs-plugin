package com.rdude.exECS.plugin.ir.utils.reference

import com.rdude.exECS.plugin.ir.utils.MetaData
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.FqName

object System {

    val irType by lazy { MetaData.context.referenceClass(FqName("com.rdude.exECS.system.System"))!!.defaultType }


}