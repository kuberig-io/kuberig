package eu.rigeldev.kuberig.support

import java.io.File
import java.util.*

object PropertiesLoaderSupport {
    fun loadProperties(propertiesFile: File) : Properties {
        val properties = Properties()

        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use {
                properties.load(it)
            }
        }

        return properties
    }
}