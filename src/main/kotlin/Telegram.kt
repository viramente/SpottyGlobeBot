fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0L

    val trainer = LearnWordsTrainer()
    var question = trainer.getNextQuestion()

    while (true) {
        Thread.sleep(2000)

        val updates: String = TelegramBotService(botToken).getUpdates(updateId)
        println(updates)

        val maskForUpdates: Regex = "\"update_id\":(.+?),".toRegex()
        val updatesMessageGroup = maskForUpdates.findAll(updates).toList()

        if (updatesMessageGroup.isEmpty()) continue

        updateId = updatesMessageGroup.map { it.groupValues.last() }.last().toLong() + 1

        val inputRawMessages: List<String> =
            updates
                .split("{\"update_id\"")
                .drop(1)
                .map { "{\"update_id\"}$it" }

        val callbacks: List<Callback> = inputRawMessages.map { parseCallback(it) }

        for (eachCallback in callbacks) {

            println("eachCallback.massageText = ${eachCallback.massageText}")

            if (eachCallback.massageText.equals(MENU_STRING, true) ||
                eachCallback.massageText.equals(START_COMMAND, true) ||
                eachCallback.massageText.equals(START_COMMAND_RUS_UNICODE, true) ||
                eachCallback.callbackData.equals(MENU_CLICKED) ||
                eachCallback.callbackData.equals(STATISTICS_CLICKED) // UTF-16
            ) {

                if (eachCallback.callbackData.equals(STATISTICS_CLICKED)) {
                    TelegramBotService(botToken).sendMessage(
                        eachCallback.massageChat.chatId!!,
                        "Выучено%20${trainer.getStatistics().learnedWordsAmount}%20из%20${trainer.getStatistics().total}" +
                                "%20слов%20%7C%20${trainer.getStatistics().percent}%25"
                    )
                }

                TelegramBotService(botToken).sendMenu(
                    eachCallback.massageChat.chatId!!
                )
            }

            if (eachCallback.massageText.equals("hello", true)) {

                TelegramBotService(botToken).sendMessage(
                    eachCallback.massageChat.chatId!!,
                    "hello,+${eachCallback.massageFrom.messageUsername}!"
                )
            }

            if (eachCallback.callbackData != null) {

                if (eachCallback.callbackData.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {

                    val userAnswerId =
                        eachCallback.callbackData.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toIntOrNull()

                    when {

                        trainer.checkAnswer(userAnswerId?.minus(1)) -> {
                            TelegramBotService(botToken).sendMessage(
                                eachCallback.massageChat.chatId!!,
                                "Верно!"
                            )
                        }

                        else -> {

                            TelegramBotService(botToken).sendMessage(
                                eachCallback.massageChat.chatId!!,
                                "Неверно!%20${question?.taskWord?.original}%20–%20${question?.taskWord?.translate}"
                            )
                        }
                    }
                }
            }

            if (eachCallback.callbackData != null
                && (eachCallback.callbackData == LEARN_WORDS_CLICKED ||
                        eachCallback.callbackData.startsWith(CALLBACK_DATA_ANSWER_PREFIX))
            ) {

                question = trainer.getNextQuestion()

                if (question != null) {

                    TelegramBotService(botToken).checkNextQuestionAndSend(
                        eachCallback.massageChat.chatId!!,
                        question
                    )

                } else {

                    TelegramBotService(botToken).sendMessage(
                        eachCallback.massageChat.chatId!!,
                        NO_UNKNOWN_WORDS
                    )

                    return
                }
            }
        }
    }
}