package io.kuberig.core.detection

import io.kuberig.support.JarFileSupport
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

class ClassFileExtractionManager {
    private val logger = LoggerFactory.getLogger(ClassFileExtractionManager::class.java)
    
    private val packageJarFilesMappings = mutableMapOf<String, MutableSet<File>>()
    private val extractedJarClasses = mutableMapOf<String, File>()
    private val unExtractableClasses = mutableSetOf<String>()
    
    fun addLibraryJar(libraryJar: File) {
        JarFilePackageLister().listJarPackages(libraryJar, object : JarFilePackageListener {
            override fun receivePackage(packageName: String) {
                val jarFiles = packageJarFilesMappings.getOrDefault(packageName, mutableSetOf())
                jarFiles.add(libraryJar)
                
                packageJarFilesMappings[packageName] = jarFiles
            }
        }) 
    }
    
    fun getLibraryClassFile(className: String): File? {
        return if (unExtractableClasses.contains(className)) {
            null
        } else {
            if (!extractedJarClasses.containsKey(className)) {
                if (!extractClassFile(className)) {
                    unExtractableClasses.add(className)
                }
            }

            extractedJarClasses[className]
        }
    }
    
    private fun extractClassFile(className: String) : Boolean {
        var classFileExtracted = false
        
        val extractedClassFile = tempFile()
        val classFileName = "$className.class"
        val classNamePackage = classNamePackage(className)
        val packageJarFiles = packageJarFilesMappings[classNamePackage]
        
        var classFileSourceJar : File? = null
        
        if (packageJarFiles != null && packageJarFiles.isNotEmpty()) {
            val packageJarFilesIt = packageJarFiles.iterator()
            
            while (!classFileExtracted && packageJarFilesIt.hasNext()) {
                val jarFile = packageJarFilesIt.next()
                
                try {
                    classFileExtracted = JarFileSupport.extractZipEntry(jarFile, classFileName, extractedClassFile)
                    
                    if (classFileExtracted) {
                        classFileSourceJar = jarFile
                    }
                } catch (e : IOException) {
                    throw IllegalStateException("Failed to extract class file from jar ($jarFile)", e)
                }
            }
            
            if (classFileExtracted) {
                logger.debug("extracted class {} from {}", className, classFileSourceJar?.name)
                
                extractedJarClasses[className] = extractedClassFile
            }
        } // super class not on the classpath - unable to scan parent class
        
        return classFileExtracted
    }
    
    private fun classNamePackage(className: String) : String {
        val lastSlashIndex = className.lastIndexOf('/')
        
        check(lastSlashIndex == -1) { "$className is in root package - should not happen"}
        
        return className.substring(0, lastSlashIndex + 1)
    }
    
    private fun tempFile() : File {
        return File.createTempFile("jar_extract_", "_tmp")
    }
}