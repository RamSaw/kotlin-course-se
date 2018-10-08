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

class TextElement(private val text: String) : Element {
    override fun render(print: (String) -> Unit, indent: String) {
        print("$indent$text\n")
    }
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

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

abstract class BeginEndBlockTag(override val value: String?, override val options: List<String>?, name: String) : TagWithText(name) {
    fun itemize(init: Itemize.() -> Unit) = initTag(Itemize(), init)

    fun enumerate(init: Enumerate.() -> Unit) = initTag(Enumerate(), init)

    fun math(init: Math.() -> Unit) = initTag(Math(), init)

    fun left(init: Left.() -> Unit) = initTag(Left(), init)

    fun right(init: Right.() -> Unit) = initTag(Right(), init)

    fun center(init: Center.() -> Unit) = initTag(Center(), init)

    fun customTag(name: String, vararg options: Pair<String, String>, init: CustomTag.() -> Unit) = initTag(CustomTag(name, joinOptionPairs(options)), init)

    override fun render(print: (String) -> Unit, indent: String) {
        print("$indent\\begin{$name}${if (options == null) "" else "[${renderOptions()}]"}${if (value == null) "" else "{$value}"}\n")
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

abstract class ItemizableTag(name: String) : BeginEndBlockTag(null, null, name) {
    fun item(init: Item.() -> Unit) = initTag(Item(), init)
}

class Left : BeginEndBlockTag(null, null, "flushleft")

class Right : BeginEndBlockTag(null, null, "flushright")

class Center : BeginEndBlockTag(null, null, "center")

class Math : BeginEndBlockTag(null, null, "displaymath")

class CustomTag(name: String, options: List<String>?) : BeginEndBlockTag(null, options, name)

class Item : TagWithText("item") {
    override val value: String?
        get() = null
    override val options: List<String>?
        get() = null

}

class Itemize : ItemizableTag("itemize")

class Enumerate : ItemizableTag("enumerate")

class Frame(value: String?, options: List<String>?) : BeginEndBlockTag(value, options, "frame")

class Document : BeginEndBlockTag(null, null, "document") {
    private val documentClasses: MutableList<DocumentClass> = listOf<DocumentClass>().toMutableList()
    private val usePackages: MutableList<UsePackage> = listOf<UsePackage>().toMutableList()
    override fun render(print: (String) -> Unit, indent: String) {
        renderHeader(print, indent)
        super.render(print, indent)
    }

    private fun renderHeader(print: (String) -> Unit, indent: String) {
        documentClasses.forEach { documentClass: DocumentClass -> documentClass.render(print, indent) }
        usePackages.forEach { usePackage: UsePackage -> usePackage.render(print, indent) }
    }

    fun frame(frameTitle: String, vararg options: Pair<String, String>, init: Frame.() -> Unit) = initTag(Frame(frameTitle, joinOptionPairs(options)), init)

    fun documentClass(aClass: String, vararg options: String) {
        documentClasses.add(DocumentClass(aClass, options.toList()))
    }

    fun usepackage(aPackage: String, vararg options: String) {
        usePackages.add(UsePackage(aPackage, options.toList()))
    }
}

class DocumentClass(override val value: String?, override val options: List<String>) : TagWithText("documentclass")

class UsePackage(override val value: String?, override val options: List<String>) : TagWithText("usepackage")

fun document(init: Document.() -> Unit): Document {
    val document = Document()
    document.init()
    return document
}

fun joinOptionPairs(options: Array<out Pair<String, String>>): List<String> = options.toList().stream().map { t -> "${t.first}=${t.second}" }.toList()
