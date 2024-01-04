import java.io.File

fun main() {

    if (!File("words.txt").exists()) createInitialDictionary()

    val dictionarySource = File("words.txt")

    val dictionary = mutableListOf<Word>()

    dictionarySource.forEachLine {

        val stringToParse = it.split("|", "^")

        val rightAnswersAmount = try {
            stringToParse[2].toInt()
        } catch (e: NumberFormatException) {
            0
        }

        dictionary.add(
            Word(
                stringToParse[0],
                stringToParse[1],
                rightAnswersAmount
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

                    val answers = unlearnedWords.shuffled().take(POSED_ANSWERS_AMOUNT).toMutableList()

                    val taskWord = answers.random()

                    if (answers.size < POSED_ANSWERS_AMOUNT) {
                        val extraWrongAnswers = (dictionary - answers)
                            .shuffled()
                            .take(POSED_ANSWERS_AMOUNT - answers.size)
                        answers.addAll(extraWrongAnswers)
                    }

                    println("Выберете перевод для слова: ${taskWord.original}")

                    answers.forEach { println("${answers.indexOf(it) + 1} ${it.translate}") }
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

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun createInitialDictionary() {
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

}