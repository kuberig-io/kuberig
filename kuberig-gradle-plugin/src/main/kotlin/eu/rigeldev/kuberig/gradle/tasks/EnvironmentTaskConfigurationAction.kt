package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import org.gradle.api.Action

open class EnvironmentTaskConfigurationAction<A:AbstractEnvironmentTask>(val environment: KubeRigEnvironment) : Action<A> {

    override fun execute(task: A) {
        task.environment = this.environment
    }
}