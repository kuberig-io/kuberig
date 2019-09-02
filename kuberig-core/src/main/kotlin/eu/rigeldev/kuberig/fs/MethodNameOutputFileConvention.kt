package eu.rigeldev.kuberig.fs

import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod
import java.io.File

class MethodNameOutputFileConvention : OutputFileConvention {

    override fun outputFile(outputDirectory: File, resourceGeneratorMethod: ResourceGeneratorMethod, yaml: String): File {
        return File(outputDirectory, "${resourceGeneratorMethod.methodName}.yaml")
    }
}