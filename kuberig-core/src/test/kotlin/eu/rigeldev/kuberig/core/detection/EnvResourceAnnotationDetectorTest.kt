package eu.rigeldev.kuberig.core.detection

import org.junit.jupiter.api.Test
import java.io.File

internal class EnvResourceAnnotationDetectorTest {

    @Test
    fun scanClassesDirectories() {

        val listener = object : EnvResourceAnnotationDetectionListener {
            val annotatedTypes = mutableListOf<AnnotatedType>()

            override fun receiveEnvResourceAnnotatedType(className: String, annotatedMethods: Set<String>) {
                annotatedTypes.add(AnnotatedType(className, annotatedMethods))
            }
        }

        EnvResourceAnnotationDetector(
            listOf(File("/home/teyckmans/work/ktrack/ktrack-environment/build/classes/kotlin/main")),
            emptySet(),
            listener
        ).scanClassesDirectories()

        for (annotatedType in listener.annotatedTypes) {
            annotatedType.annotatedMethods.forEach {
                println("[DETECTED] ${annotatedType.className}.$it()")
            }
        }

    }
}

data class AnnotatedType(val className: String, val annotatedMethods: Set<String>)