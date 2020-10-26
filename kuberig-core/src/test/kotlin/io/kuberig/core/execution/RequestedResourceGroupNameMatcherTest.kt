package io.kuberig.core.execution

import io.kuberig.core.execution.RequestedResourceGroupNameMatcher
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class RequestedResourceGroupNameMatcherTest {

    private val requestedGroupName = "some-group-name"

    private val matcher = RequestedResourceGroupNameMatcher(requestedGroupName)

    @Test
    fun matchResourceGroupName() {
        assertFalse(matcher.matchResourceGroupName(null))
        assertFalse(matcher.matchResourceGroupName(""))
        assertTrue(matcher.matchResourceGroupName(requestedGroupName))
    }
}