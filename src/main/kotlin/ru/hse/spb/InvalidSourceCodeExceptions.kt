package ru.hse.spb

open class InvalidSourceCodeException(private val s: String, private val startLine: Int) : Throwable() {
    override val message: String?
        get() = super.message + "\nError starts at line: $startLine\n$s\n"
}

class FunctionMultipleDeclarationException(functionName: String?, startLine: Int) :
        InvalidSourceCodeException("Function $functionName was declared multiple times", startLine)

class InvalidArgumentsNumberException(functionName: String, correctArgumentsNumber: Int,
                                      passedArgumentsNumber: Int, startLine: Int) :
        InvalidSourceCodeException("In function $functionName was passed " +
                "$passedArgumentsNumber but must be $correctArgumentsNumber", startLine)

class LiteralIsNotANumberException(literal: String, startLine: Int) :
        InvalidSourceCodeException("Literal can be only numbers, but found $literal", startLine)

class UnknownFunctionException(functionName: String, startLine: Int) :
        InvalidSourceCodeException("Unknown function $functionName", startLine)

class UnknownOperationException(operation: String, startLine: Int) :
        InvalidSourceCodeException("Unknown operation $operation", startLine)

class UnknownVariableException(variableName: String, startLine: Int) :
        InvalidSourceCodeException("Unknown variable $variableName", startLine)

class VariableMultipleDeclarationException(variableName: String?, startLine: Int) :
        InvalidSourceCodeException("Variable $variableName was declared multiple times", startLine)
