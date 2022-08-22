package com.rdude.exECS.plugin.ir.debug

import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class CustomDumperVisitor : IrElementVisitor<Unit, CustomDumperVisitor.Entry?> {

    lateinit var root: Entry
    lateinit var last: Entry

    override fun visitElement(element: IrElement, data: Entry?) {
        val thisEntry = if (element == root.element) root else data?.children?.find { it.element == element }
        thisEntry?.startOffset = element.startOffset
        thisEntry?.endOffset = element.endOffset
        element.acceptChildren(this, thisEntry)
    }

    override fun visitAnonymousInitializer(declaration: IrAnonymousInitializer, data: Entry?) {
        val info = "AnonymousInitializer"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitAnonymousInitializer(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitAnonymousInitializer(declaration, data)
    }

    override fun visitBlock(expression: IrBlock, data: Entry?) {
        val info = "Block"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitBlock(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitBlock(expression, data)
    }

    override fun visitBlockBody(body: IrBlockBody, data: Entry?) {
        val info = "BlockBody"
        if (last?.element == body) {
            last?.mainInfo += " $info"
            super.visitBlockBody(body, data)
            return
        }
        val entry = Entry(body)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitBlockBody(body, data)
    }

    override fun visitBody(body: IrBody, data: Entry?) {
        val info = "Body"
        if (last?.element == body) {
            last?.mainInfo += " $info"
            super.visitBody(body, data)
            return
        }
        val entry = Entry(body)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitBody(body, data)
    }

    override fun visitBranch(branch: IrBranch, data: Entry?) {
        val info = "Branch"
        if (last?.element == branch) {
            last?.mainInfo += " $info"
            super.visitBranch(branch, data)
            return
        }
        val entry = Entry(branch)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitBranch(branch, data)
    }

    override fun visitBreak(jump: IrBreak, data: Entry?) {
        val info = "Break"
        if (last?.element == jump) {
            last?.mainInfo += " $info"
            super.visitBreak(jump, data)
            return
        }
        val entry = Entry(jump)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitBreak(jump, data)
    }

    override fun visitBreakContinue(jump: IrBreakContinue, data: Entry?) {
        val info = "BreakContinue"
        if (last?.element == jump) {
            last?.mainInfo += " $info"
            super.visitBreakContinue(jump, data)
            return
        }
        val entry = Entry(jump)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitBreakContinue(jump, data)
    }

    override fun visitCall(expression: IrCall, data: Entry?) {
        val info = "Call"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitCall(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        entry.dispatchReceiver = expression.dispatchReceiver
        entry.extensionReciver = expression.extensionReceiver
        entry.superQualifierSymbol = expression.superQualifierSymbol
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitCall(expression, data)
    }

    override fun visitCallableReference(expression: IrCallableReference<*>, data: Entry?) {
        val info = "CallableReference"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitCallableReference(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitCallableReference(expression, data)
    }

    override fun visitCatch(aCatch: IrCatch, data: Entry?) {
        val info = "Catch"
        if (last?.element == aCatch) {
            last?.mainInfo += " $info"
            super.visitCatch(aCatch, data)
            return
        }
        val entry = Entry(aCatch)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitCatch(aCatch, data)
    }

    override fun visitClass(declaration: IrClass, data: Entry?) {
        val info = "Class"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            last?.modality = declaration.modality
            last?.visibility = declaration.visibility
            last?.superTypes = declaration.superTypes
            super.visitClass(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        entry.modality = declaration.modality
        entry.visibility = declaration.visibility
        entry.superTypes = declaration.superTypes
        super.visitClass(declaration, data)
    }

    override fun visitClassReference(expression: IrClassReference, data: Entry?) {
        val info = "ClassReference"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitClassReference(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitClassReference(expression, data)
    }

    override fun visitComposite(expression: IrComposite, data: Entry?) {
        val info = "Composite"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitComposite(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitComposite(expression, data)
    }

    override fun <T> visitConst(expression: IrConst<T>, data: Entry?) {
        val info = "Const"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitConst(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitConst(expression, data)
    }

    override fun visitConstructor(declaration: IrConstructor, data: Entry?) {
        val info = "Constructor"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitConstructor(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitConstructor(declaration, data)
    }

    override fun visitConstructorCall(expression: IrConstructorCall, data: Entry?) {
        val info = "ConstructorCall"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitConstructorCall(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitConstructorCall(expression, data)
    }

    override fun visitContainerExpression(expression: IrContainerExpression, data: Entry?) {
        val info = "ContainerExpression"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitContainerExpression(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitContainerExpression(expression, data)
    }

    override fun visitContinue(jump: IrContinue, data: Entry?) {
        val info = "Continue"
        if (last?.element == jump) {
            last?.mainInfo += " $info"
            super.visitContinue(jump, data)
            return
        }
        val entry = Entry(jump)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitContinue(jump, data)
    }

    override fun visitDeclaration(declaration: IrDeclarationBase, data: Entry?) {
        val info = "Declaration"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            last?.declarationOrigin = declaration.origin
            last?.declarationParent = declaration.parent
            (declaration as? IrDeclarationWithName)?.let {
                last?.name = declaration.fqNameWhenAvailable?.asString() ?: declaration.name.asString()
            }
            (declaration as? IrTypeParametersContainer)?.let {
                last?.typeParameters = declaration.typeParameters
            }
            (declaration as? IrOverridableDeclaration<*>)?.let {
                last?.override = declaration.overriddenSymbols
            }
            super.visitDeclaration(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        entry.declarationOrigin = declaration.origin
        entry.declarationParent = declaration.parent
        (declaration as? IrDeclarationWithName)?.let {
            entry.name = declaration.fqNameWhenAvailable?.asString() ?: declaration.name.asString()
        }
        (declaration as? IrTypeParametersContainer)?.let {
            entry?.typeParameters = declaration.typeParameters
        }
        (declaration as? IrOverridableDeclaration<*>)?.let {
            entry?.override = declaration.overriddenSymbols
        }
        super.visitDeclaration(declaration, data)
    }

    override fun visitDeclarationReference(expression: IrDeclarationReference, data: Entry?) {
        val info = "DeclarationReference"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitDeclarationReference(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitDeclarationReference(expression, data)
    }

    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall, data: Entry?) {
        val info = "DelegatingConstructorCall"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitDelegatingConstructorCall(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitDelegatingConstructorCall(expression, data)
    }

    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: Entry?) {
        val info = "DoWhileLoop"
        if (last?.element == loop) {
            last?.mainInfo += " $info"
            super.visitDoWhileLoop(loop, data)
            return
        }
        val entry = Entry(loop)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitDoWhileLoop(loop, data)
    }

    override fun visitDynamicExpression(expression: IrDynamicExpression, data: Entry?) {
        val info = "DynamicExpression"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitDynamicExpression(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitDynamicExpression(expression, data)
    }

    override fun visitDynamicMemberExpression(expression: IrDynamicMemberExpression, data: Entry?) {
        val info = "DynamicMemberExpression"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitDynamicMemberExpression(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitDynamicMemberExpression(expression, data)
    }

    override fun visitDynamicOperatorExpression(expression: IrDynamicOperatorExpression, data: Entry?) {
        val info = "DynamicOperatorExpression"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitDynamicOperatorExpression(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitDynamicOperatorExpression(expression, data)
    }

    override fun visitElseBranch(branch: IrElseBranch, data: Entry?) {
        val info = "ElseBranch"
        if (last?.element == branch) {
            last?.mainInfo += " $info"
            super.visitElseBranch(branch, data)
            return
        }
        val entry = Entry(branch)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitElseBranch(branch, data)
    }

    override fun visitEnumConstructorCall(expression: IrEnumConstructorCall, data: Entry?) {
        val info = "EnumConstructorCall"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitEnumConstructorCall(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitEnumConstructorCall(expression, data)
    }

    override fun visitEnumEntry(declaration: IrEnumEntry, data: Entry?) {
        val info = "EnumEntry"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitEnumEntry(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitEnumEntry(declaration, data)
    }

    override fun visitErrorCallExpression(expression: IrErrorCallExpression, data: Entry?) {
        val info = "ErrorCallExpression"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitErrorCallExpression(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitErrorCallExpression(expression, data)
    }

    override fun visitErrorDeclaration(declaration: IrErrorDeclaration, data: Entry?) {
        val info = "ErrorDeclaration"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitErrorDeclaration(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitErrorDeclaration(declaration, data)
    }

    override fun visitErrorExpression(expression: IrErrorExpression, data: Entry?) {
        val info = "ErrorExpression"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitErrorExpression(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitErrorExpression(expression, data)
    }

    override fun visitExpression(expression: IrExpression, data: Entry?) {
        val info = "Expression"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            last.type = expression.type
            super.visitExpression(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        entry.type = expression.type
        super.visitExpression(expression, data)
    }

    override fun visitExpressionBody(body: IrExpressionBody, data: Entry?) {
        val info = "ExpressionBody"
        if (last?.element == body) {
            last?.mainInfo += " $info"
            super.visitExpressionBody(body, data)
            return
        }
        val entry = Entry(body)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitExpressionBody(body, data)
    }

    override fun visitExternalPackageFragment(declaration: IrExternalPackageFragment, data: Entry?) {
        val info = "ExternalPackageFragment"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitExternalPackageFragment(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitExternalPackageFragment(declaration, data)
    }

    override fun visitField(declaration: IrField, data: Entry?) {
        val info = "Field"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitField(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitField(declaration, data)
    }

    override fun visitFieldAccess(expression: IrFieldAccessExpression, data: Entry?) {
        val info = "FieldAccess"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitFieldAccess(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitFieldAccess(expression, data)
    }

    override fun visitFile(declaration: IrFile, data: Entry?) {
        val info = "File"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitFile(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitFile(declaration, data)
    }

    override fun visitFunction(declaration: IrFunction, data: Entry?) {
        val info = "Function"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            last.valueParameters = declaration.valueParameters
            last.returnType = declaration.returnType
            super.visitFunction(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        entry.valueParameters = declaration.valueParameters
        entry.returnType = declaration.returnType
        super.visitFunction(declaration, data)
    }

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression, data: Entry?) {
        val info = "FunctionAccess"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitFunctionAccess(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitFunctionAccess(expression, data)
    }

    override fun visitFunctionExpression(expression: IrFunctionExpression, data: Entry?) {
        val info = "FunctionExpression"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitFunctionExpression(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitFunctionExpression(expression, data)
    }

    override fun visitFunctionReference(expression: IrFunctionReference, data: Entry?) {
        val info = "FunctionReference"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitFunctionReference(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitFunctionReference(expression, data)
    }

    override fun visitGetClass(expression: IrGetClass, data: Entry?) {
        val info = "GetClass"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitGetClass(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitGetClass(expression, data)
    }

    override fun visitGetEnumValue(expression: IrGetEnumValue, data: Entry?) {
        val info = "GetEnumValue"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitGetEnumValue(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitGetEnumValue(expression, data)
    }

    override fun visitGetField(expression: IrGetField, data: Entry?) {
        val info = "GetField"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitGetField(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitGetField(expression, data)
    }

    override fun visitGetObjectValue(expression: IrGetObjectValue, data: Entry?) {
        val info = "GetObjectValue"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitGetObjectValue(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitGetObjectValue(expression, data)
    }

    override fun visitGetValue(expression: IrGetValue, data: Entry?) {
        val info = "GetValue"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitGetValue(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitGetValue(expression, data)
    }

    override fun visitInstanceInitializerCall(expression: IrInstanceInitializerCall, data: Entry?) {
        val info = "InstanceInitializerCall"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitInstanceInitializerCall(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitInstanceInitializerCall(expression, data)
    }

    override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty, data: Entry?) {
        val info = "LocalDelegatedProperty"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitLocalDelegatedProperty(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitLocalDelegatedProperty(declaration, data)
    }

    override fun visitLocalDelegatedPropertyReference(expression: IrLocalDelegatedPropertyReference, data: Entry?) {
        val info = "LocalDelegatedPropertyReference"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitLocalDelegatedPropertyReference(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitLocalDelegatedPropertyReference(expression, data)
    }

    override fun visitLoop(loop: IrLoop, data: Entry?) {
        val info = "Loop"
        if (last?.element == loop) {
            last?.mainInfo += " $info"
            super.visitLoop(loop, data)
            return
        }
        val entry = Entry(loop)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitLoop(loop, data)
    }

    override fun visitMemberAccess(expression: IrMemberAccessExpression<*>, data: Entry?) {
        val info = "MemberAccess"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitMemberAccess(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitMemberAccess(expression, data)
    }

    override fun visitModuleFragment(declaration: IrModuleFragment, data: Entry?) {
        val info = "ModuleFragment"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitModuleFragment(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitModuleFragment(declaration, data)
    }

    override fun visitPackageFragment(declaration: IrPackageFragment, data: Entry?) {
        val info = "PackageFragment"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitPackageFragment(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitPackageFragment(declaration, data)
    }

    override fun visitProperty(declaration: IrProperty, data: Entry?) {
        val info = "Property"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitProperty(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitProperty(declaration, data)
    }

    override fun visitPropertyReference(expression: IrPropertyReference, data: Entry?) {
        val info = "PropertyReference"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitPropertyReference(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitPropertyReference(expression, data)
    }

    override fun visitRawFunctionReference(expression: IrRawFunctionReference, data: Entry?) {
        val info = "RawFunctionReference"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitRawFunctionReference(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitRawFunctionReference(expression, data)
    }

    override fun visitReturn(expression: IrReturn, data: Entry?) {
        val info = "Return"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitReturn(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitReturn(expression, data)
    }

    override fun visitScript(declaration: IrScript, data: Entry?) {
        val info = "Script"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitScript(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitScript(declaration, data)
    }

    override fun visitSetField(expression: IrSetField, data: Entry?) {
        val info = "SetField"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitSetField(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitSetField(expression, data)
    }

    override fun visitSetValue(expression: IrSetValue, data: Entry?) {
        val info = "SetValue"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitSetValue(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitSetValue(expression, data)
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction, data: Entry?) {
        val info = "SimpleFunction"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitSimpleFunction(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitSimpleFunction(declaration, data)
    }

    override fun visitSingletonReference(expression: IrGetSingletonValue, data: Entry?) {
        val info = "SingletonReference"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitSingletonReference(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitSingletonReference(expression, data)
    }

    override fun visitSpreadElement(spread: IrSpreadElement, data: Entry?) {
        val info = "SpreadElement"
        if (last?.element == spread) {
            last?.mainInfo += " $info"
            super.visitSpreadElement(spread, data)
            return
        }
        val entry = Entry(spread)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitSpreadElement(spread, data)
    }

    override fun visitStringConcatenation(expression: IrStringConcatenation, data: Entry?) {
        val info = "StringConcatenation"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitStringConcatenation(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitStringConcatenation(expression, data)
    }

    override fun visitSuspendableExpression(expression: IrSuspendableExpression, data: Entry?) {
        val info = "SuspendableExpression"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitSuspendableExpression(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitSuspendableExpression(expression, data)
    }

    override fun visitSuspensionPoint(expression: IrSuspensionPoint, data: Entry?) {
        val info = "SuspensionPoint"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitSuspensionPoint(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitSuspensionPoint(expression, data)
    }

    override fun visitSyntheticBody(body: IrSyntheticBody, data: Entry?) {
        val info = "SyntheticBody"
        if (last?.element == body) {
            last?.mainInfo += " $info"
            super.visitSyntheticBody(body, data)
            return
        }
        val entry = Entry(body)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitSyntheticBody(body, data)
    }

    override fun visitThrow(expression: IrThrow, data: Entry?) {
        val info = "Throw"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitThrow(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitThrow(expression, data)
    }

    override fun visitTry(aTry: IrTry, data: Entry?) {
        val info = "Try"
        if (last?.element == aTry) {
            last?.mainInfo += " $info"
            super.visitTry(aTry, data)
            return
        }
        val entry = Entry(aTry)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitTry(aTry, data)
    }

    override fun visitTypeAlias(declaration: IrTypeAlias, data: Entry?) {
        val info = "TypeAlias"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitTypeAlias(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitTypeAlias(declaration, data)
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Entry?) {
        val info = "TypeOperator"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitTypeOperator(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitTypeOperator(expression, data)
    }

    override fun visitTypeParameter(declaration: IrTypeParameter, data: Entry?) {
        val info = "TypeParameter"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitTypeParameter(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitTypeParameter(declaration, data)
    }

    override fun visitValueAccess(expression: IrValueAccessExpression, data: Entry?) {
        val info = "ValueAccess"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitValueAccess(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitValueAccess(expression, data)
    }

    override fun visitValueParameter(declaration: IrValueParameter, data: Entry?) {
        val info = "ValueParameter"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            last?.type = declaration.type
            super.visitValueParameter(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitValueParameter(declaration, data)
    }

    override fun visitVararg(expression: IrVararg, data: Entry?) {
        val info = "Vararg"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitVararg(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitVararg(expression, data)
    }

    override fun visitVariable(declaration: IrVariable, data: Entry?) {
        val info = "Variable"
        if (last?.element == declaration) {
            last?.mainInfo += " $info"
            super.visitVariable(declaration, data)
            return
        }
        val entry = Entry(declaration)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitVariable(declaration, data)
    }

    override fun visitWhen(expression: IrWhen, data: Entry?) {
        val info = "When"
        if (last?.element == expression) {
            last?.mainInfo += " $info"
            super.visitWhen(expression, data)
            return
        }
        val entry = Entry(expression)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitWhen(expression, data)
    }

    override fun visitWhileLoop(loop: IrWhileLoop, data: Entry?) {
        val info = "WhileLoop"
        if (last?.element == loop) {
            last?.mainInfo += " $info"
            super.visitWhileLoop(loop, data)
            return
        }
        val entry = Entry(loop)
        last = entry
        entry.parent = data
        data?.let { it.children += entry }
        entry.mainInfo = info
        super.visitWhileLoop(loop, data)
    }

    inner class Entry(val element: IrElement) {
        val id = currentEntryId++
        var parent: Entry? = null
        var declarationParent: IrDeclarationParent? = null
        var mainInfo: String = ""
        var name: String? = null
        var declarationOrigin: IrDeclarationOrigin? = null
        var startOffset = 0
        var endOffset = 0
        var modality: Modality? = null
        var visibility: DescriptorVisibility? = null
        var type: IrType? = null
        var superTypes: List<IrType>? = null
        var valueParameters: List<IrValueParameter>? = null
        var typeParameters: List<IrTypeParameter>? = null
        var returnType: IrType? = null
        var override: List<IrSymbol>? = null
        val children = mutableListOf<Entry>()
        var dispatchReceiver: IrExpression? = null
        var extensionReciver: IrExpression? = null
        var superQualifierSymbol: IrSymbol? = null
    }

    companion object {
        var currentEntryId = 0
    }
}