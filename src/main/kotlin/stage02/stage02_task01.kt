package stage02

import java.io.File

fun main() {

    val dictionarySource: File = File("words.txt")

    val dictionary = mutableListOf<Word>()

    dictionarySource.forEachLine {
        val stringToParse = it.split("|", "^")
        dictionary.add(Word(stringToParse[0], stringToParse[1], stringToParse[2].toInt() ?: 0))
    }

    dictionary.forEach { println(it) }
}

data class Word(val original: String, val translate: String, var correctAnswersCount: Int = 0)
