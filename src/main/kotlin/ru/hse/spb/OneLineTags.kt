package ru.hse.spb

class DocumentClass(override val value: String?, override val options: List<String>) : TagWithText("documentclass")

class UsePackage(override val value: String?, override val options: List<String>) : TagWithText("usepackage")
