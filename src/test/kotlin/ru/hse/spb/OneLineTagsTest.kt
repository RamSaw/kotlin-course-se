package ru.hse.spb

import junit.framework.TestCase.assertEquals
import org.junit.Test

class OneLineTagsTest {
    @Test
    fun testDocumentClass() {
        assertEquals("""
            |\documentclass[a4paper,20pt]{beamer}
            |\begin{document}
            |\end{document}
            |""".trimMargin(),
                document {
                    documentClass("beamer", "a4paper", "20pt")
                }.toString()
        )
    }

    @Test(expected = InvalidTexException::class)
    fun testNoDocumentClassFaild() {
        document {
        }.toString()
    }

    @Test(expected = InvalidTexException::class)
    fun testSeveralDocumentClassFaild() {
        document {
            documentClass("beamer", "a4paper", "20pt")
            documentClass("beamer", "a4paper", "20pt")
        }.toString()
    }

    @Test
    fun testUsePackage() {
        assertEquals("""
            |\documentclass[a4paper,20pt]{beamer}
            |\usepackage[]{loca}
            |\usepackage[english,russian]{babel}
            |\usepackage[russian]{babel}
            |\begin{document}
            |\end{document}
            |""".trimMargin(),
                document {
                    documentClass("beamer", "a4paper", "20pt")
                    usepackage("loca")
                    usepackage("babel", "english", "russian")
                    usepackage("babel", "russian")
                }.toString()
        )
    }
}