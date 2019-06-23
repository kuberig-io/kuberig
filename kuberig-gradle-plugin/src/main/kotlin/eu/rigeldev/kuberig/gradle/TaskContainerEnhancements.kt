package eu.rigeldev.kuberig.gradle

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

fun <A: Task> TaskContainer.registerEnvironmentTask(taskActionName: String, environment: KubeRigEnvironment, taskType: Class<A>, configurationAction: Action<A>) {
    this.register(
        taskName(taskActionName, environment),
        taskType,
        configurationAction
    )
}

private fun taskName(action: String, environment: KubeRigEnvironment): String {
    val capitalizedEnvironmentName = environment.name.capitalize()

    return "$action${capitalizedEnvironmentName}Environment"
}