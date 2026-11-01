package sct

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.io.File
import javax.inject.Inject

abstract class GitVersion {
    @get:Inject
    abstract val providers: ProviderFactory

    fun versionProvider(rootDir: File): Provider<String> =
        providers.exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
            workingDir(rootDir)
            isIgnoreExitValue = true
        }.standardOutput.asText.map { raw ->
            val tag = raw.trim()
            when {
                tag.isEmpty() -> "2.0.0-dev"
                else -> tag.removePrefix("v").substringBefore("+")
            }
        }
}
