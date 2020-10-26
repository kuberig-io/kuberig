package io.kuberig.gradle.tasks.encryption

import io.kuberig.gradle.tasks.AbstractEnvironmentTask
import org.gradle.api.tasks.TaskAction

open class GenerateEncryptionKeyTask : AbstractEnvironmentTask() {

    @TaskAction
    fun generateEncryptionKey() {
        val environmentFileSystem = this.environmentFileSystem()

        environmentFileSystem.initEncryption()
    }

}