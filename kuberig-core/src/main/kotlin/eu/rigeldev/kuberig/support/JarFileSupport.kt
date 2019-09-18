package eu.rigeldev.kuberig.support

import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.util.zip.ZipInputStream

object JarFileSupport {
    fun extractZipEntry(jarFile: File, entryName: String, extractToFile: File): Boolean {
        var entryExtracted = false

        FileInputStream(jarFile).use { fileInputStream ->
            ZipInputStream(fileInputStream).use { zipStream -> 
                var zipStreamEndReached = false
                
                while (!entryExtracted && !zipStreamEndReached) {
                    val candidateZipEntry = zipStream.nextEntry
                    
                    if (candidateZipEntry == null) {
                        zipStreamEndReached = true
                    } else {
                        if (candidateZipEntry.name == entryName) {
                            
                            Files.newOutputStream(extractToFile.toPath()).use { extractTargetStream ->
                                zipStream.copyTo(extractTargetStream)
                            }
                            
                            entryExtracted = true
                        }
                    }
                }
            }
        }

        return entryExtracted
    }
}