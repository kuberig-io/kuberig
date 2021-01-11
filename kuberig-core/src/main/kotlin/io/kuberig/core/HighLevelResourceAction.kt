package io.kuberig.core

import io.kuberig.config.KubeRigEnvironment
import io.kuberig.config.KubeRigFlags
import io.kuberig.core.execution.filtering.group.AlwaysResourceGroupNameMatcher
import io.kuberig.core.execution.filtering.group.NoResourceGroupNameMatcher
import io.kuberig.core.execution.filtering.group.RequestedResourceGroupNameMatcher
import io.kuberig.core.execution.filtering.group.ResourceGroupNameMatcher
import io.kuberig.fs.RootFileSystem
import java.io.File

abstract class HighLevelResourceAction<out T : HighLevelResourceAction<T>> {

    protected lateinit var groupNameMatcher: ResourceGroupNameMatcher
    protected lateinit var flags: KubeRigFlags
    protected lateinit var rootFileSystem: RootFileSystem
    protected lateinit var environment: KubeRigEnvironment
    protected lateinit var compileOutputDirectory: File
    protected lateinit var resourceGenerationCompileClasspath: Set<File>
    protected lateinit var resourceGenerationRuntimeClasspath: ClassLoader

    @Suppress("UNCHECKED_CAST")
    fun groupNameMatcher(groupName: String, allGroups: Boolean): T {
        this.groupNameMatcher = when {
            allGroups -> {
                AlwaysResourceGroupNameMatcher()
            }
            groupName != "" -> {
                RequestedResourceGroupNameMatcher(groupName)
            }
            else -> {
                NoResourceGroupNameMatcher()
            }
        }

        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun flags(flags: KubeRigFlags): T {
        this.flags = flags

        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun rootFileSystem(rootFileSystem: RootFileSystem): T {
        this.rootFileSystem = rootFileSystem

        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun environment(environment: KubeRigEnvironment): T {
        this.environment = environment

        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun compileOutputDirectory(compileOutputDirectory: File): T {
        this.compileOutputDirectory = compileOutputDirectory

        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun resourceGenerationCompileClasspath(resourceGenerationCompileClasspath: Set<File>): T {
        this.resourceGenerationCompileClasspath = resourceGenerationCompileClasspath

        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun resourceGenerationRuntimeClasspath(resourceGenerationRuntimeClasspath: ClassLoader): T {
        this.resourceGenerationRuntimeClasspath = resourceGenerationRuntimeClasspath

        return this as T
    }

    abstract fun execute()
}