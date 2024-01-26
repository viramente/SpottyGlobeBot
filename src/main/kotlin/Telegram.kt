
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0

    while(true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val mask: Regex = "\"update_id\":(.+?),".toRegex()
        val messageGroup = mask.findAll(updates).toList()

        if (messageGroup.isEmpty()) continue
        updateId = messageGroup.map {it.groupValues.last()}.last().toInt() + 1

        // ВЫВОД ТЕКСТА (ВСЕХ СТРОК АПДЕЙТА)
        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult: Sequence<MatchResult> = messageTextRegex.findAll(updates)
        val groups: Sequence<String> = matchResult.map {it.groupValues.last()}
        groups.forEach { println(it) }

        createMessage(botToken = botToken, chat_id = 316962688, text = "Hello") // чат айди я позже пропишу

    }
}

fun createMessage(botToken: String, chat_id: Int, text: String) { // caht_Id = Int or String : Message

    val urlSendMessage = "https://api.telegram.org/$botToken/sendMessage?chat_id=$chat_id,text=$text"
    // куда вписывать chat_id и text?
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    // но в ответ же нужно Message получить, как это прописать?
}


fun getUpdates(botToken: String, updateId: Int): String {

    val urlGetUpdates: String = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

    return response.body()
}

class User (
    val id: Int, // тут айди чата, оно же пользователя
    val is_bot: Boolean = false,
    val first_name: String, // User's or bot's first name
    val language_code: String// IETF language tag
)

class Chat(
    val id: Int, // тут айди чата, оно же пользователя
    val first_name: String, // First name of the other party in a private chat
    val type: String // Type of chat, can be either “private”, “group”, “supergroup” or “channel”
)

class MessageEntity(
    val offset: Int, // Offset in UTF-16 code units to the start of the entity
    val length: Int,
    val type: String, // Currently, can be “mention” (упоминание), “hashtag”, “cashtag” (валюта),
    // “bot_command”, “url”, “email”, “phone_number”,
    // “bold”, “italic”, “underline”, “strikethrough”, “spoiler”, “blockquote”, “code” (моноширинная строка),
    // “pre” (моноширинный блок), “text_link” (ссылка),
    // “text_mention” (для пользователей без имени), “custom_emoji” (стикеры)
)

class Message() {
    val message_id: Int = 0 // тут айди сообщения
    val from = User (id = 0, is_bot = false, first_name = "", language_code = "")
    val chat = Chat(id = 0, first_name = "", type = "")
    val date: Int = 0 // Date the message was sent in Unix time.
    val text: String = "" // текст сообщения
    val entities: Array<MessageEntity> = emptyArray()
}
