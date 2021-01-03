package io.kuberig.fs

import java.io.File

interface OutputFileConvention {

    fun outputFile(outputDirectory: File, yaml: String): File

}