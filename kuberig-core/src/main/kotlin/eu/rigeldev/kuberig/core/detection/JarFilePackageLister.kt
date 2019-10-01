package eu.rigeldev.kuberig.core.detection

import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

class JarFilePackageLister {
    
    fun listJarPackages(jarFile: File, listener: JarFilePackageListener) {
        val jarFileAbsolutePath = jarFile.absolutePath

        check(jarFile.exists()) { "jarFile doesn't exist! ($jarFileAbsolutePath)" }
        check(jarFile.isFile) { "jarFile is not a file! ($jarFileAbsolutePath)" }
        check(jarFile.extension == "jar") { "jarFile is not a jarFile! ($jarFileAbsolutePath)" }

        try {
            val zipFile = ZipFile(jarFile)
            try {
                val zipFileEntries = zipFile.entries()

                while(zipFileEntries.hasMoreElements()) {
                    val zipFileEntry = zipFileEntries.nextElement()

                    if (zipFileEntry.isDirectory) {
                        val zipFileEntryName = zipFileEntry.name

                        if (!zipFileEntryName.startsWith("META-INF")) {
                            listener.receivePackage(zipFileEntryName)
                        }
                    }
                }
            }
            finally {
                zipFile.close()
            }
        }
        catch (e : IOException) {
            throw IllegalStateException("failed to scan jar file for packages ($jarFileAbsolutePath)", e)
        }
    }
    
}