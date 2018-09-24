package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ComplexTest {
    private fun changeStandardOutput(out: ByteArrayOutputStream) {
        System.setOut(PrintStream(out))
    }

    @Test
    fun example1() {
        val sourceCode = "var a = 10\n" +
                "var b = 20\n" +
                "if (a > b) {\n" +
                "    println(1)\n" +
                "} else {\n" +
                "    println(0)\n" +
                "}"
        val expected = "0\n"
        baseExampleTest(sourceCode, expected)
    }

    @Test
    fun example2() {
        val sourceCode = "fun fib(n) {\n" +
                "    if (n <= 1) {\n" +
                "        return 1\n" +
                "    }\n" +
                "    return fib(n - 1) + fib(n - 2)\n" +
                "}\n" +
                "\n" +
                "var i = 1\n" +
                "while (i <= 5) {\n" +
                "    println(i, fib(i))\n" +
                "    i = i + 1\n" +
                "}"
        val expected = "1 1\n2 2\n3 3\n4 5\n5 8\n"
        baseExampleTest(sourceCode, expected)
    }

    @Test
    fun example3() {
        val sourceCode = "fun foo(n) {\n" +
                "    fun bar(m) {\n" +
                "        return m + n\n" +
                "    }\n" +
                "\n" +
                "    return bar(1)\n" +
                "}\n" +
                "\n" +
                "println(foo(41)) // prints 42"
        val expected = "42\n"
        baseExampleTest(sourceCode, expected)
    }

    private fun baseExampleTest(sourceCode: String, exprected: String) {
        val buffer = ByteArrayOutputStream()
        changeStandardOutput(buffer)
        interpretSourceCode(sourceCode)
        assertEquals(exprected, buffer.toString())
    }
}