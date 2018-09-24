package ru.hse.spb

import org.junit.Test

class TestSource {
    @Test
    fun example1() {
        val sourceCode = "var a = 10\n" +
                "var b = 20\n" +
                "if (a > b) {\n" +
                "    println(1)\n" +
                "} else {\n" +
                "    println(0)\n" +
                "}"
        interpretSourceCode(sourceCode)
    }
}