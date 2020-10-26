package io.kuberig.fs

import io.kuberig.core.detection.ResourceGeneratorMethod
import java.io.File

interface OutputFileConvention {

    fun outputFile(outputDirectory: File, resourceGeneratorMethod: ResourceGeneratorMethod, yaml: String): File

}