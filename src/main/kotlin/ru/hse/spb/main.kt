package ru.hse.spb

import java.nio.file.Paths

fun main(args: Array<String>) {
    val rows = listOf(1, 2, 3)
    document {
        documentClass("beamer")
        usepackage("babel", "russian" /* varargs */)
        +"AA"
    }.exportToPDF(Paths.get("./generated"))
}