import java.io.File
import java.lang.IllegalStateException

class Statistics(
    val learnedWordsAmount: Int,
    val total: Int,
    val percent: Int,
)

data class Question (
    val variants: List<Word>,
    val taskWord: Word
)

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

class LearnWordsTrainer (
    private val matchesAmountToBecomeLearned: Int = 3,
    private val countOfQuestionWords: Int = 4,
) {

    private var question: Question? = null

    private val dictionary = loadDictionary()

    fun getStatistic(): Statistics {
        val learnedWordsAmount: Int = dictionary.filter { it.correctAnswersCount >= matchesAmountToBecomeLearned }.size
        val total: Int = dictionary.size
        val percent: Int = getPercents(dictionary.size, learnedWordsAmount)
        return Statistics(learnedWordsAmount, total, percent)
    }

    fun getNextQuestion(): Question? {
        val unlearnedWords = dictionary
            .filter { it.correctAnswersCount < matchesAmountToBecomeLearned }
        if (unlearnedWords.isEmpty()) return null
        val variants = unlearnedWords.shuffled().take(POSED_ANSWERS_AMOUNT).toMutableList()

        val taskWord = variants.random()

        if (variants.size < POSED_ANSWERS_AMOUNT) {
            val extraWrongAnswers = (dictionary - variants)
                .shuffled()
                .take(POSED_ANSWERS_AMOUNT - variants.size)
            variants.addAll(extraWrongAnswers)
        }

        question = Question(variants, taskWord)

        return question
    }

    fun checkAnswer(userAnswerId: Int?): Boolean {

        return question?.let {
            val correctAnswerIndex = it.variants.indexOf(it.taskWord)
            if (userAnswerId == correctAnswerIndex) {
                it.taskWord.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false

    }

    private fun createInitialDictionary() {
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

    private fun saveDictionary(dictionary: MutableList<Word>) {
        val dictionarySource = File("words.txt")
        dictionarySource.writeText("")
        dictionary.forEach { dictionarySource.appendText("${it.original}|${it.translate}^${it.correctAnswersCount}\n") }
    }

    private fun loadDictionary(): MutableList<Word> {

        try {
            val dictionarySource = File("words.txt")
            if (!dictionarySource.exists()) createInitialDictionary()

            val dictionary = mutableListOf<Word>()

            dictionarySource.forEachLine {

                val stringToParse = it.split("|", "^")

                dictionary.add(
                    Word(
                        stringToParse[0],
                        stringToParse[1],
                        stringToParse[2].toIntOrNull() ?: 0
                    )
                )
            }
            return dictionary

        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException ("Некорректный файл словаря")
        }
    }

}

fun getPercents(whole: Int, part: Int) = (100 * part) / whole