package com.rdude.exECS.plugin.ir

import com.rdude.exECS.plugin.ir.lowering.GeneratedInsideEntitiesPropertyAdder
import com.rdude.exECS.plugin.ir.lowering.GeneratedPoolAdder
import com.rdude.exECS.plugin.ir.lowering.GeneratedRichComponentEntityIdPropertyAdder
import com.rdude.exECS.plugin.ir.lowering.GeneratedTypeIdCompanionAndAccessFunctionAdder
import com.rdude.exECS.plugin.ir.transform.*
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.utils.reference.*
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import com.rdude.exECS.plugin.ir.visit.ClassesFinder
import com.rdude.exECS.plugin.ir.visit.CompanionsFinder
import com.rdude.exECS.plugin.ir.visit.PoolsMapper
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class ExEcsIrPluginExtension() : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        MetaData.context = pluginContext

        val callsFinder = CallsFinder()
        val propertyTransformer = PropertyTransformer()

        val componentFqName = "com.rdude.exECS.component.Component"
        val richComponentFqName = "com.rdude.exECS.component.RichComponent"
        val systemFqName = "com.rdude.exECS.system.System"
        val eventFqName = "com.rdude.exECS.event.Event"
        val poolableFqName = "com.rdude.exECS.pool.Poolable"
        val poolableComponentFqName = "com.rdude.exECS.component.PoolableComponent"
        val singletonEntityFqName = "com.rdude.exECS.entity.SingletonEntity"


        // classes plugin is interested in
        val classes: MutableMap<String, MutableList<IrClass>> = HashMap()

        // find subtypes of System, Component, Event, Poolable, PoolableComponent and Singleton Entity
        val classesFinder = ClassesFinder(
            listOf(
                componentFqName,
                richComponentFqName,
                systemFqName,
                eventFqName,
                poolableFqName,
                poolableComponentFqName,
                singletonEntityFqName
            )
        )
        moduleFragment.accept(classesFinder, classes)

        // find existing companion objects of classes that plugin is interested in
        val companionsFinder = CompanionsFinder()
        val existingCompanions = companionsFinder.find(classes.values.flatten())

        // add insideEntities property if needed to PoolableComponents
        if (classes[poolableComponentFqName]?.isNotEmpty() == true || classes[componentFqName]?.isNotEmpty() == true) {

            val insideEntitiesPropertyAdder = GeneratedInsideEntitiesPropertyAdder(propertyTransformer)

            if (classes[poolableFqName]?.isNotEmpty() == true) {
                classes[poolableFqName]!!.filter { it.modality != Modality.ABSTRACT }.forEach {
                    insideEntitiesPropertyAdder.addInsideEntitiesPropertyIfNeeded(it)
                }
            }

            // make components that implements Poolable but not PoolableComponent, implement PoolableComponent and add property
            if (classes[componentFqName]?.isNotEmpty() == true) {
                val toPoolableComponent = PoolableToPoolableComponentTransformer()
                classes[componentFqName]!!.filter { it.modality != Modality.ABSTRACT }.forEach {
                    val transformed = toPoolableComponent.transformIfNeeded(it)
                    if (transformed) insideEntitiesPropertyAdder.addInsideEntitiesPropertyIfNeeded(it)
                }
            }
        }

        // override entityId property of rich components if needed
        if (classes[richComponentFqName]?.isNotEmpty() == true) {
            val entityIdPropertyTransformer = GeneratedRichComponentEntityIdPropertyAdder(propertyTransformer)
            classes[richComponentFqName]!!.filter { it.modality != Modality.ABSTRACT }.forEach { entityIdPropertyTransformer.addOrTransformEntityIdPropertyIfNeeded(it) }
        }

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

        // transform queueEvent
        val queueEventCalls =
            callsFinder.find(moduleFragment, listOf(System.queuePoolableEventFun, World.queuePoolableEventFun))
        if (queueEventCalls.isNotEmpty()) {
            val queueEventTransformer = QueueEventTransformer(pools)
            queueEventCalls.forEach { queueEventTransformer.transform(it) }
        }


        // debug
        //moduleFragment.accept(DebugVisitor(), null)
    }
}