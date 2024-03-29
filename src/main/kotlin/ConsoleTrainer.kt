fun main() {

    val trainer = try {
        LearnWordsTrainer(matchesAmountToBecomeLearned = 3, countOfQuestionWords = 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {

        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")

        val input: String? = readlnOrNull()

        when (input) {
            "1" -> {

                val question = trainer.getNextQuestion()

                if (question == null) {
                    println("У вас нет невыученных слов!")
                    break
                }

                println("Выберете перевод для слова: ${question.taskWord.original}")
                question.variants.forEach { println("${question.variants.indexOf(it) + 1} ${it.translate}") }
                println("0 Меню")

                val userAnswerId = readln().toIntOrNull()

                when {

                    userAnswerId == 0 -> continue

                    trainer.checkAnswer(userAnswerId?.minus(1)) -> {
                        println("Верно!")
                        continue
                    }

                    else -> {
                        println("Ответ неверный. Верный ответ: ${question.taskWord.translate}")
                        continue
                    }
                }
            }

            "2" -> {
                val statistics = trainer.getStatistics()
                println(
                    "Выучено ${statistics.learnedWordsAmount} из ${statistics.total} слов " +
                            "| ${statistics.percent}%"
                )
            }

            "0" -> break

            else -> {
                println("Вам следует выбрать пункт меню.")
            }
        }

        continue
    }
}