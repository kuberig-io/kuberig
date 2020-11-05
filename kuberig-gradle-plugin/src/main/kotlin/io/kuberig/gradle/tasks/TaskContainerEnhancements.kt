package io.kuberig.gradle.tasks

import io.kuberig.config.KubeRigEnvironment
import org.gradle.api.tasks.TaskContainer

fun <A: AbstractEnvironmentTask> TaskContainer.registerEnvironmentTask(taskActionName: String, environment: KubeRigEnvironment, taskType: Class<A>) {
    this.register(
        taskName(taskActionName, environment),
        taskType,
        EnvironmentTaskConfigurationAction<A>(environment)
    )
}

private fun taskName(action: String, environment: KubeRigEnvironment): String {
    val capitalizedEnvironmentName = environment.name.capitalize()

    return "$action${capitalizedEnvironmentName}Environment"
}