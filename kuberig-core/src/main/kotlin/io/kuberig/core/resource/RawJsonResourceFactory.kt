package io.kuberig.core.resource

import io.kuberig.config.KubeRigFlags
import io.kuberig.core.deployment.ApiServerIntegration
import io.kuberig.core.preparation.*
import io.kuberig.dsl.support.*
import io.kuberig.fs.EnvironmentFileSystem
import org.json.JSONObject

/**
 * Will generate the resource JSON value and extract needed information for processing.
 *
 * Will retrieve APIResource information from the API server in order to determine the resource being generated is a
 * namespace resource or not. The APIResource information is also needed in order to generate the resource URL.
 *
 * TargetNamespace resolution rule is handled here in order to determine the actual NamespaceValue which is needed in
 * order to generate the target YAML file in the `generateYaml` task and prepare as much up front as possible before deploying.
 */
class RawJsonResourceFactory(
    flags: KubeRigFlags,
    environmentFileSystem: EnvironmentFileSystem,
    private val resourceUrlInfoFactory: ResourceUrlInfoFactory
) {

    private val apiServerUrl: String = environmentFileSystem.apiServerUrl()
    private val defaultNamespace: String = environmentFileSystem.defaultNamespace()

    private val apiServerIntegration: ApiServerIntegration = ApiServerIntegration(
        environmentFileSystem.certificateAuthorityData(),
        environmentFileSystem.readAuthDetails(),
        flags
    )

    fun rawResourceInfo(
        resourceJson: JSONObject,
        sourceLocation: String,
        targetNamespace: TargetNamespace
    ): RawJsonResourceInfo {
        val apiVersion = resourceJson.getString("apiVersion").toLowerCase()
        val kind = resourceJson.getString("kind")
        val metadataJson: JSONObject = resourceJson.getJSONObject("metadata")
        val resourceName = metadataJson.getString("name")

        val apiResources = APIServerAPIResourceInfoSource(
            apiServerIntegration,
            apiServerUrl
        )

        when (val apiResourceLookup = apiResources.apiResource(apiVersion, kind)) {
            is UnknownAPIResourceInfo -> {
                throw IllegalStateException("Encountered unknown kind $kind for version $apiVersion, unable to proceed.")
            }
            is KnownAPIResourceInfo -> {
                val apiResource = apiResourceLookup.apiResource

                val resourceUrlInfo: ResourceUrlInfo
                val postProcessedResourceJson: JSONObject
                val namespaceValue : NamespaceValue

                if (apiResource.namespaced) {
                    val namespace = resolveTargetNamespace(
                        targetNamespace,
                        resourceJson,
                        resourceName,
                        kind
                    )

                    resourceUrlInfo = resourceUrlInfoFactory.namespacedResourceUrl(apiVersion, kind, resourceName, namespace)
                    postProcessedResourceJson = updateNamespace(resourceJson, namespace)
                    namespaceValue = Namespaced(namespace)
                } else {
                    resourceUrlInfo = resourceUrlInfoFactory.resourceUrl(apiVersion, kind, resourceName)
                    postProcessedResourceJson = removeNamespace(resourceJson)
                    namespaceValue = Clustered
                }

                return RawJsonResourceInfo(
                    apiVersion,
                    kind,
                    resourceName,
                    namespaceValue,
                    postProcessedResourceJson,
                    sourceLocation,
                    resourceUrlInfo
                )
            }
        }
    }

    private fun resolveTargetNamespace(
        targetNamespace: TargetNamespace,
        json: JSONObject,
        resourceName: String,
        kind: String
    ): String {
        val metadataJson: JSONObject = json.getJSONObject("metadata")

        return when (targetNamespace) {
            is UseSourceOrEnvironmentDefault -> {
                if (metadataJson.has("namespace")) {
                    metadataJson.getString("namespace")
                } else {
                    metadataJson.put("namespace", this.defaultNamespace)
                    this.defaultNamespace
                }
            }
            is UseSource -> {
                check(metadataJson.has("namespace")) {
                    "Resource $resourceName of type $kind does not define a namespace. Add a namespace is required by useSourceNamespace(). "
                }
                metadataJson.getString("namespace")
            }
            is UseEnvironmentDefault -> {
                defaultNamespace
            }
            is UseSpecific -> {
                targetNamespace.namespace
            }
        }
    }

    private fun updateNamespace(initialJson: JSONObject, namespace: String): JSONObject {
        removeNamespace(initialJson)
        val metadataJson: JSONObject = initialJson.getJSONObject("metadata")
        metadataJson.put("namespace", namespace)
        return initialJson
    }

    private fun removeNamespace(initialJson: JSONObject): JSONObject {
        val metadataJson: JSONObject = initialJson.getJSONObject("metadata")
        if (metadataJson.has("namespace")) {
            metadataJson.remove("namespace")
        }
        return initialJson
    }
}