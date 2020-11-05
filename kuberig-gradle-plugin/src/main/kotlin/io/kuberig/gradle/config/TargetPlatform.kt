package io.kuberig.gradle.config

data class TargetPlatform(
        val platform: PlatformType,
        val platformVersion: String
)