package eu.rigeldev.kuberig.gradle

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.gradle.tasks.AbstractEnvironmentTask
import eu.rigeldev.kuberig.gradle.tasks.EnvironmentTaskConfigurationAction
import org.gradle.api.Action
import org.gradle.api.Task
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