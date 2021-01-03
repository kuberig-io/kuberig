package io.kuberig.core.model

class GeneratorType(val typeName: String, val generatorMethods: List<GeneratorMethod>) {

    fun fullMethod(generatorMethod: GeneratorMethod): String {
        check(generatorMethods.contains(generatorMethod)) { "$typeName has no generator method $generatorMethod"}

        return "$typeName#${generatorMethod.methodName}"
    }
}