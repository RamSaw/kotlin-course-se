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
    fun testBinaryExpression() {
        val sourceCode = "a + 1"
        val scope = Scope(null)
        scope.addNewVariable("a", 10)
        assertEquals(11,
                InterpreterVisitor(scope)
                        .visitBinaryExpression(getExpParser(sourceCode).expression()
                                as ExpParser.BinaryExpressionContext?))
        checkStreams("", "")
    }

    @Test
    fun testExpressionInBraces() {
        val sourceCode = "(a + 1)"
        val scope = Scope(null)
        scope.addNewVariable("a", 10)
        assertEquals(11,
                InterpreterVisitor(scope)
                        .visitExpressionInBraces(getExpParser(sourceCode).expression()
                                as ExpParser.ExpressionInBracesContext?))
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
}