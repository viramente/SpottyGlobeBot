import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

data class User(
    val messageId: Long?,
    val is_bot: Boolean = false,
    val messageFirstName: String?, // User's or bot's first name
    val messageUsername: String?,
    val messageLanguageCode: String? // IETF language tag
)

data class Chat(
    val chatId: Long?,
    val chatFirstName: String?, // First name of the other party in a private chat
    val chatUsername: String?,
    val chatType: String? // Type of chat, can be either “private”, “group”, “supergroup” or “channel”
)

data class Callback(
    val callback_id: String? = "",
    val messageId: Long? = 0L, // тут айди сообщения
    val massageFrom: User = User(messageId = 0L, is_bot = false, messageFirstName = "", messageUsername = "", messageLanguageCode = ""),
    val massageChat: Chat = Chat(chatId = 0, chatFirstName = "", chatUsername = "", chatType = ""),
    val massageDate: Long? = 0L, // Date the message was sent in Unix time.
    val massageText: String? = "", // текст сообщения
    val callbackChatInstance: String? = "",
    val callbackData: String? = ""
)

fun sendMessage(botToken: String, chatId: Long, message: String): String { // chat_Id = Int or String : Message
    val encoded = URLEncoder.encode(
        message, StandardCharsets.UTF_8
    )
    val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$message"
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun sendMenu(botToken: String, chatId: Long): String {
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val sendMenuBody = """
{
    "chat_id": "$chatId",
    "text": "Основное меню",
    "reply_markup": {
        "inline_keyboard": [
            [
                {
                    "text":"Изучить слова",
                    "callback_data": "learn_words_clicked"
                },
                {
                    "text":"Статистика",
                    "callback_data": "statistics_clicked"
                }
            ]
        ]
    }
}
    """.trimIndent()

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
        .header("Content-type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
        .build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun checkNextQuestionAndSend(botToken: String, chatId: Long, question: Question): String {
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"

    val answers = formatStringForQuestion(question.variants)

    val sendMenuBody = """
{
    "chat_id": "$chatId",
    "text": "Выберите перевод для слова:\n${question.taskWord.original}",
    "reply_markup": {
        "inline_keyboard": [
            $answers
        ]
    }
}
    """.trimIndent()

    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
        .header("Content-type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
        .build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

//- В качестве поля text укажи корректный ответ на английском.
//- Для поля inline_keyboard необходимо создать json-объект из вариантов ответа,
// при этом поле text каждой кнопки будет содержать: перевод варианта ответа,
// а callback_data конкатенацию константы CALLBACK_DATA_ANSWER_PREFIX и индекса варианта ответа.
// Для получения индекса варианта рекомендуется использовать forEachIndexed или mapIndexed
// при работе со списком вариантов.



fun getUpdates(botToken: String, updateId: Long): String {
    val urlGetUpdates: String = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun parseCallback(inputString: String): Callback {

    val callbackQueryId: String? = findProperty(changeCurlyBraces("\"callback_query\"(.+?),"), inputString)?.filter { it.isDigit() }

    val messageId: Long? = findProperty(changeCurlyBraces("\"message_id\":(.+?),"), inputString)?.filter { it.isDigit() }?.toLong()

    val messageFromObject = findProperty(changeCurlyBraces("\"message_id\"(.+?)\"chat\""), inputString)!!

    val messageFrom = User(
        messageId = findProperty(changeCurlyBraces("\"id\":(.+?),"), messageFromObject)?.filter { it.isDigit() }?.toLong(),
        messageFirstName = findProperty("\"first_name\":\"(.+?)\"", messageFromObject),
        messageUsername = findProperty("\"username\":\"(.+?)\"", messageFromObject),
        messageLanguageCode = findProperty("\"language_code\":\"(.+?)\"", messageFromObject), // у ботов нет
    )

    val messageChatFromObject = findProperty(changeCurlyBraces("\"chat\"(.+?)\"date\""), inputString)!!

    val messageChat = Chat(
        chatId = findProperty(changeCurlyBraces("\"id\":(.+?),"), messageChatFromObject)?.filter { it.isDigit() }?.toLong(),
        chatFirstName = findProperty("\"first_name\":\"(.+?)\"", messageChatFromObject),
        chatUsername = findProperty("\"username\":\"(.+?)\"", messageChatFromObject),
        chatType = findProperty("\"type\":\"(.+?)\"", messageChatFromObject),
    )

    val messageDate: Long? = findProperty("\"date\":(.+?),", inputString)?.filter { it.isDigit() }?.toLong()

    val messageText: String? = findProperty("\"text\":\"(.+?)\"", inputString)
    // TODO: позже переделать, чтобы не спуталось с текстом в reply_markup; пока привязка тупо позиционная;
    // возможно, можно решить через подсчет скобок, но прямо сейчас пока работает. СЛЕДИТЬ ЗА КОНТЕКСТОМ

    val callbackChatInstance: String? = findProperty(changeCurlyBraces("\"chat_instance\":(.+?),"), inputString)
    val callbackData = findProperty(changeCurlyBraces("\"data\":\"(.+?)\""), inputString)

    return Callback(
        callbackQueryId,
        messageId,
        messageFrom,
        messageChat,
        messageDate,
        messageText,
        callbackChatInstance,
        callbackData
    )

}

const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val MENU_CLICKED = "menu_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val NO_UNKNOWN_WORDS = "У%20вас%20нет%20невыученных%20слов!"
const val START_COMMAND = "/start"
const val MENU_STRING = "menu"
const val START_STRING = "старт"// "\u0441\u0442\u0430\u0440\u0442"

fun String.substringAfter(string: String) = this.drop(string.length)

fun findProperty(mask: String, wholeString: String): String? {
    return mask
        .toRegex()
        .find(changeCurlyBraces(wholeString))
        ?.groups?.get(1)
        ?.value
        ?.filterNot { it == ' '}
}

fun changeCurlyBraces(string: String) =
    string
        .replace('{', '<')
        .replace('}', '>')

fun formatStringForQuestion(variants: List<Word>): String {
    val answers = variants.mapIndexed() { index, word ->
        "[{\"text\":\"${word.translate}\",\"callback_data\": \"$CALLBACK_DATA_ANSWER_PREFIX${index+1}\"}]"
    }.joinToString(",")
    val menuButton = ",[{\"text\":\":: Меню ::\",\"callback_data\": \"menu_clicked\"}]"
    return answers + menuButton
}