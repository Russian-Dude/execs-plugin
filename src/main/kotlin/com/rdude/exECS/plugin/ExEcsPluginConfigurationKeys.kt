package com.rdude.exECS.plugin

import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ExEcsPluginConfigurationKeys {

    val OUTPUT_DIR_KEY: CompilerConfigurationKey<String> = CompilerConfigurationKey.create<String>("Output directory")

}