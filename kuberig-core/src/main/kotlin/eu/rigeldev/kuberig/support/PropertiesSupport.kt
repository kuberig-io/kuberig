package eu.rigeldev.kuberig.support

import eu.rigeldev.kuberig.fs.FileSystemOperations
import java.io.File

object PropertiesSupport {

    fun readConfig(propertiesFile: File, key: String): String? {
        FileSystemOperations.isExistingFile(propertiesFile)

        var configValue: String? = null

        val currentLines = propertiesFile.readLines()
        for (currentLine in currentLines) {
            if (currentLine.startsWith(key)) {
                val splitIndex = currentLine.indexOf('=')
                configValue = currentLine.substring(splitIndex + 1).trim()
            }
        }

        return configValue
    }

    fun addOrChangeConfig(propertiesFile: File, key: String, newValue: String) {
        FileSystemOperations.isExistingFile(propertiesFile)

        val currentLines = propertiesFile.readLines()
        val newLines = mutableListOf<String>()

        var changed = false

        for (currentLine in currentLines) {
            if (currentLine.startsWith(key)) {
                newLines.add("$key=$newValue")

                changed = true
            } else {
                newLines.add(currentLine)
            }
        }

        if (!changed) {
            newLines.add("$key=$newValue")
        }

        propertiesFile.writeText(newLines.joinToString("\n"))
    }

    fun changeConfig(propertiesFile: File, key: String, changeAction: (value:String) -> String): Boolean {
        FileSystemOperations.isExistingFile(propertiesFile)

        val currentValue = this.readConfig(propertiesFile, key)

        var changed = false

        if (currentValue != null) {
            val newValue = changeAction(currentValue)

            this.addOrChangeConfig(propertiesFile, key, newValue)

            changed = true
        }

        return changed
    }

    fun removeConfig(propertiesFile: File, key: String): Boolean {
        FileSystemOperations.isExistingFile(propertiesFile)

        val currentLines = propertiesFile.readLines()
        val newLines = mutableListOf<String>()

        var removed = false

        for (currentLine in currentLines) {
            if (currentLine.startsWith(key)) {
                removed = true
            } else {
                newLines.add(currentLine)
            }
        }

        propertiesFile.writeText(newLines.joinToString("\n"))

        return removed
    }



}