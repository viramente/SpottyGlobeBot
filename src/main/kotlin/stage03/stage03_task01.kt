package stage03

import java.io.File

fun main() {

    val dictionarySource = File("words.txt")

    val dictionary = mutableListOf<WordST3>()

    dictionarySource.forEachLine {
        val stringToParse = it.split("|", "^")
        dictionary.add(WordST3(stringToParse[0], stringToParse[1], stringToParse[2].toInt() ?: 0))
    }

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")

        val input: String? = readlnOrNull()

        when (input) {
            "1" -> {
                println("Учим слова")
                continue
                // TODO() дописать функционал позже
            }

            "2" -> {
                val learnedWordsAmount = dictionary.filter { it.correctAnswersCount >= 3 }.size

                println(
                    "Выучено $learnedWordsAmount из ${dictionary.size} слов " +
                            "| ${getPercents(dictionary.size, learnedWordsAmount)}%"
                )
                continue
            }

            "0" -> break

        }

        println("Вам следует выбрать пункт меню.")
        continue
    }

}

fun getPercents(whole: Int, part: Int) = (100 * part) / whole

data class WordST3(val original: String, val translate: String, var correctAnswersCount: Int = 0)