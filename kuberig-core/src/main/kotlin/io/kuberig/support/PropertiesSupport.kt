package io.kuberig.support

import java.io.File

object PropertiesSupport {

    fun readConfig(propertiesFile: File, key: String): String? {
        val currentLines = readLines(propertiesFile)

        val linesForKey = currentLines.filter { it.startsWith(key) }

        return if (linesForKey.isEmpty()) {
            null
        } else {
            check(linesForKey.size == 1) {
                "Multiple lines for key $key, ${propertiesFile.absolutePath} is no valid properties file!"
            }

            val lineForKey = linesForKey[0]

            val splitIndex = lineForKey.indexOf('=')

            check(splitIndex != -1) {
                "Line for key $key is not valid, it does not contain a '=': $lineForKey"
            }

            return lineForKey.substring(splitIndex + 1).trim()
        }
    }

    fun addOrChangeConfig(propertiesFile: File, key: String, newValue: String) {
        val currentLines = readLines(propertiesFile)
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

        writeLines(propertiesFile, newLines)
    }

    fun changeConfig(propertiesFile: File, key: String, changeAction: (value:String) -> String): Boolean {
        val currentValue = readConfig(propertiesFile, key)

        var changed = false

        if (currentValue != null) {
            val newValue = changeAction(currentValue)

            addOrChangeConfig(propertiesFile, key, newValue)

            changed = true
        }

        return changed
    }

    fun removeConfig(propertiesFile: File, key: String): Boolean {
        val currentLines = readLines(propertiesFile)

        val newLines = mutableListOf<String>()

        var removed = false

        for (currentLine in currentLines) {
            if (currentLine.startsWith(key)) {
                removed = true
            } else {
                newLines.add(currentLine)
            }
        }

        writeLines(propertiesFile, newLines)

        return removed
    }

    /**
     * Read all the lines of a properties file.
     *
     * In case all the file contains is whitespace; the file is treated as empty and an empty list is returned.
     *
     * Context:
     * After clearing all properties 1 empty line will be left in the properties file.
     * When you add a new property afterwards. The new property will be appended after that line. Which is sloppy.
     * Treating the file as empty when it only contains whitespace fixes this.
     */
    private fun readLines(propertiesFile: File): List<String> {
        return if (propertiesFile.exists()) {
            val currentLines = propertiesFile.readLines()

            var allLinesEmpty = true

            val currentLinesIterator = currentLines.iterator()
            while (allLinesEmpty && currentLinesIterator.hasNext()) {
                val currentLine = currentLinesIterator.next()

                allLinesEmpty = currentLine.trim().isEmpty()
            }

            if (allLinesEmpty) {
                listOf()
            } else {
                currentLines
            }
        } else {
            listOf()
        }
    }

    private fun writeLines(propertiesFile: File, lines: MutableList<String>) {
        val noLines = lines.isEmpty()
        val lastLineEmpty =if (noLines) {
            false
        } else {
            lines[lines.lastIndex].trim().isEmpty()
        }

        if (noLines || !lastLineEmpty) {
            lines.add("\n")
        }

        propertiesFile.writeText(lines.joinToString("\n"))
    }

}