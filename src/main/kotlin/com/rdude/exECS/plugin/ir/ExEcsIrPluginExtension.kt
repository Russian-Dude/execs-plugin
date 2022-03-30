package com.rdude.exECS.plugin.ir

import com.rdude.exECS.plugin.ir.lowering.GeneratedIdCompanionAndAccessFunctionAdder
import com.rdude.exECS.plugin.ir.transform.EntityWrapperToComponentMapperCallsTransformer
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.reference.Component
import com.rdude.exECS.plugin.ir.utils.reference.EntityWrapper
import com.rdude.exECS.plugin.ir.utils.reference.Event
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import com.rdude.exECS.plugin.ir.visit.ClassesFinder
import com.rdude.exECS.plugin.ir.visit.CompanionsFinder
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class ExEcsIrPluginExtension() : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        MetaData.context = pluginContext

        val componentFqName = "com.rdude.exECS.component.Component"
        val systemFqName = "com.rdude.exECS.system.System"
        val eventFqName = "com.rdude.exECS.event.Event"

        // classes plugin is interested in
        val classes: MutableMap<String, MutableList<IrClass>> = HashMap()

        // find subtypes of System, Component and Event
        val classesFinder = ClassesFinder(listOf(componentFqName, systemFqName, eventFqName))
        moduleFragment.accept(classesFinder, classes)

        // find existing companion objects of classes that plugin is interested in
        val companionsFinder = CompanionsFinder()
        val existingCompanions = companionsFinder.find(classes.values.flatten())

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


        val generatedIdAdder = GeneratedIdCompanionAndAccessFunctionAdder(existingCompanions)
        // add companions that holds type ids to events
        if (classes[eventFqName]?.isNotEmpty() == true) {
            generatedIdAdder.addTo(classes[eventFqName]!!, Event)
        }
        // add companions that holds type ids to components
        if (classes[componentFqName]?.isNotEmpty() == true) {
            generatedIdAdder.addTo(classes[componentFqName]!!, Component)
        }


        // debug
        //moduleFragment.accept(DebugVisitor(), null)
    }
}