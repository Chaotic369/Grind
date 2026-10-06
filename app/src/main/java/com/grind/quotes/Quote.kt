package com.grind.quotes

data class Quote(val text: String, val author: String) {
    fun display() = "\u201C$text\u201D"
    fun shareText() = "\u201C$text\u201D \u2014 $author\n\nvia Grind \uD83D\uDD25"
}
