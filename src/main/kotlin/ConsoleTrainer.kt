import java.io.File

fun main() {

    val dictionarySource = File("words.txt")

    dictionarySource.writeText(
        """
        hello|привет^0
        dog|собака^3
        cat|кошка^3
        ball|мяч^1
        girl|девочка^3
        house|дом^3
        pen|ручка^1
        door|дверь^3
   """.trimIndent()
    )

    val dictionary = mutableListOf<Word>()

    dictionarySource.forEachLine {
        val stringToParse = it.split("|", "^")
        dictionary.add(
            Word(
                stringToParse[0],
                stringToParse[1],
                stringToParse[2].toInt() ?: 0
            )
        )
    }

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")

        val input: String? = readlnOrNull()

        when (input) {
            "1" -> {
                val unlearnedWords = dictionary
                    .filter { it.correctAnswersCount < MATCHES_AMOUNT_TO_BECOME_LEARNED }

                if (unlearnedWords.isEmpty()) {
                    println("У вас нет невыученных слов!")
                    break
                } else {

                    val taskWord = unlearnedWords.random()

                    val wrongAnswers = (unlearnedWords - taskWord).toMutableList()

                    if (wrongAnswers.size < POSED_ANSWERS_AMOUNT - 1) {
                        val extraWrongAnswers = (dictionary - taskWord - wrongAnswers)
                            .shuffled()
                            .take(POSED_ANSWERS_AMOUNT - wrongAnswers.size)
                        wrongAnswers.addAll(extraWrongAnswers)
                    }

                    val answers = mutableListOf<String>()
                    answers.add(taskWord.translate)
                    answers.addAll(wrongAnswers.take(POSED_ANSWERS_AMOUNT - 1).map { it.translate })

                    println("Выберете перевод для слова: ${taskWord.original}")
                    println(
                        """
                        1 ${answers[0]}
                        2 ${answers[1]}
                        3 ${answers[2]}
                        4 ${answers[3]}
                        """.trimIndent()
                    )
                }

                continue
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

const val POSED_ANSWERS_AMOUNT = 4

const val MATCHES_AMOUNT_TO_BECOME_LEARNED = 3

fun getPercents(whole: Int, part: Int) = (100 * part) / whole

data class Word(val original: String, val translate: String, var correctAnswersCount: Int = 0)