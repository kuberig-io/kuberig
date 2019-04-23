package eu.rigeldev.kuberig.core.generation

import org.objectweb.asm.ClassReader
import java.io.File

class ResourceGenerationDetector {

    val resourceGeneratorTypes = mutableListOf<ResourceGeneratorType>()

    fun detectResourceMethods(fileOrDirectory : File) {
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
        val classVisitor = ResourceGenerationClassVisitor()

        val classReader = ClassReader(classFile.readBytes())
        classReader.accept(classVisitor, ClassReader.SKIP_DEBUG or ClassReader.SKIP_CODE or ClassReader.SKIP_FRAMES)

        if (classVisitor.resourceMethods.isNotEmpty()) {
            resourceGeneratorTypes.add(
                ResourceGeneratorType(
                    classVisitor.className,
                    classVisitor.resourceMethods
                )
            )
        }
    }

}