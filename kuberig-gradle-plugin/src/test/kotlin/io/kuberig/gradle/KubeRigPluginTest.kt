package io.kuberig.gradle

import io.kuberig.gradle.config.KubeRigExtension
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.util.stream.Collectors

internal class KubeRigPluginTest {

    private val defaultAmountOfTasks = 5
    private val amountOfEnvironmentTasks = 10

    private val project = ProjectBuilder.builder().build()

    @BeforeEach
    fun setup() {
        project.plugins.apply("io.kuberig.kuberig")
    }

    @Test
    fun verifyDefaultTasksForPluginAvailable() {
        val kuberigTasks = kuberigTasks()

        assertEquals(defaultAmountOfTasks, kuberigTasks.size)

        verifyDefaultTasksAvailable(kuberigTasks)
    }

    @Test
    fun verifyEnvironmentTasksAreAdded() {
        val environmentNames = listOf("local", "dev", "qa", "prod")

        val extension : KubeRigExtension = project.extensions.getByType(KubeRigExtension::class.java)
        environmentNames.forEach { environmentName -> extension.environments.create(environmentName) }

        val kuberigTasks = kuberigTasks()

        assertEquals(defaultAmountOfTasks + ( environmentNames.size * amountOfEnvironmentTasks) , kuberigTasks.size)

        verifyDefaultTasksAvailable(kuberigTasks)
        environmentNames.forEach { environmentName -> verifyEnvironmentTasksAvailable(kuberigTasks, environmentName) }
    }

    private fun verifyEnvironmentTasksAvailable(kuberigTasks: List<Task>, environmentName: String) {
        val taskNameSuffix = environmentName.capitalize() + "Environment"

        // encryption support tasks
        assertNotNull(kuberigTasks.find { it.name == "createEncryptionKey$taskNameSuffix" })
        assertNotNull(kuberigTasks.find { it.name == "decryptConfig$taskNameSuffix" })
        assertNotNull(kuberigTasks.find { it.name == "decryptFile$taskNameSuffix" })
        assertNotNull(kuberigTasks.find { it.name == "decrypt$taskNameSuffix" })
        assertNotNull(kuberigTasks.find { it.name == "encryptConfig$taskNameSuffix" })
        assertNotNull(kuberigTasks.find { it.name == "encryptFile$taskNameSuffix" })
        assertNotNull(kuberigTasks.find { it.name == "encrypt$taskNameSuffix" })
        assertNotNull(kuberigTasks.find { it.name == "showConfig$taskNameSuffix" })

        // deploy task
        assertNotNull(kuberigTasks.find { it.name == "deploy$taskNameSuffix" })

        // generate yaml task
        assertNotNull(kuberigTasks.find { it.name == "generateYaml$taskNameSuffix" })
    }

    private fun verifyDefaultTasksAvailable(kuberigTasks: List<Task>) {
        // container version management tasks
        assertNotNull(kuberigTasks.find { it.name == "clearContainerVersion" })
        assertNotNull(kuberigTasks.find { it.name == "getContainerVersion" })
        assertNotNull(kuberigTasks.find { it.name == "setContainerVersion" })

        // init tasks
        assertNotNull(kuberigTasks.find { it.name == "initEnvironment" })
        assertNotNull(kuberigTasks.find { it.name == "initGitIgnore" })
    }

    private fun kuberigTasks() = project.tasks
        .filter { it.group == "kuberig" }
}