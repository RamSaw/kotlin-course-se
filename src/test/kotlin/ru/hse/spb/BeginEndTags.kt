package ru.hse.spb

import junit.framework.TestCase.assertEquals
import org.junit.Test

class BeginEndTags {
    @Test
    fun testLeft() {
        val expected = """
            |\documentclass[a4paper,20pt]{beamer}
            |\begin{document}
            |  \begin{flushleft}
            |    left text
            |  \end{flushleft}
            |\end{document}
            |""".trimMargin()
        val real = document {
            documentClass("beamer", "a4paper", "20pt")
            left {
                +"left text"
            }
        }.toString()
        assertEquals(expected, real)
    }

    @Test
    fun testRight() {
        val expected = """
            |\documentclass[a4paper,20pt]{beamer}
            |\begin{document}
            |  \begin{flushright}
            |    right text
            |  \end{flushright}
            |\end{document}
            |""".trimMargin()
        val real = document {
            documentClass("beamer", "a4paper", "20pt")
            right {
                +"right text"
            }
        }.toString()
        assertEquals(expected, real)
    }

    @Test
    fun testCenter() {
        val expected = """
            |\documentclass[a4paper,20pt]{beamer}
            |\begin{document}
            |  \begin{center}
            |    center text
            |  \end{center}
            |\end{document}
            |""".trimMargin()
        val real = document {
            documentClass("beamer", "a4paper", "20pt")
            center {
                +"center text"
            }
        }.toString()
        assertEquals(expected, real)
    }

    @Test
    fun testMath() {
        val a = 0
        val expected = """
            |\documentclass[a4paper,20pt]{beamer}
            |\begin{document}
            |  \begin{displaymath}
            |    0 + b
            |  \end{displaymath}
            |\end{document}
            |""".trimMargin()
        val real = document {
            documentClass("beamer", "a4paper", "20pt")
            math {
                +"$a + b"
            }
        }.toString()
        assertEquals(expected, real)
    }

    @Test
    fun testCustomTag() {
        val a = 0
        val expected = """
            |\documentclass[a4paper,20pt]{beamer}
            |\begin{document}
            |  \begin{customTag}[arg1=arg2]
            |    0 + b
            |  \end{customTag}
            |\end{document}
            |""".trimMargin()
        val real = document {
            documentClass("beamer", "a4paper", "20pt")
            customTag("customTag", "arg1" to "arg2") {
                +"$a + b"
            }
        }.toString()
        assertEquals(expected, real)
    }

    @Test
    fun testItemize() {
        val a = 0
        val expected = """
            |\documentclass[a4paper,20pt]{beamer}
            |\begin{document}
            |  \begin{itemize}
            |    \item
            |      0 + b
            |    \item
            |  \end{itemize}
            |\end{document}
            |""".trimMargin()
        val real = document {
            documentClass("beamer", "a4paper", "20pt")
            itemize {
                item { +"$a + b" }
                item { }
            }
        }.toString()
        assertEquals(expected, real)
    }

    @Test
    fun testEnumerate() {
        val a = 0
        val expected = """
            |\documentclass[a4paper,20pt]{beamer}
            |\begin{document}
            |  \begin{enumerate}
            |    \item
            |      0 + b
            |    \item
            |  \end{enumerate}
            |\end{document}
            |""".trimMargin()
        val real = document {
            documentClass("beamer", "a4paper", "20pt")
            enumerate {
                item { +"$a + b" }
                item { }
            }
        }.toString()
        assertEquals(expected, real)
    }

    @Test
    fun testFrame() {
        val expected = """
            |\documentclass[]{beamer}
            |\begin{document}
            |  \begin{frame}[arg1=arg2]{frametitle}
            |    frame
            |  \end{frame}
            |\end{document}
            |""".trimMargin()
        val real = document {
            documentClass("beamer")
            frame(frameTitle = "frametitle", options = *arrayOf("arg1" to "arg2")) {
                +"frame"
            }
        }.toString()
        assertEquals(expected, real)
    }

    @Test
    fun testDocument() {
        val expected = """
            |\documentclass[]{beamer}
            |\usepackage[russian]{babel}
            |\begin{document}
            |  \begin{frame}[arg1=arg2]{frametitle}
            |    \begin{itemize}
            |      \item
            |        1 text
            |      \item
            |        2 text
            |    \end{itemize}
            |    \begin{pyglist}[language=kotlin]
            |      val a = 1
            |haha
            |    \end{pyglist}
            |  \end{frame}
            |\end{document}
            |""".trimMargin()
        val rows = listOf(1, 2)
        val real = document {
            documentClass("beamer")
            usepackage("babel", "russian" /* varargs */)
            frame(frameTitle = "frametitle", options = *arrayOf("arg1" to "arg2")) {
                itemize {
                    for (row in rows) {
                        item { +"$row text" }
                    }
                }

                // begin{pyglist}[language=kotlin]...\end{pyglist}
                customTag(name = "pyglist", options = *arrayOf("language" to "kotlin")) {
                    +"""
               |val a = 1
               |haha
            """.trimMargin()
                }
            }
        }.toString()
        assertEquals(expected, real)
    }
}