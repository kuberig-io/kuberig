package io.kuberig.core.preparation

import org.json.JSONObject

/**
 * Scans the initial resources for CRD definitions and builds up a APIResource catalog of the
 *
 * CRD definitions that are going to be deployed/defined by the deployment so we can generate the final JSON
 * before we are going to deploy. This is also anyway needed when the `generateYaml` task is executed.
 */
class CRDDefinitionAPIResourceInfoSource(initialResources: List<InitialResourceInfo>) : APIResourceInfoSource {

    private val versionsContainer: VersionsContainer

    init {
        var buildingVersionsContainer = VersionsContainer()

        initialResources.forEach {
            buildingVersionsContainer = buildingVersionsContainer.add(it)
        }

        this.versionsContainer = buildingVersionsContainer
    }

    override fun apiResource(apiVersion: String, kind: String): APIResourceInfo {
        val result = versionsContainer.get(apiVersion)
            .find { it.kind == kind }

        return result?.let { KnownAPIResourceInfo(it) } ?: UnknownAPIResourceInfo
    }

    class VersionsContainer(private val versions: Map<String, List<APIResource>> = mapOf()) {
        fun get(version: String): List<APIResource> {
            return versions.getOrDefault(version, listOf())
        }

        fun add(initialResource: InitialResourceInfo): VersionsContainer {
            if (initialResource.kind.toLowerCase() != "customerresourcedefinition") {
                return this
            }

            validateCrdDefinition(initialResource)

            val initialJson = initialResource.initialJson

            val crdApiResource = extractAPIResource(initialJson)
            val crdVersions = initialJson.getJSONArray("versions")

            val newVersions = mutableMapOf<String, List<APIResource>>()
            newVersions.putAll(versions)

            crdVersions.forEach {
                val crdVersion = it as JSONObject

                val crdVersionName = crdVersion.getString("name")

                val currentAPIResources = newVersions.getOrDefault(crdVersionName, listOf())

                val newAPIResources = mutableListOf<APIResource>()
                newAPIResources.addAll(currentAPIResources)
                newAPIResources.add(crdApiResource)

                newVersions[crdVersionName] = newAPIResources.toList()
            }

            return VersionsContainer(newVersions.toMap())
        }

        private fun validateCrdDefinition(initialResource: InitialResourceInfo) {
            val initialJson = initialResource.initialJson
            val errorPrefix = "CRD definition from ${initialResource.sourceLocation} is invalid."

            for(requiredAttribute in listOf("spec", "versions", "names", "scope")) {
                check(initialJson.has(requiredAttribute)) { "$errorPrefix It is missing an $requiredAttribute attribute." }
            }

            val names = initialJson.getJSONObject("names")

            for(requiredAttribute in listOf("plural", "singular", "kind")) {
                check(names.has(requiredAttribute)) { "$errorPrefix It is missing an $requiredAttribute attribute in the names section." }
            }

            val crdVersions = initialJson.getJSONArray("versions")

            check(crdVersions.length() >= 1) { "$errorPrefix It should have at least one version defined."}

            crdVersions.withIndex().forEach {
                val crdVersion = crdVersions.getJSONObject(it.index);
                check(crdVersion.has("name")) { "$errorPrefix Version ${it.index + 1} is missing a name attribute." }
            }
        }

        private fun extractAPIResource(initialJson: JSONObject): APIResource {
            val spec = initialJson.getJSONObject("spec")
            val names = spec.getJSONObject("names")

            val scope = if (spec.has("scope")) {
                spec.getString("scope").capitalize()
            } else {
                "Namespaced"
            }

            return APIResource(
                names.getString("plural"),
                names.getString("singular"),
                scope == "Namespaced",
                names.getString("kind")
            )
        }
    }

}

