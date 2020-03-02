package eu.rigeldev.kuberig.core.execution

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DelegatingResourceGeneratorFilterTest {

    private val mockFilterOne : ResourceGeneratorFilter = mock()
    private val mockFilterTwo : ResourceGeneratorFilter = mock()

    private val filter = DelegatingResourceGeneratorFilter(listOf(mockFilterOne, mockFilterTwo))

    @Test
    fun shouldGenerateNoYes() {
        val resourceGenerationMethod = this.javaClass.getDeclaredMethod("testMethod")

        whenever(mockFilterOne.shouldGenerate(any())).thenReturn(false)
        whenever(mockFilterTwo.shouldGenerate(any())).thenReturn(true)

        assertFalse(filter.shouldGenerate(resourceGenerationMethod))

        verify(mockFilterOne).shouldGenerate(any())
        verifyZeroInteractions(mockFilterTwo)
    }

    @Test
    fun shouldGenerateYesNo() {
        val resourceGenerationMethod = this.javaClass.getDeclaredMethod("testMethod")

        whenever(mockFilterOne.shouldGenerate(any())).thenReturn(true)
        whenever(mockFilterTwo.shouldGenerate(any())).thenReturn(false)

        assertFalse(filter.shouldGenerate(resourceGenerationMethod))

        verify(mockFilterOne).shouldGenerate(any())
        verify(mockFilterTwo).shouldGenerate(any())
    }

    @Test
    fun shouldGenerateTrueTrue() {
        val resourceGenerationMethod = this.javaClass.getDeclaredMethod("testMethod")

        whenever(mockFilterOne.shouldGenerate(any())).thenReturn(true)
        whenever(mockFilterTwo.shouldGenerate(any())).thenReturn(true)

        assertTrue(filter.shouldGenerate(resourceGenerationMethod))

        verify(mockFilterOne).shouldGenerate(any())
        verify(mockFilterTwo).shouldGenerate(any())
    }

    fun testMethod() {
        // empty test method
    }

}