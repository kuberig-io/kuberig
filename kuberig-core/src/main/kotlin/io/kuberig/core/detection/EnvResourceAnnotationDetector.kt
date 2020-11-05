package io.kuberig.core.detection

import org.objectweb.asm.ClassReader
import org.slf4j.LoggerFactory
import java.io.File
import java.util.regex.Pattern

/**
 * Detects methods annotated with @EnvResource or @EnvResources.
 */
class EnvResourceAnnotationDetector(
    private val classesDirectories: List<File>,
    private val classpathFiles: Set<File>,
    private val listener: EnvResourceAnnotationDetectionListener
) {

    private val logger = LoggerFactory.getLogger(EnvResourceAnnotationDetector::class.java)

    private val anonymousClassName = Pattern.compile(".*\\$\\d+")

    private val classFileExtractionManager = ClassFileExtractionManager()
    private val superClasses = mutableMapOf<File, EnvResourceTypeDetectionData>()
    private var classpathPrepared = false
    private val classDirectories = mutableListOf<File>()

    fun getSuperClassFile(superClassName: String): File? {
        check(superClassName.isNotEmpty()) { "superClassName is empty!" }

        if ("java/lang/Object" == superClassName) {
            // java.lang.Object found
            return null
        }

        prepareClasspath()

        var superTestClassFile: File? = null
        val testClassDirectoriesIt = classDirectories.iterator()
        while (superTestClassFile == null && testClassDirectoriesIt.hasNext()) {
            val testClassDirectory = testClassDirectoriesIt.next()
            val candidate = File(testClassDirectory, "$superClassName.class")
            if (candidate.exists()) {
                superTestClassFile = candidate
            }
        }

        return superTestClassFile ?: classFileExtractionManager.getLibraryClassFile(superClassName)
    }

    private fun prepareClasspath() {
        if (classpathPrepared) {
            return
        }

        if (classesDirectories.isNotEmpty()) {
            classDirectories.addAll(classesDirectories)
        }
        if (classpathFiles.isNotEmpty()) {
            for (file in classpathFiles) {
                if (file.isDirectory) {
                    classDirectories.add(file)
                } else if (file.exists() && file.extension == "jar") {
                    classFileExtractionManager.addLibraryJar(file)
                }
            }
        }

        classpathPrepared = true
    }
    
    fun scanClassesDirectories() {
        for (classesDirectory in classesDirectories) {
            classesDirectory.walk().iterator().forEach {
                if (it.isFile) {
                    val relativePath = classesDirectory.toPath().relativize(it.toPath()).toString()
                    val className = getClassName(relativePath)
                    val anonymousClass = anonymousClassName.matcher(className).matches()

                    if (it.extension == "class" && !anonymousClass) {
                        if (logger.isDebugEnabled) {
                            logger.debug("Scanning annotations for {}", it.absolutePath)
                        }
                        processClass(it)
                    }
                }
            }
        }
    }

    private fun getClassName(relativePath: String): String {
        return relativePath.replace("\\.class".toRegex(), "").replace('/', '.')
    }

    private fun processClass(classFile: File) {
        processClass(classFile, false)
    }
    
    private fun processClass(classFile: File, superClass: Boolean) : EnvResourceTypeDetectionData {
        var detectionData = readClassFile(classFile)
        
        var isEnvResourceAnnotatedType = false
        
        if (detectionData.resourceMethods.isEmpty()) {
            val superClassName = detectionData.superClassName
            val superClassFile = getSuperClassFile(superClassName)
            
            if (superClassFile != null) {
                val parentDetectionData = processSuperClass(superClassFile)

                isEnvResourceAnnotatedType = parentDetectionData.resourceMethods.isNotEmpty()

                detectionData = EnvResourceTypeDetectionData(
                    detectionData.isAbstract,
                    detectionData.className,
                    detectionData.superClassName,
                    parentDetectionData.resourceMethods
                )
            }
        } else {
            isEnvResourceAnnotatedType = true
        }

        this.publishEnvResourceAnnotatedType(isEnvResourceAnnotatedType, superClass, detectionData)

        return detectionData
    }
    
    private fun publishEnvResourceAnnotatedType(isEnvResourceAnnotatedType: Boolean, superClass: Boolean, detectionData: EnvResourceTypeDetectionData) {
        if (isEnvResourceAnnotatedType && !detectionData.isAbstract && !superClass) {
            listener.receiveEnvResourceAnnotatedType(
                detectionData.className.replace("/", "."),
                detectionData.resourceMethods.toSet()
            )
        }
    }
    
    private fun readClassFile(classFile: File): EnvResourceTypeDetectionData {
        val classVisitor = EnvResourceClassVisitor()

        val classFileBytes = classFile.readBytes()
        val classReader = ClassReader(classFileBytes)
        classReader.accept(classVisitor, ClassReader.SKIP_DEBUG or ClassReader.SKIP_CODE or ClassReader.SKIP_FRAMES)

        return EnvResourceTypeDetectionData(
            classVisitor.isAbstract,
            classVisitor.className,
            classVisitor.superClassName,
            classVisitor.resourceMethods.toList()
        )
    }

    private fun processSuperClass(superClassFile: File): EnvResourceTypeDetectionData {
        val detectionData = superClasses[superClassFile]

        return if (detectionData == null) {
            val freshDetectionData = processClass(superClassFile, true)

            superClasses[superClassFile] = freshDetectionData

            freshDetectionData
        } else {
            detectionData
        }
    }

}