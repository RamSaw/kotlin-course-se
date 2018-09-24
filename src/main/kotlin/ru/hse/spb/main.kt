package ru.hse.spb

import java.io.File

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Invalid arguments. Must be: <path to source file>")
        return
    }
    println("==========RESULT==========")
    println(interpretSourceCode(File(args[0]).readText()))
}