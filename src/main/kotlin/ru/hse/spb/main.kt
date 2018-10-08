package ru.hse.spb

fun result(args: Array<String>) =
        document {
            documentClass("beamer")
            usepackage {}
            frame {}
        }

fun main(args: Array<String>) {
    val res = result(arrayOf("first", "second"))
    println(res)
}