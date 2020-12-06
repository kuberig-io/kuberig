package io.kuberig.core.detection

data class ResourceGeneratorMethod(val generatorType : String, val methodName : String) {
    fun fullMethod(): String {
        return "$generatorType#$methodName"
    }
}