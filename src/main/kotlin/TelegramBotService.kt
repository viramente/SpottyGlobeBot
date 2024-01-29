import java.lang.IllegalStateException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class User(
    val id: Int?, // тут айди чата, оно же пользователя
    val is_bot: Boolean = false,
    val first_name: String?, // User's or bot's first name
    val username: String?, // User's or bot's first name
    val language_code: String? // IETF language tag
)

data class Chat(
    val id: Int?, // тут айди чата, оно же пользователя
    val first_name: String?, // First name of the other party in a private chat
    val type: String? // Type of chat, can be either “private”, “group”, “supergroup” or “channel”
)

data class Message(
    val message_id: Int? = 0, // тут айди сообщения
    val from: User = User(id = 0, is_bot = false, first_name = "", username = "", language_code = ""),
    val chat: Chat = Chat(id = 0, first_name = "", type = ""),
    val date: Int? = 0, // Date the message was sent in Unix time.
    val text: String? = "", // текст сообщения
)

fun createMessage(botToken: String, chat_id: Int, text: String): String { // chat_Id = Int or String : Message
    val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chat_id&text=$text"
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates: String = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun parseMessage(inputString: String): Message {

    val message_id: Int? = findProperty("\"message_id\":(.+?),", inputString)?.toInt()
    val from = User(
        id = findProperty("\"id\":(.+?),", inputString)?.toInt(),
        first_name = findProperty("\"first_name\":\"(.+?)\"", inputString),
        username = findProperty("\"username\":\"(.+?)\"", inputString),
        language_code = findProperty("\"language_code\":\"(.+?)\"", inputString),
    )
    val chat = Chat(
        id = findProperty("\"id\":(.+?),", inputString)?.toInt(),
        first_name = findProperty("\"first_name\":\"(.+?)\"", inputString),
        type = findProperty("\"type\":\"(.+?)\"", inputString),
    )
    val date: Int? = findProperty("\"date\":(.+?),", inputString)?.toInt()
    val text: String? = findProperty("\"text\":\"(.+?)\"", inputString)
    return Message(message_id, from, chat, date, text)

}

fun findProperty(mask: String, wholeString: String): String? {
    return mask.toRegex().find(wholeString)?.groups?.get(1)?.value
}