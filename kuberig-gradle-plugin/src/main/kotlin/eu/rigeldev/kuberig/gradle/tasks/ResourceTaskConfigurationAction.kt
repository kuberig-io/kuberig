package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.config.KubeRigEnvironment

class ResourceTaskConfigurationAction<A:AbstractResourceTask>(environment: KubeRigEnvironment, val kuberigVersion: String) : EnvironmentTaskConfigurationAction<A>(environment) {

    override fun execute(resourceTask: A) {
        super.execute(resourceTask)

        resourceTask.dependsOn("jar")
        resourceTask.kuberigVersion = this.kuberigVersion
    }
}