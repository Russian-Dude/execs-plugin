package com.rdude.exECS.plugin

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

object TestMessenger {

    private var counter = 0
    lateinit var messageCollector: MessageCollector

    fun printMessage(message: String) {
        messageCollector.report(CompilerMessageSeverity.STRONG_WARNING, message)
    }

}

fun debugMessage(message: String) = TestMessenger.printMessage(message)