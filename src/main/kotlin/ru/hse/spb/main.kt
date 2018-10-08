package ru.hse.spb

fun main(args: Array<String>) {
    document {
        documentClass("beamer", "12pt", "a4paper")
        usepackage("babel")
        +"AAAA"
    }.toOutputStream(System.out)
}