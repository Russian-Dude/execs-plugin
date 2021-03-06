package com.rdude.exECS.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption


class ExEcsGradlePlugin : Plugin<Project>, KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {  }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        return project.provider {
            listOf(SubpluginOption("outputDir", project.buildDir.absolutePath + "/generated/ktPlugin"))
        }
    }

    override fun getCompilerPluginId(): String = "execs-plugin"

/*    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(groupId = "com.russian-dude", artifactId = "com.russian-dude.execs-plugin", version = "local-test")*/

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "com.github.Russian-Dude.execs-plugin",
            artifactId = "exECS_plugin",
            version = "1.4.4-1.0.2")

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}