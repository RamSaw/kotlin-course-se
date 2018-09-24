package ru.hse.spb

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.RuleContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.hse.spb.parser.ExpLexer
import ru.hse.spb.parser.ExpParser
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ParsingTest {
    private val stdErr = System.err
    private val err = ByteArrayOutputStream()

    @Before
    fun changeOutput() {
        System.setErr(PrintStream(err))
    }

    @After
    fun resetOutput() {
        System.setErr(stdErr)
    }

    private fun ruleBaseTest(sourceCode: String, methodName: String,
                             parsedCode: String = sourceCode.replace(" ", "")) {
        val expLexer = ExpLexer(CharStreams.fromString(sourceCode))
        val expParser = ExpParser(BufferedTokenStream(expLexer))
        assertEquals(parsedCode,
                (expParser.javaClass.getMethod(methodName).invoke(expParser) as RuleContext).text)
        assertTrue(err.size() == 0)
    }

    @Test
    fun testEmptyBlock() {
        ruleBaseTest("", "block")
    }

    @Test
    fun testArguments() {
        ruleBaseTest("10,1+1,a", "arguments")
    }

    @Test
    fun testFunctionCall() {
        ruleBaseTest("foo(10,1+1,a)", "functionCall")
    }

    @Test
    fun testBinaryExpression() {
        ruleBaseTest("10+foo(a)", "expression")
    }

    @Test
    fun testExpressionInBraces() {
        ruleBaseTest("(1+1)", "expression")
    }

    @Test
    fun testReturnStatement() {
        ruleBaseTest("return(1+1)", "returnStatement")
    }

    @Test
    fun testAssignment() {
        ruleBaseTest("i=10", "assignment")
    }

    @Test
    fun testIfStatement() {
        ruleBaseTest("if(i==10){return1}else{println(10)}", "ifStatement")
    }

    @Test
    fun testWhileStatement() {
        ruleBaseTest("while(i==10){return1}", "whileStatement")
    }

    @Test
    fun testParameterNames() {
        ruleBaseTest("a,b,c", "parameterNames")
    }

    @Test
    fun testVariableDeclaration() {
        ruleBaseTest("var i=10", "variableDeclaration")
    }

    @Test
    fun testFunctionDefinition() {
        ruleBaseTest("fun foo(i) { println(i, 10) }", "functionDefinition")
    }

    @Test
    fun testBlockWithBraces() {
        ruleBaseTest("{ println(i, 10) }", "blockWithBraces")
    }

    @Test
    fun testBlock() {
        ruleBaseTest("foo(10) var i = 1", "block")
    }

    @Test
    fun testFile() {
        ruleBaseTest("", "file", "<EOF>")
    }
}