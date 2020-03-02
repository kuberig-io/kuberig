package eu.rigeldev.kuberig.core.execution

import com.nhaarman.mockitokotlin2.mock
import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.annotations.EnvFilter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EnvResourceGeneratorFilterTest {

    private val devEnvFilter = EnvResourceGeneratorFilter(KubeRigEnvironment("dev"))
    private val testEnvFilter = EnvResourceGeneratorFilter(KubeRigEnvironment("test"))
    private val prodEnvFilter = EnvResourceGeneratorFilter(KubeRigEnvironment("prod"))

    @Test
    fun environmentMatches() {
        val testMethod = this.javaClass.getDeclaredMethod("testMethod")

        assertTrue(devEnvFilter.shouldGenerate(testMethod))
        assertTrue(testEnvFilter.shouldGenerate(testMethod))
        assertFalse(prodEnvFilter.shouldGenerate(testMethod))

        val testMethodNoEnv = this.javaClass.getDeclaredMethod("testMethodNoEnv")

        assertTrue(devEnvFilter.shouldGenerate(testMethodNoEnv))
        assertTrue(testEnvFilter.shouldGenerate(testMethodNoEnv))
        assertTrue(prodEnvFilter.shouldGenerate(testMethodNoEnv))
    }

    @EnvFilter(environments = ["dev", "test"])
    fun testMethod() {
        // empty test method, only annotations are important
    }

    fun testMethodNoEnv() {

    }


}