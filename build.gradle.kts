plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intellijPlatform)
}

group = "io.ritely"
version = "0.1.1-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion"),
        )

        bundledPlugin("org.jetbrains.plugins.yaml")

        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }

        changeNotes = """
            <h3>0.1.0</h3>
            <p>Initial release.</p>
            <ul>
              <li>Language support for <code>*.rite.yaml</code> files via the bundled <code>rite-ls</code> language server.</li>
              <li>Diagnostics, hover, and completions.</li>
              <li>Bundled binaries for macOS (Apple Silicon and Intel), Linux (x64 and ARM64), and Windows (x64).</li>
              <li>Settings page for configuring a custom <code>rite-ls</code> binary path.</li>
            </ul>
        """.trimIndent()
    }

    pluginVerification {
        ides {
            current()
        }
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }
}
