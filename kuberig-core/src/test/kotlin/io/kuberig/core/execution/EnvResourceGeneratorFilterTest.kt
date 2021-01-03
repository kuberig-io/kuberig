package io.kuberig.core.execution

import io.kuberig.config.KubeRigEnvironment
import io.kuberig.annotations.EnvFilter
import io.kuberig.core.execution.filtering.environment.EnvResourceGeneratorFilter
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