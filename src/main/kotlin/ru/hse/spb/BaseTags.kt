package ru.hse.spb

import java.io.OutputStream
import kotlin.streams.toList

@DslMarker
annotation class TexTagMarker

@TexTagMarker
interface Element {
    fun render(builder: StringBuilder, indent: String) {
        render({ s: String -> builder.append(s) }, indent)
    }

    fun render(out: OutputStream, indent: String) {
        render({ s: String -> out.write(s.toByteArray()) }, indent)
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
        val builder = StringBuilder()
        builder.append(options!!.joinToString(","))
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

fun joinOptionPairs(options: Array<out Pair<String, String>>): List<String> = options.toList().stream().map { t -> "${t.first}=${t.second}" }.toList()
