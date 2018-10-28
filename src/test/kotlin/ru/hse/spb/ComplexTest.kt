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
        val sourceCode =
                """var a = 10
                var b = 20
                if (a > b) {
                    println(1)
                } else {
                    println(0)
                }"""
        val expected = "0\n"
        baseExampleTest(sourceCode, expected)
    }

    @Test
    fun example2() {
        val sourceCode =
                """fun fib(n) {
                        if (n <= 1) {
                            return 1
                        }
                        return fib(n - 1) + fib(n - 2)
                   }

                   var i = 1
                   while (i <= 5) {
                       println(i, fib(i))
                       i = i + 1
                   }"""
        val expected = "1 1\n2 2\n3 3\n4 5\n5 8\n"
        baseExampleTest(sourceCode, expected)
    }

    @Test
    fun example3() {
        val sourceCode =
                """ fun foo(n) {
                        fun bar(m) {
                            return m + n
                        }

                        return bar(1)
                    }
                    println(foo(41)) // prints 42"""
        val expected = "42\n"
        baseExampleTest(sourceCode, expected)
    }

    @Test(expected = InvalidSourceCodeException::class)
    fun testParsingErrorHandling() {
        val sourceCode =
                """ fun foo(n) {
                        fun bar(m) {
                            return m + n
                        }

                        return bar(1)
                    }
                    println(foo(41.0)) // prints 42"""
        val expected = "42\n"
        baseExampleTest(sourceCode, expected)
    }

    private fun baseExampleTest(sourceCode: String, expected: String) {
        val buffer = ByteArrayOutputStream()
        changeStandardOutput(buffer)
        interpretSourceCode(sourceCode)
        assertEquals(expected, buffer.toString())
    }
}