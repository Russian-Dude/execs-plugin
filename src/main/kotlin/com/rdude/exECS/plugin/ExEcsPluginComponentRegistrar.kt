package com.rdude.exECS.plugin

import com.rdude.exECS.plugin.ir.ExEcsIrPluginExtension
import com.rdude.exECS.plugin.synthetic.ExEcsSyntheticResolveExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension


class ExEcsPluginComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        TestMessenger.messageCollector = messageCollector

        SyntheticResolveExtension.registerExtension(project, ExEcsSyntheticResolveExtension())
        IrGenerationExtension.registerExtension(project, ExEcsIrPluginExtension())
    }
}