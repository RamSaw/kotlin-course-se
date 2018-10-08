package ru.hse.spb

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

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

    fun exportToPDF(file: Path) {
        Files.write(file, toString().toByteArray())
        ProcessBuilder("pdflatex", file.toAbsolutePath().toString())
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor(5, TimeUnit.SECONDS)
    }
}

fun document(init: Document.() -> Unit): Document {
    val document = Document()
    document.init()
    return document
}