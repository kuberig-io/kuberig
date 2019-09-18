package eu.rigeldev.kuberig.core.detection

import org.objectweb.asm.ClassReader
import java.io.File

class ResourceGeneratorDetector(private val compileOutputDirectory: File, private val classpath: Set<File>) {

    private val resourceGeneratorMethods = mutableListOf<ResourceGeneratorMethod>()

    fun detectResourceGeneratorMethods() : List<ResourceGeneratorMethod> {
        this.detectResourceMethods(compileOutputDirectory)

        return resourceGeneratorMethods
    }

    private fun detectResourceMethods(fileOrDirectory : File) {
        if (fileOrDirectory.isDirectory) {

            val childFiles = fileOrDirectory.listFiles()
            if (childFiles != null) {
                for (childFile in childFiles) {
                    this.detectResourceMethods(childFile)
                }
            }

        } else {
            if (fileOrDirectory.name.endsWith("class")) {
                this.scanClassFile(fileOrDirectory)
            }
        }
    }

    private fun scanClassFile(classFile : File) {
        val classVisitor = EnvResourceClassVisitor()

        val classReader = ClassReader(classFile.readBytes())
        classReader.accept(classVisitor, ClassReader.SKIP_DEBUG or ClassReader.SKIP_CODE or ClassReader.SKIP_FRAMES)

        if (classVisitor.resourceMethods.isNotEmpty()) {
            val generatorType = classVisitor.className.replace(
                '/',
                '.'
            )

            this.resourceGeneratorMethods.addAll(
                classVisitor.resourceMethods.map { ResourceGeneratorMethod(generatorType, it) }
            )
        }
    }

}