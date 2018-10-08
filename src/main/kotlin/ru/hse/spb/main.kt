package ru.hse.spb

import java.nio.file.Paths

fun main(args: Array<String>) {
    document {
        documentClass("beamer")
        usepackage("babel", "russian")
        +"test PDF"
    }.exportToPDF(Paths.get("./generated"))
}