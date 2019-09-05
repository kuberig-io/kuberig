package eu.rigeldev.kuberig.fs

import eu.rigeldev.kuberig.support.PropertiesSupport
import java.io.File

class ContainerVersionsFile(private val parentDirectory: File) {
    val containerVersionsFile: File = File(parentDirectory, "container.versions")

    fun init() {
        check(parentDirectory.isDirectory) { "${parentDirectory.absolutePath} is not a directory!" }
    }

    fun addOrUpdateContainerVersion(containerAlias: String, containerVersion: String) {
        this.lateInit()

        PropertiesSupport.addOrChangeConfig(
            this.containerVersionsFile,
            containerAlias,
            containerVersion
        )
    }

    fun readContainerVersion(containerAlias: String): String? {
        this.lateInit()

        return PropertiesSupport.readConfig(this.containerVersionsFile, containerAlias)
    }

    fun removeContainerVersion(containerAlias: String) {
        this.lateInit()

        PropertiesSupport.removeConfig(this.containerVersionsFile, containerAlias)
    }

    private fun lateInit() {
        if (!this.containerVersionsFile.exists()) {
            this.init()
        }
    }
}