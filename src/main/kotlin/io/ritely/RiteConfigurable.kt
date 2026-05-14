package io.ritely

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import java.nio.file.Files
import java.nio.file.Path

class RiteConfigurable : BoundConfigurable("Rite") {
    private lateinit var pathField: TextFieldWithBrowseButton

    override fun createPanel(): DialogPanel {
        val state = RiteSettings.getInstance().state
        return panel {
            row("rite-ls path:") {
                pathField = textFieldWithBrowseButton(
                    FileChooserDescriptorFactory
                        .createSingleFileNoJarsDescriptor()
                        .withTitle("Select rite-ls Binary"),
                )
                    .bindText(state::serverPath)
                    .align(AlignX.FILL)
                    .component
            }
            row {
                comment("Absolute path to a custom rite-ls binary. Leave empty to use the bundled one.")
            }
        }
    }

    override fun apply() {
        val trimmed = pathField.text.trim()
        if (trimmed.isNotBlank()) {
            val path = Path.of(trimmed)
            val message = when {
                !Files.exists(path) -> "No file exists at this path"
                !Files.isRegularFile(path) -> "Path is not a file"
                !Files.isExecutable(path) -> "File is not executable (check permissions)"
                else -> null
            }
            if (message != null) throw ConfigurationException(message)
        }
        if (pathField.text != trimmed) {
            pathField.text = trimmed
        }
        super.apply()
        ProjectManager.getInstance().openProjects.forEach { project ->
            LspServerManager.getInstance(project)
                .stopAndRestartIfNeeded(RiteLspServerSupportProvider::class.java)
        }
    }
}
