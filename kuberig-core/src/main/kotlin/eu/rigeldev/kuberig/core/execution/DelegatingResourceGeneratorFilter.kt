package eu.rigeldev.kuberig.core.execution

import java.lang.reflect.Method

/**
 * All filters need to agree before shouldGenerate will return true.
 */
open class DelegatingResourceGeneratorFilter(private val delegates: List<ResourceGeneratorFilter>): ResourceGeneratorFilter {

    override fun shouldGenerate(method: Method): Boolean {
        var generateNeeded = true

        val delegatesIterator = delegates.iterator()
        while (generateNeeded && delegatesIterator.hasNext()) {
            generateNeeded = delegatesIterator.next().shouldGenerate(method)
        }

        return generateNeeded
    }
}