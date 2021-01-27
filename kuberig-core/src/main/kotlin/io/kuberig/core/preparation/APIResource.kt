package io.kuberig.core.preparation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class APIResource (
    val name : String,
    val singularBind: String?,
    val namespaced: Boolean,
    val kind : String
)