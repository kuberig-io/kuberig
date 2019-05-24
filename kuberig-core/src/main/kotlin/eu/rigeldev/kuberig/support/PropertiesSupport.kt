package eu.rigeldev.kuberig.support

import java.io.File

object PropertiesSupport {

    fun changeConfig(propertiesFile: File, key: String, changeAction: (value:String) -> String): Boolean {

        val currentLines = propertiesFile.readLines()
        val newLines = mutableListOf<String>()

        var updated = false

        for (currentLine in currentLines) {
            if (currentLine.startsWith(key)) {
                val splitIndex = currentLine.indexOf('=')
                val currentValue = currentLine.substring(splitIndex + 1)
                val newValue = changeAction(currentValue)

                newLines.add("$key=$newValue")

                updated = true
            } else {
                newLines.add(currentLine)
            }
        }

        propertiesFile.writeText(newLines.joinToString("\n"))

        return updated
    }

}