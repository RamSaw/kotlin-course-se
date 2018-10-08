package ru.hse.spb

fun main(args: Array<String>) {
    document {
        documentClass("beamer", "12pt", "a4paper")
        usepackage("babel", "english", "russian")
        +"AAAA"
        frame("title1", "arg1" to "arg2") {
            frame(frameTitle = "title2", options = *arrayOf("arg1" to "arg2")) {
            }
        }
    }.toOutputStream(System.out)
}