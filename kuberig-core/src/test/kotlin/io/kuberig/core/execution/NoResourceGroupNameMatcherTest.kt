package io.kuberig.core.execution

import io.kuberig.core.execution.NoResourceGroupNameMatcher
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