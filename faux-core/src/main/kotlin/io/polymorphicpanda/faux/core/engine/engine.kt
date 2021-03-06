package io.polymorphicpanda.faux.core.engine

import io.polymorphicpanda.faux.core.service.ServiceManager
import io.polymorphicpanda.faux.core.util.DynamicGraph
import io.polymorphicpanda.faux.core.window.WindowEventHandler
import io.polymorphicpanda.faux.event.Event
import io.polymorphicpanda.faux.runtime.EnginePeer
import io.polymorphicpanda.faux.service.Service
import io.polymorphicpanda.faux.system.System
import io.polymorphicpanda.faux.system.SystemContext
import io.polymorphicpanda.faux.system.SystemDescriptor
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging
import kotlin.reflect.KClass


class Engine(private val globalContext: GlobalContextImpl,
             private val executionModel: EngineExecutionModel,
             private val serviceManager: ServiceManager): EnginePeer, WindowEventHandler {
    private val logger = KotlinLogging.logger {}

    private val backend = executionModel.graphics
    private val sharedPool = executionModel.sharedPool
    private val mainThread = executionModel.mainThread
    private val systemExecutor = executionModel.systemExecutor

    private val systemInstanceMap = mutableMapOf<SystemDescriptor<*>, Pair<System, SystemContext>>()
    private val executionGraph by lazy { buildExecutionGraph() }

    suspend fun update(duration: Double) {
        // TODO: execute state

        launch(sharedPool) {
            systemExecutor.execute(coroutineContext, duration, executionGraph.clone())
        }.join()
    }

    override fun handleInput(event: Event) {
        // TODO: pass event to state
    }

    override fun handleWindowResize(width: Int, height: Int) {
        backend.handleWindowResize(width, height)
    }

    override fun handleFrameBufferResize(width: Int, height: Int) {
        backend.handleFrameBufferResize(width, height)
    }

    override fun <T: Service> getService(service: KClass<T>): T {
        return serviceManager.getService(service)
    }

    override fun getSharedPool() = sharedPool

    override fun getMainThread() = mainThread

    private fun buildExecutionGraph(): DynamicGraph<Pair<System, SystemContext>> {
        return DynamicGraph<Pair<System, SystemContext>>().apply {
            executionModel.systems.forEach { descriptor, dependencies ->
                val mapping = systemInstanceFor(descriptor)
                if (dependencies.isEmpty()) {
                    addNode(systemInstanceFor(descriptor) )
                } else {
                    dependencies.forEach {
                        addEdge(mapping, systemInstanceFor(it))
                    }
                }
            }

            // this should happen last
            // add renderer that depends on everything
            val renderSystem = systemInstanceFor(backend.getRenderer())
            executionModel.systems.keys.forEach {
                addEdge(renderSystem, systemInstanceFor(it))
            }

        }

    }

    private fun systemInstanceFor(descriptor: SystemDescriptor<*>): Pair<System, SystemContext> {
        return systemInstanceMap.computeIfAbsent(descriptor) {
            descriptor.create() to globalContext.contextFor(descriptor)
        }
    }
}
