package com.rdude.exECS.plugin.ir

import com.rdude.exECS.plugin.ir.debug.DebugVisitor
import com.rdude.exECS.plugin.ir.lowering.GeneratedComponentIdPropertyAdder
import com.rdude.exECS.plugin.ir.lowering.GeneratedComponentInsideEntitiesPropertyAdder
import com.rdude.exECS.plugin.ir.lowering.GeneratedPoolAdder
import com.rdude.exECS.plugin.ir.lowering.GeneratedTypeIdCompanionAndAccessFunctionAdder
import com.rdude.exECS.plugin.ir.transform.EntityCallsToComponentMapperCallsTransformer
import com.rdude.exECS.plugin.ir.transform.FromPoolCallsTransformer
import com.rdude.exECS.plugin.ir.transform.QueueEventTransformer
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.reference.*
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import com.rdude.exECS.plugin.ir.visit.ClassesFinder
import com.rdude.exECS.plugin.ir.visit.CompanionsFinder
import com.rdude.exECS.plugin.ir.visit.PoolsMapper
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class ExEcsIrPluginExtension() : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        MetaData.context = pluginContext

        val callsFinder = CallsFinder()

        val componentFqName = "com.rdude.exECS.component.Component"
        val systemFqName = "com.rdude.exECS.system.System"
        val eventFqName = "com.rdude.exECS.event.Event"
        val poolableFqName = "com.rdude.exECS.pool.Poolable"
        val singletonEntityFqName = "com.rdude.exECS.entity.SingletonEntity"


        // classes plugin is interested in
        val classes: MutableMap<String, MutableList<IrClass>> = HashMap()

        // find subtypes of System, Component, Event, Poolable and Singleton Entity
        val classesFinder = ClassesFinder(
            listOf(
                componentFqName,
                systemFqName,
                eventFqName,
                poolableFqName,
                singletonEntityFqName
            )
        )
        moduleFragment.accept(classesFinder, classes)

        // find existing companion objects of classes that plugin is interested in
        val companionsFinder = CompanionsFinder()
        val existingCompanions = companionsFinder.find(classes.values.flatten())

        // generate pools
        val pools = PoolsMapper()
        val generatedPoolAdder = GeneratedPoolAdder(existingCompanions, pools)
        if (classes[poolableFqName]?.isNotEmpty() == true) {
            generatedPoolAdder.addTo(classes[poolableFqName]!!)
        }

        // find and transform calls to Entity methods inside system subclasses
        if (classes[systemFqName]?.isNotEmpty() == true) {

            // methods to transform
            val transformMethods = listOf(
                EntityWrapper.getComponentFun,
                EntityWrapper.hasComponentFun,
                EntityWrapper.removeComponentFun,
                EntityWrapper.addComponentFun,
                EntityWrapper.addPoolableComponentFun,
                SingletonEntity.getComponentFun,
                SingletonEntity.hasComponentFun,
                SingletonEntity.removeComponentFun,
                SingletonEntity.addComponentFun,
                SingletonEntity.addPoolableComponentFun
            )

            // find
            val entityWrapperMethodCallsData = callsFinder.find(
                moduleFragment = moduleFragment,
                representations = transformMethods,
                inClasses = classes[systemFqName]!!
            )

            // transform
            val entityWrapperToComponentMapperCallsTransformer = EntityCallsToComponentMapperCallsTransformer(pools)
            entityWrapperMethodCallsData.forEach { entityWrapperToComponentMapperCallsTransformer.transform(it) }
        }

        // find and transform calls to SingletonEntity methods inside SingletonEntity subclasses
        if (classes[singletonEntityFqName]?.isNotEmpty() == true) {

            // methods to transform
            val singletonEntitiesTransformMethods = listOf(
                SingletonEntity.getComponentFun,
                SingletonEntity.hasComponentFun,
                SingletonEntity.removeComponentFun,
                SingletonEntity.addComponentFun,
                SingletonEntity.addPoolableComponentFun
            )

            // find
            val singletonEntitiesCallsData = callsFinder.find(
                moduleFragment = moduleFragment,
                representations = singletonEntitiesTransformMethods,
                inClasses = classes[singletonEntityFqName]!!
            )

            // transform
            val entityToComponentMapperCallsTransformer = EntityCallsToComponentMapperCallsTransformer(pools)
            singletonEntitiesCallsData.forEach { entityToComponentMapperCallsTransformer.transform(it) }
        }

        // find and transform fromPool() calls
        val fromPoolCalls = callsFinder.find(moduleFragment, listOf(ExEcsExternal.fromPoolFun))
        if (fromPoolCalls.isNotEmpty()) {
            val fromPoolCallsTransformer = FromPoolCallsTransformer(pools)
            fromPoolCalls.forEach { fromPoolCallsTransformer.transform(it) }
        }


        val generatedTypeIdAdder = GeneratedTypeIdCompanionAndAccessFunctionAdder(existingCompanions)
        // add companions that holds type ids to events
        if (classes[eventFqName]?.isNotEmpty() == true) {
            generatedTypeIdAdder.addTo(classes[eventFqName]!!, Event)
        }
        // add companions that holds type ids to components
        if (classes[componentFqName]?.isNotEmpty() == true) {
            generatedTypeIdAdder.addTo(classes[componentFqName]!!, Component)
        }

        // add generated id to components and id factory to component's companion
        val generatedComponentIdPropertyAdder = GeneratedComponentIdPropertyAdder(existingCompanions)
        if (classes[componentFqName]?.isNotEmpty() == true) {
            generatedComponentIdPropertyAdder.addTo(classes[componentFqName]!!)
        }

        // override insideEntities property in components
        val generatedComponentInsideEntitiesPropertyAdder = GeneratedComponentInsideEntitiesPropertyAdder()
        if (classes[componentFqName]?.isNotEmpty() == true) {
            generatedComponentInsideEntitiesPropertyAdder.addTo(classes[componentFqName]!!)
        }

        // transform queueEvent
        val queueEventCalls =
            callsFinder.find(moduleFragment, listOf(System.queuePoolableEventFun, World.queuePoolableEventFun))
        if (queueEventCalls.isNotEmpty()) {
            val queueEventTransformer = QueueEventTransformer(pools)
            queueEventCalls.forEach { queueEventTransformer.transform(it) }
        }


        // debug
        moduleFragment.accept(DebugVisitor(), null)
    }
}