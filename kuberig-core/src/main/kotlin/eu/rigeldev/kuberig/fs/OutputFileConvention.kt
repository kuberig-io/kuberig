package eu.rigeldev.kuberig.fs

import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod
import java.io.File

interface OutputFileConvention {

    fun outputFile(outputDirectory: File, resourceGeneratorMethod: ResourceGeneratorMethod, yaml: String): File

}