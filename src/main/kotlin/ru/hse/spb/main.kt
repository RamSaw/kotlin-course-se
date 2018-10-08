package ru.hse.spb

fun main(args: Array<String>) {
    val a = 4
    val rows = listOf(1, 2, 3)


    document {
        documentClass("beamer")
        usepackage("babel", "russian" /* varargs */)
        frame(frameTitle = "frametitle", options = *arrayOf("arg1" to "arg2")) {
            itemize {
                for (row in rows) {
                    item { +"$row text" }
                }
            }
        }
        // begin{pyglist}[language=kotlin]...\end{pyglist}
        customTag(name = "frame", options = *arrayOf("language" to "kotlin")) {
            +"""
               |val a = 1
               |
            """
        }
    }.toOutputStream(System.out)
}