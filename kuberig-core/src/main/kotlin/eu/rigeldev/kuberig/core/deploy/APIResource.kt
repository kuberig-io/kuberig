package eu.rigeldev.kuberig.core.deploy

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class APIResource (
    val name : String,
    val singularBind: String?,
    val namespaced: Boolean,
    val kind : String
)