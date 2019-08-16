package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.config.KubeRigEnvironment

class ResourceTaskConfigurationAction<A:AbstractResourceTask>(environment: KubeRigEnvironment, val kuberigVersion: String) : EnvironmentTaskConfigurationAction<A>(environment) {

    override fun execute(task: A) {
        super.execute(task)

        task.dependsOn("jar")
        task.kuberigVersion = this.kuberigVersion
    }
}