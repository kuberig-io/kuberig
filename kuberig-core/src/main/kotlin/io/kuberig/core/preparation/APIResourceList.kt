package io.kuberig.core.preparation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class APIResourceList(
    val kind: String,
    val groupVersion: String,
    val resources: List<APIResource>
)