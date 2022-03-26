package com.rdude.exECS.plugin

import com.rdude.exECS.plugin.ir.ExEcsIrPluginExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration


class ExEcsPluginComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val outputDir = ExEcsPluginConfigurationKeys.OUTPUT_DIR_KEY
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        TestMessenger.messageCollector = messageCollector

        //AnalysisHandlerExtension.registerExtension(project, ExEcsPluginExtension(outputDir.toString(), messageCollector))
        IrGenerationExtension.registerExtension(project, ExEcsIrPluginExtension())
    }
}