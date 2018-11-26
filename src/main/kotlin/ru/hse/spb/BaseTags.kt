package ru.hse.spb

import java.io.OutputStream

@DslMarker
annotation class TexTagMarker

@TexTagMarker
interface Element {
    fun render(builder: StringBuilder, indent: String) {
        render({ builder.append(it) }, indent)
    }

    fun render(out: OutputStream, indent: String) {
        render({ out.write(it.toByteArray()) }, indent)
    }

    fun render(print: (String) -> Unit, indent: String)
}

abstract class Tag(val name: String) : Element {
    val children = arrayListOf<Element>()
    protected abstract val value: String?
    protected abstract val options: List<String>?

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
        return buildString {
            append(options!!.joinToString(","))
        }
    }

    override fun toString(): String {
        return buildString {
            render(this, "")
        }
    }

    fun toOutputStream(out: OutputStream) {
        render(out, "")
    }
}

class TextElement(private val text: String) : Element {
    override fun render(print: (String) -> Unit, indent: String) {
        print("$indent$text\n")
    }
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

fun joinOptionPairs(options: Array<out Pair<String, String>>): List<String> = options.map { t -> "${t.first}=${t.second}" }.toList()
