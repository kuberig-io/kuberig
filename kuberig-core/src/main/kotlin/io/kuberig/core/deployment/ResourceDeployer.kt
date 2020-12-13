package io.kuberig.core.deployment

import io.kuberig.config.ClientSideApplyFlags
import io.kuberig.config.KubeRigFlags
import io.kuberig.config.ServerSideApplyFlags
import io.kuberig.core.deployment.control.DeployControl
import io.kuberig.core.execution.ResourceGeneratorMethodResult
import io.kuberig.core.execution.SuccessResult
import io.kuberig.fs.EnvironmentFileSystem
import org.json.JSONObject
import org.json.JSONTokener

class ResourceDeployer(
    flags: KubeRigFlags,
    environmentFileSystem: EnvironmentFileSystem,
    deployControl: DeployControl,
    resourceGenerationRuntimeClasspathClassLoader: ClassLoader
) {

    private val apiServerUrl: String = environmentFileSystem.apiServerUrl()
    private val defaultNamespace: String = environmentFileSystem.defaultNamespace()

    private val apiServerIntegration: ApiServerIntegration = ApiServerIntegration(
        environmentFileSystem.certificateAuthorityData(),
        environmentFileSystem.readAuthDetails(),
        flags
    )
    private val applyStrategy: ApplyStrategy<Any> = when (val applyStrategyFlags = flags.applyStrategyFlags) {
        is ServerSideApplyFlags -> {
            ServerSideApplyStrategy(apiServerIntegration, applyStrategyFlags, DeploymentListenerRegistry)
        }
        is ClientSideApplyFlags -> {
            ClientSideApplyStrategy(apiServerIntegration, applyStrategyFlags, DeploymentListenerRegistry)
        }
    }
    private val tickSystemControl = TickSystemControl(
        deployControl,
        resourceGenerationRuntimeClasspathClassLoader,
        DeploymentListenerRegistry
    )
    private val statusTrackingDeploymentListener = StatusTrackingDeploymentListener()

    init {
        DeploymentListenerRegistry.reset()

        DeploymentListenerRegistry.registerListener(ProgressReportingDeploymentListener(applyStrategy))
        DeploymentListenerRegistry.registerListener(statusTrackingDeploymentListener)
    }

    fun deploy(methodResults: List<ResourceGeneratorMethodResult>) {
        val newResourceInfoList = newResourceInfoList(methodResults)

        try {
            tickSystemControl.execute(newResourceInfoList) { newResourceInfo ->
                deployResource(newResourceInfo)
            }
        } finally {
            apiServerIntegration.shutDown()
        }

        if (!statusTrackingDeploymentListener.success) {
            throw DeploymentFailure(statusTrackingDeploymentListener.failureMessage)
        }
    }

    private fun newResourceInfoList(methodResults: List<ResourceGeneratorMethodResult>): List<NewResourceInfo> {
        val successResults = methodResults.filterIsInstance<SuccessResult>()

        val newResourceInfoList = mutableListOf<NewResourceInfo>()
        for (successResult in successResults) {
            for (resource in successResult.resources) {
                newResourceInfoList.add(newResourceInfo(resource, successResult))
            }
        }
        return newResourceInfoList.toList()
    }

    private fun deployResource(newResourceInfo: NewResourceInfo): Boolean {
        val apiResources = ApiResources(
            apiServerIntegration,
            apiServerUrl,
            newResourceInfo.apiVersion
        )
        val resourceUrlInfo = apiResources.resourceUrl(newResourceInfo)

        return applyStrategy.applyResource(newResourceInfo, resourceUrlInfo)
    }

    /**
     * Serialize the resource to JSON and extract some key information from it.
     */
    private fun newResourceInfo(resource: Any, methodResult: SuccessResult): NewResourceInfo {
        val newJson = JSONObject(JSONTokener(apiServerIntegration.jsonSerialize(resource)))

        val apiVersion = newJson.getString("apiVersion").toLowerCase()
        val kind = newJson.getString("kind")
        val metadataJson: JSONObject = newJson.getJSONObject("metadata")
        val resourceName = metadataJson.getString("name")

        val namespace = if (metadataJson.has("namespace")) {
            metadataJson.getString("namespace")
        } else {
            this.defaultNamespace
        }

        if (!metadataJson.has("namespace")) {
            metadataJson.put("namespace", namespace)
        }

        return NewResourceInfo(
            apiVersion,
            kind,
            resourceName,
            namespace,
            newJson,
            methodResult
        )
    }
}

data class NewResourceInfo(
    val apiVersion: String,
    val kind: String,
    val resourceName: String,
    val namespace: String,
    val json: JSONObject,
    val methodResult: SuccessResult
) {
    constructor(source: NewResourceInfo, newJson: JSONObject)
            : this(source.apiVersion, source.kind, source.resourceName, source.namespace, newJson, source.methodResult)

    fun infoText(): String {
        return "$kind - $resourceName in $namespace namespace"
    }

    fun fullInfoText(): String {
        return infoText() + " from resource generator method ${methodResult.method.fullMethod()} "
    }
}

abstract class ApplyStrategy<out F>(
    val name: String,
    val apiServerIntegration: ApiServerIntegration,
    val flags: F,
    val deploymentListener: DeploymentListener
) {

    fun applyResource(newResourceInfo: NewResourceInfo, resourceUrlInfo: ResourceUrlInfo): Boolean {
        deploymentListener.deploymentStart(newResourceInfo, resourceUrlInfo)

        return when (val getResourceResult = apiServerIntegration.getResource(resourceUrlInfo)) {
            is UnknownGetResourceResult -> {
                createResource(newResourceInfo, resourceUrlInfo)
            }
            is ExistsGetResourceResult -> {
                updateResource(newResourceInfo, resourceUrlInfo, getResourceResult)
            }
        }
    }

    protected abstract fun createResource(newResourceInfo: NewResourceInfo, resourceUrlInfo: ResourceUrlInfo): Boolean

    protected abstract fun updateResource(
        newResourceInfo: NewResourceInfo,
        resourceUrlInfo: ResourceUrlInfo,
        getResourceResult: ExistsGetResourceResult
    ): Boolean
}

class DeploymentFailure(message: String) : Exception(message)