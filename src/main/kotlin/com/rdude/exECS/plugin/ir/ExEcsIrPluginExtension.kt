package com.rdude.exECS.plugin.ir

import com.rdude.exECS.plugin.ir.debug.DebugVisitor
import com.rdude.exECS.plugin.ir.transform.EntityWrapperToComponentMapperCallsTransformer
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.reference.EntityWrapper
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import com.rdude.exECS.plugin.ir.visit.ClassesFinder
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class ExEcsIrPluginExtension() : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        MetaData.context = pluginContext

        val componentFqName = "com.rdude.exECS.component.Component"
        val systemFqName = "com.rdude.exECS.system.System"

        val classes: MutableMap<String, MutableList<IrClass>> = HashMap()

        // find subtypes of System and Component
        val classesFinder = ClassesFinder(listOf(componentFqName, systemFqName))
        moduleFragment.accept(classesFinder, classes)


        // find and transform calls to EntityWrapper methods inside system subclasses
        if (classes[systemFqName]?.isNotEmpty() == true) {

            // methods to transform
            val transformMethods = listOf(
                EntityWrapper.getComponentFun,
                EntityWrapper.hasComponentFun,
                EntityWrapper.removeComponentFun,
                EntityWrapper.addComponentFun)

            // find
            val entityWrapperMethodCallsData = CallsFinder().find(
                moduleFragment = moduleFragment,
                representations = transformMethods,
                inClasses = classes[systemFqName]!!
            )

            // transform
            val entityWrapperToComponentMapperCallsTransformer = EntityWrapperToComponentMapperCallsTransformer()
            entityWrapperMethodCallsData.forEach { entityWrapperToComponentMapperCallsTransformer.transform(it) }
        }


        // debug
        moduleFragment.accept(DebugVisitor(), null)
    }
}