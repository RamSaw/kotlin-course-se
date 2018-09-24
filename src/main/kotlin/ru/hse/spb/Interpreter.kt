package ru.hse.spb

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import ru.hse.spb.parser.ExpLexer
import ru.hse.spb.parser.ExpParser
import java.util.*

fun interpretSourceCode(sourceCode: String) {
    val expLexer = ExpLexer(CharStreams.fromString(sourceCode))
    val expParser = ExpParser(BufferedTokenStream(expLexer))
    interpret(expParser.file())
}

private fun interpret(fileContext: ExpParser.FileContext) {
    InterpreterVisitor(Scope(null)).visitFile(fileContext)
}

private fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}

val binaryOperationFunctions = mapOf<String, (Int, Int) -> Int>(
        "+" to { a, b -> a + b },
        "-" to { a, b -> a - b },
        "*" to { a, b -> a * b },
        "/" to { a, b -> a / b },
        "%" to { a, b -> a % b },
        ">" to { a, b -> (a > b).toInt() },
        "<" to { a, b -> (a < b).toInt() },
        ">=" to { a, b -> (a >= b).toInt() },
        "<=" to { a, b -> (a <= b).toInt() },
        "==" to { a, b -> (a == b).toInt() },
        "!=" to { a, b -> (a != b).toInt() },
        "||" to { a, b -> (a != 0 || b != 0).toInt() },
        "&&" to { a, b -> (a != 0 && b != 0).toInt() }
)

interface PreDefinedFunction {
    fun execute(scope: Scope, arguments: List<Int>)
}

val preDefinedFunctions = mapOf<String, PreDefinedFunction>(
        "println" to object : PreDefinedFunction {
            override fun execute(scope: Scope, arguments: List<Int>) {
                println(arguments.joinToString(" "))
            }
        }
)

class SourceCodeFunction(functionDefinition: ExpParser.FunctionDefinitionContext) {
    private val parameterNames = functionDefinition.parameterNames().IDENTIFIER().map { it.text }
    private val body = functionDefinition.blockWithBraces()
    private val functionName = functionDefinition.IDENTIFIER().text
    private val correctArgumentsNumber = parameterNames.size
    private val definitionStartLine = functionDefinition.start.line

    fun execute(scope: Scope, arguments: List<Int>): Int {
        if (arguments.size != parameterNames.size) {
            throw InvalidArgumentsNumberException(functionName, correctArgumentsNumber,
                    arguments.size, definitionStartLine)
        }
        val newScope = Scope(scope)
        for (i in 0 until correctArgumentsNumber) {
            newScope.addNewVariable(parameterNames[i], arguments[i])
        }
        return InterpreterVisitor(newScope).visitBlockWithBraces(body) ?: 0
    }
}

data class Scope(val upperScope: Scope?) {
    private val functions: MutableMap<String, SourceCodeFunction> = HashMap()
    private val variables: MutableMap<String, Int> = HashMap()

    fun getFunction(functionName: String): SourceCodeFunction? {
        return functions[functionName] ?: upperScope?.getFunction(functionName)
    }

    fun getVariableValue(variableName: String): Int? {
        return getVariableScope(variableName)?.variables?.get(variableName)
    }

    fun addNewVariable(variableName: String, value: Int?): Int? {
        if (variables.containsKey(variableName)) {
            return null
        }
        variables[variableName] = value ?: 0
        return variables[variableName]
    }

    fun addNewFunction(functionName: String, function: SourceCodeFunction): SourceCodeFunction? {
        if (functions.containsKey(functionName)) {
            return null
        }
        functions[functionName] = function
        return functions[functionName]
    }

    fun setVariableValue(variableName: String, value: Int): Int? {
        val variableScope = getVariableScope(variableName) ?: return null
        val previousValue = variableScope.variables[variableName]
        variableScope.variables[variableName] = value
        return previousValue
    }

    private fun getVariableScope(variableName: String): Scope? {
        return if (variables.containsKey(variableName)) this else upperScope?.getVariableScope(variableName)
    }
}