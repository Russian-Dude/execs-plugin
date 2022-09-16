package com.rdude.exECS.plugin.ir

import com.rdude.exECS.plugin.ir.check.PoolableObjectChecker
import com.rdude.exECS.plugin.ir.check.UniqueAndRichComponentInterceptionChecker
import com.rdude.exECS.plugin.ir.debug.DebugPrinter
import com.rdude.exECS.plugin.ir.transform.*
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.transformUsing
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class ExEcsIrPluginExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        MetaData.context = pluginContext

        moduleFragment.transformUsing(
            PoolableObjectChecker(),
            UniqueAndRichComponentInterceptionChecker(),
            PoolableToPoolableComponentTransformer(),
            FakeToRealOverridePoolableComponentInsideEntitiesPropertyTransformer(),
            FakeToRealOverrideUniqueComponentEntityIdPropertyTransformer(),
            FakeToRealOverrideRichComponentEntitiesIdsPropertyTransformer(),
            FakeToRealOverrideObservableComponentWorldPropertyTransformer(),
            FakeToRealOverridePoolablePoolPropertyTransformer(),
            FakeToRealOverridePoolableIsInPoolPropertyTransformer(),
            DefaultPoolsAdder(),
            FromPoolCallsTransformer(),
            TypeIdStaticPropertyAndGetTypeIdMethodTransformer(),
            WorldAccessorCallsTransformer(),
            QueuePoolableEventTransformer(),
            SingletonEntityMethodsTransformer(),
            EntityBlueprintWithPoolableConfigInvokeTransformer(),
            EntityBuilderWithPoolableComponentFunTransformer(),
            DebugPrinter()
        )
    }
}