package com.rdude.exECS.plugin.ir

import com.rdude.exECS.plugin.describer.*
import com.rdude.exECS.plugin.ir.check.MainCorrectnessChecker
import com.rdude.exECS.plugin.ir.debug.DebugVisitor
import com.rdude.exECS.plugin.ir.lowering.*
import com.rdude.exECS.plugin.ir.transform.*
import com.rdude.exECS.plugin.ir.utils.MetaData
import com.rdude.exECS.plugin.ir.visit.CallsFinder
import com.rdude.exECS.plugin.ir.visit.ClassesFinder
import com.rdude.exECS.plugin.ir.visit.CompanionsFinder
import com.rdude.exECS.plugin.ir.visit.PoolsMapper
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.types.IrType

class ExEcsIrPluginExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        MetaData.context = pluginContext

        val callsFinder = CallsFinder()
        val propertyTransformer = PropertyTransformer()

        // classes plugin is interested in
        val classes: MutableMap<ClassDescriber, MutableList<IrClass>> = HashMap()

        // find subtypes of exECS classes
        val classesFinder = ClassesFinder(
            listOf(
                Component,
                UniqueComponent,
                RichComponent,
                ObservableComponent,
                System,
                Event,
                Poolable,
                PoolableComponent,
                SingletonEntity,
                WorldAccessor
            )
        )
        moduleFragment.accept(classesFinder, classes)

        // Check correctness
        MainCorrectnessChecker().checkAndThrowIfNotCorrect(classes)

        // find existing companion objects of classes that plugin is interested in
        val companionsFinder = CompanionsFinder()
        val existingCompanions = companionsFinder.find(classes.values.flatten())
        val existingTypeIdProperties = mutableMapOf<IrType, MutableMap<ClassDescriber, TypeIdProperty>>()

        // add insideEntities property if needed to PoolableComponents
        if (classes[PoolableComponent]?.isNotEmpty() == true || classes[Component]?.isNotEmpty() == true) {

            val insideEntitiesPropertyAdder = GeneratedInsideEntitiesPropertyAdder()

            if (classes[PoolableComponent]?.isNotEmpty() == true) {
                classes[PoolableComponent]!!.filter { it.modality != Modality.ABSTRACT }.forEach {
                    insideEntitiesPropertyAdder.addInsideEntitiesPropertyIfNeeded(it)
                }
            }

            // make components that implements Poolable but not PoolableComponent, implement PoolableComponent and add property
            if (classes[Component]?.isNotEmpty() == true) {
                val toPoolableComponent = PoolableToPoolableComponentTransformer()
                classes[Component]!!.filter { it.modality != Modality.ABSTRACT }.forEach {
                    val transformed = toPoolableComponent.transformIfNeeded(it)
                    if (transformed) insideEntitiesPropertyAdder.addInsideEntitiesPropertyIfNeeded(it)
                }
            }
        }

        // override entityId property of unique components if needed
        if (classes[UniqueComponent]?.isNotEmpty() == true) {
            val entityIdPropertyTransformer = GeneratedUniqueComponentEntityIdPropertyAdder()
            classes[UniqueComponent]!!.filter { it.modality != Modality.ABSTRACT }.forEach { entityIdPropertyTransformer.addOrTransformEntityIdPropertyIfNeeded(it) }
        }

        // override entitiesIds property of rich components if needed
        if (classes[RichComponent]?.isNotEmpty() == true) {
            val entitiesIdsPropertyTransformer = GeneratedRichComponentEntitiesIdsPropertyAdder()
            classes[RichComponent]!!.filter { it.modality != Modality.ABSTRACT }.forEach { entitiesIdsPropertyTransformer.addOrTransformEntityIdsPropertyIfNeeded(it) }
        }

        // override world property of observable components if needed
        if (classes[ObservableComponent]?.isNotEmpty() == true) {
            val worldPropertyTransformer = GeneratedObservableComponentWorldPropertyAdder()
            classes[ObservableComponent]!!.filter { it.modality != Modality.ABSTRACT }.forEach { worldPropertyTransformer.addOrTransformWorldPropertyIfNeeded(it) }
        }

        // override pool property of poolables if needed
        if (classes[Poolable]?.isNotEmpty() == true) {
            val poolPropertyTransformer = GeneratedPoolPropertyAdder()
            classes[Poolable]!!.filter { it.modality != Modality.ABSTRACT }.forEach { poolPropertyTransformer.addOrTransformPoolPropertyIfNeeded(it) }
        }

        // override isInPool property of poolables if needed
        if (classes[Poolable]?.isNotEmpty() == true) {
            val isInPoolPropertyTransformer = GeneratedIsInPoolPropertyAdder()
            classes[Poolable]!!.filter { it.modality != Modality.ABSTRACT }.forEach { isInPoolPropertyTransformer.addOrTransformIsInPoolPropertyIfNeeded(it) }
        }

        // generate pools
        val pools = PoolsMapper()
        val generatedDefaultPoolAdder = GeneratedDefaultPoolAdder(existingCompanions, pools)
        if (classes[Poolable]?.isNotEmpty() == true) {
            generatedDefaultPoolAdder.addTo(classes[Poolable]!!)
        }

        // find and transform calls to Entity methods inside system subclasses
        if (classes[WorldAccessor]?.isNotEmpty() == true) {

            // methods to transform
            val transformMethods = listOf(
                WorldAccessor.Entity.getComponentFun,
                WorldAccessor.Entity.hasComponentFun,
                WorldAccessor.Entity.removeComponentFun,
                WorldAccessor.Entity.addComponentFun,
                WorldAccessor.Entity.addPoolableComponentFun,
                WorldAccessor.getSingletonFun,
                WorldAccessor.getSystemFun
                //SingletonEntity.getComponentFun,
                //SingletonEntity.hasComponentFun,
                //SingletonEntity.removeComponentFun,
                //SingletonEntity.addComponentFun,
                //SingletonEntity.addPoolableComponentFun
            )

            // find
            val callsData = callsFinder.find(
                moduleFragment = moduleFragment,
                representations = transformMethods,
                inClasses = classes[WorldAccessor]!!
            )

            // transform
            val worldAccessorCallsTransformer = WorldAccessorCallsTransformer(pools)
            callsData.forEach { worldAccessorCallsTransformer.transform(it) }
        }

        // find and transform fromPool() calls
        val fromPoolCalls = callsFinder.find(moduleFragment, listOf(ExEcsExternal.fromPoolFun))
        if (fromPoolCalls.isNotEmpty()) {
            val fromPoolCallsTransformer = FromPoolCallsTransformer(pools)
            fromPoolCalls.forEach { fromPoolCallsTransformer.transform(it) }
        }


        val generatedTypeIdAdder = GeneratedTypeIdCompanionAndAccessFunctionAdder()
        // add companions that holds type ids to events
        if (classes[Event]?.isNotEmpty() == true) {
            generatedTypeIdAdder.addTo(classes[Event]!!, Event)
        }
        // add companions that holds type ids to components
        if (classes[Component]?.isNotEmpty() == true) {
            generatedTypeIdAdder.addTo(classes[Component]!!, Component)
        }

        // transform queueEvent
        val queueEventCalls =
            callsFinder.find(moduleFragment, listOf(
                WorldAccessor.queuePoolableEventFun,
                WorldAccessor.queuePoolableEventWithApplyFun,
                WorldAccessor.queuePoolableEventWithPriorityFun,
                WorldAccessor.queuePoolableEventWithPriorityAndApplyFun,
                World.queuePoolableEventFun,
                World.queuePoolableEventWithApplyFun,
                World.queuePoolableEventWithPriorityFun,
                World.queuePoolableEventWithPriorityAndApplyFun
            ))
        if (queueEventCalls.isNotEmpty()) {
            val queueEventTransformer = QueueEventTransformer(pools)
            queueEventCalls.forEach { queueEventTransformer.transform(it) }
        }

        // singletonEntity methods transformer
        val singletonEntityMethodsTransformer = SingletonEntityMethodsTransformer(existingTypeIdProperties, pools)
        callsFinder.find(
            moduleFragment,
            listOf(
                SingletonEntity.getComponentFun,
                SingletonEntity.hasComponentFun,
                SingletonEntity.removeComponentFun,
                SingletonEntity.addPoolableComponentFun
            )
        ).forEach {
            singletonEntityMethodsTransformer.transform(it)
        }

        // debug
        moduleFragment.accept(DebugVisitor(), null)
    }
}