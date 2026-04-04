package io.ritely

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.util.system.CpuArch
import com.intellij.util.system.OS
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.absolutePathString

private fun VirtualFile.isRiteFile() =
    (extension == "yaml" || extension == "yml") && nameWithoutExtension.endsWith(".rite")

class RiteLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        if (file.isRiteFile()) {
            serverStarter.ensureServerStarted(RiteLspServerDescriptor(project))
        }
    }
}

class RiteLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Rite") {
    override fun isSupportedFile(file: VirtualFile) = file.isRiteFile()

    override fun createCommandLine() = GeneralCommandLine().apply {
        val binaryPath = getBinaryPath() ?: throw RuntimeException("Unsupported OS or architecture for Rite LSP")
        val executable = extractBinary(binaryPath)
        withExePath(executable.absolutePathString())
    }

    private fun getBinaryPath(): String? {
        val platform = when (OS.CURRENT) {
            OS.Linux if CpuArch.CURRENT == CpuArch.ARM64 -> "linux-arm64"
            OS.Linux if CpuArch.CURRENT == CpuArch.X86_64 -> "linux-x86_64"
            OS.macOS if CpuArch.CURRENT == CpuArch.ARM64 -> "darwin-arm64"
            OS.macOS if CpuArch.CURRENT == CpuArch.X86_64 && CpuArch.isEmulated() -> "darwin-arm64"
            OS.macOS if CpuArch.CURRENT == CpuArch.X86_64 -> "darwin-x86_64"
            else -> return null
        }

        return "/binaries/$platform/rite-ls"
    }

    private fun extractBinary(resourcePath: String): Path {
        val tempDir = PathManager.getSystemDir().resolve("io.ritely.intellij")
        Files.createDirectories(tempDir)

        val targetPath = tempDir.resolve("rite-ls")

        // TODO improve the logic to detect if file has changed or not
        if (!Files.exists(targetPath) || Files.size(targetPath) == 0L) {
            LOG.info("Rite LSP: extracting binary from resource: $resourcePath")
            val input = javaClass.getResourceAsStream(resourcePath)
                ?: throw RuntimeException("Binary resource not found: $resourcePath")
            input.use { Files.copy(it, targetPath, StandardCopyOption.REPLACE_EXISTING) }

            if (OS.CURRENT != OS.Windows) {
                try {
                    val permissions = Files.getPosixFilePermissions(targetPath).toMutableSet()
                    permissions.add(PosixFilePermission.OWNER_EXECUTE)
                    Files.setPosixFilePermissions(targetPath, permissions)
                } catch (_: UnsupportedOperationException) {
                    // Ignore if POSIX permissions are not supported
                }
            }
        }

        return targetPath
    }
}
