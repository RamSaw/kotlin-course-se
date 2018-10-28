package ru.hse.spb

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.hse.spb.parser.ExpLexer
import ru.hse.spb.parser.ExpParser
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class InterpreterTest {
    private val stdErr = System.err
    private val stdOut = System.err
    private val err = ByteArrayOutputStream()
    private val out = ByteArrayOutputStream()

    @Before
    fun changeOutput() {
        System.setOut(PrintStream(out))
        System.setErr(PrintStream(err))
    }

    @After
    fun resetOutput() {
        System.setOut(stdOut)
        System.setErr(stdErr)
    }

    private fun checkStreams(outExpected: String, errExpected: String) {
        assertEquals(outExpected, out.toString())
        assertEquals(errExpected, err.toString())
    }

    private fun getExpParser(sourceCode: String): ExpParser {
        val expLexer = ExpLexer(CharStreams.fromString(sourceCode))
        return ExpParser(BufferedTokenStream(expLexer))
    }

    @Test
    fun testEmptyBlock() {
        val sourceCode = ""
        assertEquals(null,
                InterpreterVisitor(Scope(null)).visitBlock(getExpParser(sourceCode).block()))
        checkStreams("", "")
    }

    @Test
    fun testFunctionCall() {
        val sourceCode = "fun foo(u, d, c) { return u + d + c } foo(10,1+1,a)"
        val scope = Scope(null)
        val parser = getExpParser(sourceCode)
        scope.addNewVariable("a", 10)
        scope.addNewFunction("foo", SourceCodeFunction(parser.functionDefinition()))
        assertEquals(22,
                InterpreterVisitor(scope).visitFunctionCall(parser.functionCall()))
        checkStreams("", "")
    }

    @Test
    fun testPrintln() {
        val sourceCode = "println(1, a)"
        val scope = Scope(null)
        val parser = getExpParser(sourceCode)
        scope.addNewVariable("a", 10)
        assertEquals(null,
                InterpreterVisitor(scope).visitFunctionCall(parser.functionCall()))
        checkStreams("1 10\n", "")
    }

    @Test
    fun testLogicalOr1() {
        expressionBaseTest("0 || 0", 0)
    }

    @Test
    fun testLogicalOr2() {
        expressionBaseTest("-1 || 0", 1)
    }

    @Test
    fun testLogicalOr3() {
        expressionBaseTest("0 || -1 || 1", 1)
    }

    @Test
    fun testEquality1() {
        expressionBaseTest("0 == 0", 1)
    }

    @Test
    fun testEquality2() {
        expressionBaseTest("-1 == 0", 0)
    }

    @Test
    fun testInequality1() {
        expressionBaseTest("-1 != -1", 0)
    }

    @Test
    fun testInequality2() {
        expressionBaseTest("-1 != 0", 1)
    }

    @Test
    fun testGreaterOrEqual() {
        expressionBaseTest("0 >= 0", 1)
    }

    @Test
    fun testLessOrEqual() {
        expressionBaseTest("4 <= 0", 0)
    }

    @Test
    fun testLess() {
        expressionBaseTest("-1 < -1", 0)
    }

    @Test
    fun testGreater() {
        expressionBaseTest("-1 > -2", 1)
    }

    @Test
    fun testPlus() {
        expressionBaseTest("-1 + -1", -2)
    }

    @Test
    fun testMinus() {
        expressionBaseTest("2 - 2", 0)
    }

    @Test
    fun testMultiplication() {
        expressionBaseTest("-1 * -2", 2)
    }

    @Test
    fun testDivision() {
        expressionBaseTest("4 / -2", -2)
    }

    @Test
    fun testRemainder() {
        expressionBaseTest("3 % 2", 1)
    }

    @Test
    fun testOperatorsPriority1() {
        expressionBaseTest("0 == 0 && 1", 1)
    }

    @Test
    fun testOperatorsPriority2() {
        expressionBaseTest("0 == (1 && 1)", 0)
    }

    @Test
    fun testOperatorsPriority3() {
        expressionBaseTest("1 == -1 || 0", 0)
    }

    @Test
    fun testOperatorsPriority4() {
        expressionBaseTest("2 + 2 * 3", 2 + 2 * 3)
    }

    private fun expressionBaseTest(expression: String, expectedResult: Int) {
        val sourceCode = "println($expression)"
        val scope = Scope(null)
        val parser = getExpParser(sourceCode)
        assertEquals(null,
                InterpreterVisitor(scope).visitFunctionCall(parser.functionCall()))
        checkStreams("$expectedResult\n", "")
    }

    @Test
    fun testExpressionInBraces() {
        val sourceCode = "(a + 1)"
        val scope = Scope(null)
        scope.addNewVariable("a", 10)
        assertEquals(11,
                InterpreterVisitor(scope)
                        .visitExpression(getExpParser(sourceCode).expression()))
        checkStreams("", "")
    }

    @Test
    fun testReturnStatement() {
        val sourceCode = "return (a + 1)"
        val scope = Scope(null)
        scope.addNewVariable("a", 10)
        assertEquals(11,
                InterpreterVisitor(scope)
                        .visitReturnStatement(getExpParser(sourceCode).returnStatement()))
        checkStreams("", "")
    }

    @Test
    fun testAssignment() {
        val sourceCode = "a = 0"
        val scope = Scope(null)
        scope.addNewVariable("a", 10)
        assertEquals(null, InterpreterVisitor(scope)
                .visitAssignment(getExpParser(sourceCode).assignment()))
        assertEquals(0, scope.getVariableValue("a"))
        checkStreams("", "")
    }

    @Test
    fun testIfStatement() {
        val sourceCode = "if(a==10){return 1}else{println(10)}"
        val scope = Scope(null)
        scope.addNewVariable("a", 10)
        assertEquals(1, InterpreterVisitor(scope)
                .visitIfStatement(getExpParser(sourceCode).ifStatement()))
        checkStreams("", "")
        scope.setVariableValue("a", 11)
        assertEquals(null, InterpreterVisitor(scope)
                .visitIfStatement(getExpParser(sourceCode).ifStatement()))
        checkStreams("10\n", "")
    }

    @Test
    fun testWhileStatement() {
        val sourceCode = "while(i < 2){println(i) i = i + 1}"
        val scope = Scope(null)
        scope.addNewVariable("i", 0)
        assertEquals(null, InterpreterVisitor(scope)
                .visitWhileStatement(getExpParser(sourceCode).whileStatement()))
        checkStreams("0\n1\n", "")
    }

    @Test
    fun testWhileStatementWithReturn() {
        val sourceCode = "while(i == 2){ return 10 }"
        val scope = Scope(null)
        scope.addNewVariable("i", 2)
        assertEquals(10, InterpreterVisitor(scope)
                .visitWhileStatement(getExpParser(sourceCode).whileStatement()))
        checkStreams("", "")
    }

    @Test
    fun testFunctionDefinition() {
        val sourceCode = "fun foo() { return 2 + 2 }"
        val scope = Scope(null)
        assertEquals(null, InterpreterVisitor(scope)
                .visitFunctionDefinition(getExpParser(sourceCode).functionDefinition()))
        checkStreams("", "")
        assertEquals(4, scope.getFunction("foo")!!.execute(scope, emptyList()))
    }

    @Test
    fun testUpperScope() {
        val sourceCode = "fun foo() { return a + 2 }"
        val upperScope = Scope(null)
        upperScope.addNewVariable("a", 2)
        val scope = Scope(upperScope)
        assertEquals(null, InterpreterVisitor(scope)
                .visitFunctionDefinition(getExpParser(sourceCode).functionDefinition()))
        checkStreams("", "")
        assertEquals(4, scope.getFunction("foo")!!.execute(scope, emptyList()))
    }

    @Test
    fun testBlockWithBraces() {
        val sourceCode = "{ println(i) return 10 }"
        val scope = Scope(null)
        scope.addNewVariable("i", 2)
        assertEquals(10, InterpreterVisitor(scope)
                .visitBlockWithBraces(getExpParser(sourceCode).blockWithBraces()))
        checkStreams("2\n", "")
    }

    @Test
    fun testBlock() {
        val sourceCode = "println(i) return 1"
        val scope = Scope(null)
        scope.addNewVariable("i", 2)
        assertEquals(1, InterpreterVisitor(scope)
                .visitBlock(getExpParser(sourceCode).block()))
        checkStreams("2\n", "")
    }

    @Test
    fun testComment() {
        val sourceCode = "// println(10)"
        val scope = Scope(null)
        assertEquals(null, InterpreterVisitor(scope)
                .visitFile(getExpParser(sourceCode).file()))
        checkStreams("", "")
    }

    @Test(expected = UnknownFunctionException::class)
    fun testFunctionVisibilityIncorrect() {
        val sourceCode = "if (1 == 1) { fun foo() { return 10 } } foo()"
        val scope = Scope(null)
        InterpreterVisitor(scope).visitFile(getExpParser(sourceCode).file())
    }

    @Test
    fun testFunctionVisibilityCorrect() {
        val sourceCode = "if (1 == 1) { fun foo() { return 10 } if (2 == 2) { return foo() } }"
        val scope = Scope(null)
        assertEquals(10, InterpreterVisitor(scope)
                .visitIfStatement(getExpParser(sourceCode).ifStatement()))
        checkStreams("", "")
    }

    @Test(expected = FunctionMultipleDeclarationException::class)
    fun testOverloadsNotSupported() {
        val sourceCode = "fun foo() { return 10 } fun foo(a) { return a }"
        val scope = Scope(null)
        InterpreterVisitor(scope).visitFile(getExpParser(sourceCode).file())
    }

    @Test
    fun testRedefinition() {
        val sourceCode = "var a = 4 if (a == 4) { var a = 5 println(a) }"
        val scope = Scope(null)
        InterpreterVisitor(scope).visitFile(getExpParser(sourceCode).file())
        checkStreams("5\n", "")
    }

    @Test(expected = UnknownFunctionException::class)
    fun testUnknownFunction() {
        val sourceCode = "foo(a)"
        val scope = Scope(null)
        InterpreterVisitor(scope).visitFile(getExpParser(sourceCode).file())
    }

    @Test(expected = UnknownVariableException::class)
    fun testUnknownVariable() {
        val sourceCode = "a = 2"
        val scope = Scope(null)
        InterpreterVisitor(scope).visitFile(getExpParser(sourceCode).file())
    }

    @Test
    fun testFunctionWithoutReturn() {
        val sourceCode = "fun foo() {} println(foo())"
        val scope = Scope(null)
        InterpreterVisitor(scope).visitFile(getExpParser(sourceCode).file())
        checkStreams("0\n", "")
    }
}