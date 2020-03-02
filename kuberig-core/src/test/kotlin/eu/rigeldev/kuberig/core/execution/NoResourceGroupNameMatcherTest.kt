package eu.rigeldev.kuberig.core.execution

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class NoResourceGroupNameMatcherTest {

    private val matcher = NoResourceGroupNameMatcher()

    @Test
    fun matchResourceGroupName() {
        assertTrue(matcher.matchResourceGroupName(null))
        assertFalse(matcher.matchResourceGroupName(""))
        assertFalse(matcher.matchResourceGroupName("some-group-name"))
    }
}