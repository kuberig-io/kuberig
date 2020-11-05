package io.kuberig.fs

import io.kuberig.encryption.tink.TinkEncryptionSupportFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ContainerVersionsLogicTest {

    @TempDir
    lateinit var tempDir: File

    @BeforeEach
    fun setUp() {

    }

    @Test
    fun noVersionForContainerAliasAvailable() {
        val rootFileSystem = RootFileSystem(tempDir, tempDir, TinkEncryptionSupportFactory())

        rootFileSystem.containerVersionsFile.containerVersionsFile.writeText("nginx=1.2.3")

        assertNull(rootFileSystem.readGlobalContainerVersion("does-not-exist"))

        rootFileSystem.environment("local").init()

        assertNull(rootFileSystem.readEnvironmentContainerVersion("local", "does-not-exist"))
    }

    @Test
    fun globalVersionCheck() {
        val rootFileSystem = RootFileSystem(tempDir, tempDir, TinkEncryptionSupportFactory())

        rootFileSystem.containerVersionsFile.containerVersionsFile.writeText("nginx=1.2.3")

        assertEquals("1.2.3", rootFileSystem.readGlobalContainerVersion("nginx"))
        assertNull(rootFileSystem.readGlobalContainerVersion("mailhog"))

        rootFileSystem.addOrUpdateGlobalContainerVersion("mailhog", "3.2.1")

        assertEquals("1.2.3", rootFileSystem.readGlobalContainerVersion("nginx"))
        assertEquals("3.2.1", rootFileSystem.readGlobalContainerVersion("mailhog"))

        rootFileSystem.addOrUpdateGlobalContainerVersion("nginx", "1.3.0")

        assertEquals("1.3.0", rootFileSystem.readGlobalContainerVersion("nginx"))
        assertEquals("3.2.1", rootFileSystem.readGlobalContainerVersion("mailhog"))

        rootFileSystem.removeGlobalContainerVersion("mailhog")

        assertEquals("1.3.0", rootFileSystem.readGlobalContainerVersion("nginx"))
        assertNull(rootFileSystem.readGlobalContainerVersion("mailhog"))
    }

    @Test
    fun combinedLogicCheck() {
        val rootFileSystem = RootFileSystem(tempDir, tempDir, TinkEncryptionSupportFactory())

        rootFileSystem.environment("local").init()
        rootFileSystem.environment("dev").init()

        // verify environment level takes precedence
        rootFileSystem.addOrUpdateGlobalContainerVersion("nginx", "1.2.3")
        rootFileSystem.addOrUpdateEnvironmentContainerVersion("local", "nginx", "1.3.0")

        assertEquals("1.2.3", rootFileSystem.readContainerVersion("dev", "nginx"))
        assertEquals("1.3.0", rootFileSystem.readContainerVersion("local", "nginx"))

        // in case containerAlias does not exist on global level - environment level is still available
        rootFileSystem.removeGlobalContainerVersion("nginx")

        assertEquals("1.3.0", rootFileSystem.readContainerVersion("local", "nginx"))

        // in case containerAlias is also removed from environment level - no version is available
        rootFileSystem.removeEnvironmentContainerVersion("local", "nginx")

        assertNull(rootFileSystem.readEnvironmentContainerVersion("local", "nginx"))
    }

}