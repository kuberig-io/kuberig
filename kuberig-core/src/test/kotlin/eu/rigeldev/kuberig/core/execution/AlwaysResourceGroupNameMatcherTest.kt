package eu.rigeldev.kuberig.core.execution

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class AlwaysResourceGroupNameMatcherTest {

    private val matcher = AlwaysResourceGroupNameMatcher()

    @Test
    fun matchResourceGroupName() {
        assertTrue(matcher.matchResourceGroupName(null))
        assertTrue(matcher.matchResourceGroupName(""))
        assertTrue(matcher.matchResourceGroupName("some-group-name"))
    }
}