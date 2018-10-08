package ru.hse.spb

interface Element {
    fun render(builder: StringBuilder, indent: String)
}

class TextElement(private val text: String) : Element {
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent$text\n")
    }
}

@DslMarker
annotation class TexTagMarker

@TexTagMarker
abstract class Tag(private val name: String) : Element {
    val children = arrayListOf<Element>()
    abstract val value: String?
    val attributes = hashMapOf<String, String>()

    protected fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent\\$name[${renderAttributes()}]${if (value == null) "" else "{$value}"}\n")
        for (c in children) {
            c.render(builder, "$indent  ")
        }
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
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

class Document : TagWithText("document") {
    override var value: String? = null

    fun documentClass(aClass: String, vararg options: String) {
        initTag(DocumentClass(aClass)) {}
    }

    fun usepackage(init: UsePackage.() -> Unit) = initTag(UsePackage(null), init)

    fun frame(init: Frame.() -> Unit) = initTag(Frame(null), init)
}

class DocumentClass(override val value: String?) : TagWithText("documentClass")

class UsePackage(override val value: String?) : TagWithText("usepackage")

class Frame(override val value: String?) : TagWithText("frame")

fun document(init: Document.() -> Unit): Document {
    val document = Document()
    document.init()
    return document
}