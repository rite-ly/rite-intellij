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
import java.nio.file.attribute.PosixFilePermission
import java.security.MessageDigest
import java.util.*
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
        when (OS.CURRENT) {
            OS.Linux if CpuArch.CURRENT == CpuArch.ARM64 ->
                return "/bin/linux-arm64/rite-ls"
            OS.Linux if CpuArch.CURRENT == CpuArch.X86_64 ->
                return "/bin/linux-x86_64/rite-ls"
            OS.macOS if CpuArch.CURRENT == CpuArch.ARM64 ->
                return "/bin/darwin-arm64/rite-ls"
            OS.macOS if CpuArch.CURRENT == CpuArch.X86_64 && CpuArch.isEmulated() ->
                return "/bin/darwin-arm64/rite-ls"
            OS.macOS if CpuArch.CURRENT == CpuArch.X86_64 ->
                return "/bin/darwin-x86_64/rite-ls"
            OS.Windows if CpuArch.CURRENT == CpuArch.X86_64 ->
                return "/bin/windows-x86_64/rite-ls.exe"
            else ->
                return null
        }
    }

    private fun extractBinary(resourcePath: String): Path {
        val tempDir = PathManager.getSystemDir().resolve("io.ritely.rite")
        Files.createDirectories(tempDir)

        val targetPath = tempDir.resolve(resourcePath.substringAfterLast('/'))

        val resourceBytes = javaClass.getResourceAsStream(resourcePath)
            ?.use { it.readBytes() }
            ?: throw RuntimeException("Binary resource not found: $resourcePath")

        val upToDate = Files.exists(targetPath) &&
                sha256(resourceBytes) == sha256(Files.readAllBytes(targetPath))

        if (!upToDate) {
            LOG.info("Rite LSP: extracting binary from resource: $resourcePath")
            Files.write(targetPath, resourceBytes)

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

    private fun sha256(bytes: ByteArray): String =
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))
}
