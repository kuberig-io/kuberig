package eu.rigeldev.kuberig.core.execution

import com.nhaarman.mockitokotlin2.*
import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.annotations.EnvResource
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

internal class GroupResourceGenerationFilterTest {

    private val resourceGroupNameMatcher : ResourceGroupNameMatcher = mock()

    private val filter = GroupResourceGenerationFilter(resourceGroupNameMatcher)

    @Test
    fun shouldGenerateFalse() {
        val testMethod = this.javaClass.getDeclaredMethod("testMethod")

        whenever(resourceGroupNameMatcher.matchResourceGroupName(any())).thenReturn(false)

        assertFalse(filter.shouldGenerate(testMethod))

        verify(resourceGroupNameMatcher).matchResourceGroupName(any())
    }

    @Test
    fun shouldGenerateTrue() {
        val testMethod = this.javaClass.getDeclaredMethod("testMethod")

        whenever(resourceGroupNameMatcher.matchResourceGroupName(any())).thenReturn(true)

        assertTrue(filter.shouldGenerate(testMethod))

        verify(resourceGroupNameMatcher).matchResourceGroupName(any())
    }

    @Test
    fun shouldGenerateNoGroup() {
        val testMethodNoGroup = this.javaClass.getDeclaredMethod("testMethodNoGroup")

        whenever(resourceGroupNameMatcher.matchResourceGroupName(null)).thenReturn(true)

        assertTrue(filter.shouldGenerate(testMethodNoGroup))

        verify(resourceGroupNameMatcher).matchResourceGroupName(null)
    }

    @EnvResource(group = "group-name")
    fun testMethod() {
        // empty test method, only annotations are important
    }

    @EnvResource
    fun testMethodNoGroup() {
        // empty test method, only annotations are important
    }
}