

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0

    //1618475026
    //316962688
    //createMessage(botToken = botToken, chat_id = 316962688, text = "Hello")

    while(true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val maskForUpdates: Regex = "\"update_id\":(.+?),".toRegex()
        val updatesMessageGroup = maskForUpdates.findAll(updates).toList()

        if (updatesMessageGroup.isEmpty()) continue

        updateId = updatesMessageGroup.map {it.groupValues.last()}.last().toInt() + 1

        val inputRawMessages: List<String> = updates.split(":[{", "}}").dropLast(1).drop(1)

        val messages: List<Message> = inputRawMessages.map { parseMessage(it) }

        for (eachMessage in messages) {
            if (eachMessage.text.equals("hello", true)) {
                createMessage(
                    botToken,
                    eachMessage.chat.id!!,
                    "hello,+${eachMessage.from.username}!")
            }
        }
    }
}

