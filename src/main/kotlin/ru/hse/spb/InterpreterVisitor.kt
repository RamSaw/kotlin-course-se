package ru.hse.spb

import ru.hse.spb.parser.ExpBaseVisitor
import ru.hse.spb.parser.ExpParser

class InterpreterVisitor(private var scope: Scope) : ExpBaseVisitor<Int?>() {
    override fun visitBlock(ctx: ExpParser.BlockContext?): Int? {
        scope = Scope(scope)
        for (statement in ctx!!.statement()) {
            val returnValue = visitStatement(statement)
            if (returnValue != null) {
                scope = scope.upperScope!!
                return returnValue
            }
        }
        scope = scope.upperScope!!
        return null
    }

    override fun visitFunctionDefinition(ctx: ExpParser.FunctionDefinitionContext?): Int? {
        val functionName = ctx!!.IDENTIFIER().text
        scope.addNewFunction(ctx.IDENTIFIER().text, SourceCodeFunction(ctx))
                ?: throw FunctionMultipleDeclarationException(functionName, ctx.start.line)
        return null
    }

    override fun visitVariableDeclaration(ctx: ExpParser.VariableDeclarationContext?): Int? {
        val variableName = ctx!!.IDENTIFIER().text
        scope.addNewVariable(ctx.IDENTIFIER().text, visit(ctx.expression()))
                ?: throw VariableMultipleDeclarationException(variableName, ctx.start.line)
        return null
    }

    override fun visitWhileStatement(ctx: ExpParser.WhileStatementContext?): Int? {
        while (visit(ctx!!.expression()) != 0) {
            visitBlockWithBraces(ctx.blockWithBraces())?.run { return this }
        }
        return null
    }

    override fun visitBlockWithBraces(ctx: ExpParser.BlockWithBracesContext?): Int? {
        return visitBlock(ctx!!.block())
    }

    override fun visitIfStatement(ctx: ExpParser.IfStatementContext?): Int? {
        if (visit(ctx!!.expression()) != 0) {
            return visitBlockWithBraces(ctx.blockWithBraces(0))
        } else {
            if (ctx.blockWithBraces().size == 2) {
                return visitBlockWithBraces(ctx.blockWithBraces(1))
            }
        }
        return null
    }

    override fun visitExpressionInBraces(ctx: ExpParser.ExpressionInBracesContext?): Int? {
        return visit(ctx!!.expression())
    }

    override fun visitAssignment(ctx: ExpParser.AssignmentContext?): Int? {
        val variableName = ctx!!.IDENTIFIER().text
        scope.setVariableValue(variableName, visit(ctx.expression())!!)
                ?: throw UnknownVariableException(variableName, ctx.start.line)
        return null
    }

    override fun visitReturnStatement(ctx: ExpParser.ReturnStatementContext?): Int {
        return visit(ctx!!.expression())!!
    }

    override fun visitFunctionCall(ctx: ExpParser.FunctionCallContext?): Int? {
        val functionName = ctx!!.IDENTIFIER().text
        val sourceCodeFunction = scope.getFunction(functionName)
        if (sourceCodeFunction == null && !preDefinedFunctions.containsKey(functionName)) {
            throw UnknownFunctionException(functionName, ctx.start.line)
        }
        val calculatedArguments = ctx.arguments().expression().asSequence().map(this::visit).map { it as Int }.toList()
        return if (sourceCodeFunction != null) {
            sourceCodeFunction.execute(scope, calculatedArguments)
        } else {
            preDefinedFunctions[functionName]!!.execute(scope, calculatedArguments)
            null
        }
    }

    override fun visitBinaryExpression(ctx: ExpParser.BinaryExpressionContext?): Int {
        val operation = ctx!!.operation.text
        if (!binaryOperationFunctions.containsKey(operation)) {
            throw UnknownOperationException(operation, ctx.operation.line)
        }
        return binaryOperationFunctions[operation]!!.invoke(visit(ctx.leftOperand)!!, visit(ctx.rightOperand)!!)
    }

    override fun visitIdentifierExpression(ctx: ExpParser.IdentifierExpressionContext?): Int {
        val identifierName = ctx!!.IDENTIFIER().text
        return scope.getVariableValue(identifierName) ?: throw UnknownVariableException(identifierName, ctx.start.line)
    }

    override fun visitLiteralExpression(ctx: ExpParser.LiteralExpressionContext?): Int {
        val literal = ctx!!.LITERAL().text.toIntOrNull()
        return literal ?: throw LiteralIsNotANumberException(ctx.LITERAL().text, ctx.start.line)
    }
}