package com.rdude.exECS.plugin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class ExEcsCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "execs-plugin"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(OUTPUT_DIR_OPTION)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option) {
            OUTPUT_DIR_OPTION -> configuration.put(ExEcsPluginConfigurationKeys.OUTPUT_DIR_KEY, value)
            else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        }
    }

    companion object {
        val OUTPUT_DIR_OPTION = CliOption("outputDir", "<value>", "")
    }
}

