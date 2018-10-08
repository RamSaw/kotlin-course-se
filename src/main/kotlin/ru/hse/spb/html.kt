package ru.hse.spb

import java.io.OutputStream

interface Element {
    fun render(builder: StringBuilder, indent: String) {
        render({ s: String -> builder.append(s) }, indent)
    }

    fun render(out: OutputStream, indent: String) {
        render({ s: String -> out.write(s.toByteArray()) }, indent)
    }

    fun render(print: (String) -> Unit, indent: String)
}

class TextElement(private val text: String) : Element {
    override fun render(print: (String) -> Unit, indent: String) {
        print("$indent$text\n")
    }
}

@DslMarker
annotation class TexTagMarker

@TexTagMarker
abstract class Tag(val name: String) : Element {
    val children = arrayListOf<Element>()
    protected abstract val value: String?
    protected abstract val options: List<String>?
    val attributes = hashMapOf<String, String>()

    protected fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    override fun render(print: (String) -> Unit, indent: String) {
        print("$indent\\$name${if (options == null) "" else "[${renderOptions()}]"}${if (value == null) "" else "{$value}"}\n")
        for (c in children) {
            c.render(print, "$indent  ")
        }
    }

    private fun renderOptions(): String {
        val builder = StringBuilder()
        builder.append(options!!.joinToString(","))
        return builder.toString()
    }

    private fun renderAttributes(): String {
        val builder = StringBuilder()
        for ((attr, value) in attributes) {
            builder.append(" $attr=\"$value\"")
        }
        return builder.toString()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }

    fun toOutputStream(out: OutputStream) {
        render(out, "")
    }
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

abstract class BeginEndBlockTag(name: String) : TagWithText(name) {
    override fun render(print: (String) -> Unit, indent: String) {
        print("$indent\\begin{$name}${if (options == null) "" else "[${renderOptions()}]"}\n")
        for (c in children) {
            c.render(print, "$indent  ")
        }
        print("$indent\\end{$name}\n")
    }

    private fun renderOptions(): String {
        val builder = StringBuilder()
        builder.append(options!!.joinToString(","))
        return builder.toString()
    }
}

class Document : BeginEndBlockTag("document") {
    override var value: String? = null
    override var options: List<String>? = null

    fun documentClass(aClass: String, vararg options: String) {
        initTag(DocumentClass(aClass, options.toList())) {}
    }

    fun usepackage(aPackage: String, vararg options: String) = initTag(UsePackage(aPackage, options.toList())) {}
}

class DocumentClass(override val value: String?, override val options: List<String>) : TagWithText("documentClass")

class UsePackage(override val value: String?, override val options: List<String>) : TagWithText("usepackage")

fun document(init: Document.() -> Unit): Document {
    val document = Document()
    document.init()
    return document
}