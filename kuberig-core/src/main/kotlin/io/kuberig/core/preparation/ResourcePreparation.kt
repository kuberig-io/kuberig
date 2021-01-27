package io.kuberig.core.preparation

import io.kuberig.config.KubeRigFlags
import io.kuberig.core.deployment.ApiServerIntegration
import io.kuberig.core.model.SuccessResult
import io.kuberig.core.resource.RawJsonResourceFactory
import io.kuberig.core.resource.RawJsonResourceInfo
import io.kuberig.fs.EnvironmentFileSystem

class ResourcePreparation(
    environmentFileSystem: EnvironmentFileSystem,
    flags: KubeRigFlags,
    methodResults: List<SuccessResult>
) {

    private val apiResourceInfoSource: APIResourceInfoSource
    private val rawResourceFactory: RawJsonResourceFactory

    init {
        val initialResourceInfos = mutableListOf<InitialResourceInfo>()

        for (methodResult in methodResults) {
            for (resourceApplyRequest in methodResult.resourceApplyRequests) {
                initialResourceInfos.add(resourceApplyRequest.initialResourceInfo)
            }
        }

        this.apiResourceInfoSource = DelegatingAPIResourceInfoSource(
            listOf(
                CRDDefinitionAPIResourceInfoSource(initialResourceInfos.toList()),
                APIServerAPIResourceInfoSource(
                    ApiServerIntegration(
                        environmentFileSystem.certificateAuthorityData(),
                        environmentFileSystem.readAuthDetails(),
                        flags
                    ),
                    environmentFileSystem.apiServerUrl()
                )
            )
        )

        this.rawResourceFactory = RawJsonResourceFactory(
            flags,
            environmentFileSystem,
            ResourceUrlInfoFactory(
                environmentFileSystem.apiServerUrl(),
                apiResourceInfoSource
            )
        )
    }

    fun prepare(initialResourceInfo: InitialResourceInfo): RawJsonResourceInfo {
        return this.rawResourceFactory.rawResourceInfo(
            initialResourceInfo.initialJson,
            initialResourceInfo.sourceLocation,
            initialResourceInfo.targetNamespace
        )
    }
}