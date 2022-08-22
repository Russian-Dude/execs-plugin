package com.rdude.exECS.plugin.ir.debug

import com.rdude.exECS.plugin.debugMessage
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.kotlinFqName

class CustomDumper {

    private val customDumperVisitor = CustomDumperVisitor()

    fun dump(element: IrElement) {
        val root = customDumperVisitor.Entry(element)
        customDumperVisitor.root = root
        customDumperVisitor.last = root
        element.accept(customDumperVisitor, root)
        debugMessage("CUSTOM DUMP:\r\n${visit(root)}")
    }

    private fun visit(entry: CustomDumperVisitor.Entry, currentDump: String = "", layer: Int = 0): String {
        val pad = if (layer == 0) "|" else "|-----".repeat(layer)

        // main info, offset and pad
        var updatedDump = "$currentDump\r\n${
            entry.id.toString().padEnd(3)
        }$pad ${entry.mainInfo} [${entry.startOffset}-${entry.endOffset}] "

        // name
        entry.name?.let { updatedDump = "$updatedDump \"${entry.name}\" " }

        // modality
        entry.modality?.let { updatedDump = "$updatedDump ${entry.modality} " }

        // visibility
        entry.visibility?.let { updatedDump = "$updatedDump ${entry.visibility} " }

        // origin
        entry.declarationOrigin?.let { updatedDump = "$updatedDump ${entry.declarationOrigin} " }

        // type
        entry.type?.let {
            val type = entry.type?.classFqName?.asString() ?: entry.type
            updatedDump = "$updatedDump type: $type"
        }

        // supertypes
        entry.superTypes?.let { updatedDump = "$updatedDump superTypes: ${entry.superTypes?.map { it.classFqName?.asString() }} " }

        // parent
        entry.declarationParent?.let {
            updatedDump = if (it == entry.parent?.element) {
                "$updatedDump parent: ${entry.parent?.id} "
            } else {
                val parent = entry.declarationParent?.kotlinFqName?.asString() ?: entry.declarationParent
                "$updatedDump parent: $parent"
            }
        }

        // override
        entry.override?.let {
            if (entry.override!!.isNotEmpty()) {
                updatedDump = "$updatedDump override: ${entry.override!!.map { it.owner }}"
            }
        }

        // type parameters
        entry.typeParameters?.let {
            if (entry.typeParameters!!.isNotEmpty()) {
                updatedDump = "$updatedDump typeParameters: ${entry.typeParameters?.map {
                    val reified = if (it.isReified) "Reified " else ""
                    reified + (it.fqNameWhenAvailable?.asString() ?: it.name.asString())
                }}"
            }
        }

        // value parameters
        entry.valueParameters?.let {
            if (entry.valueParameters!!.isNotEmpty()) {
                updatedDump = "$updatedDump valueParameters: ${entry.valueParameters?.map {
                    "${it.name.asString()}(${it.type.classFqName?.asString()})"
                }}"
            }
        }

        // receivers
        entry.dispatchReceiver?.let { updatedDump = "$updatedDump dispatchReceiver: ${entry.dispatchReceiver?.type?.classFqName?.asString()}" }
        entry.extensionReciver?.let { updatedDump = "$updatedDump extensionReceiver: ${entry.extensionReciver?.type?.classFqName?.asString()}" }


        // return type
        entry.returnType?.let { updatedDump = "$updatedDump returnType: ${entry.returnType?.classFqName?.asString()}" }

        // super qualifier symbol
        entry.superQualifierSymbol?.let { updatedDump = "$updatedDump superQualifierSymbol: ${entry.superQualifierSymbol}" }

        entry.children.forEach {
            updatedDump = visit(it, updatedDump, layer + 1)
        }
        return updatedDump
    }

    //-----Call
    // Call
    //|----- Call
    //|-----|----- Call
    //|-----|----- Call
    //----- Call

}