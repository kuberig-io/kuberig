package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input

abstract class AbstractEnvironmentTask: DefaultTask() {

    @Input
    lateinit var environment: KubeRigEnvironment

}