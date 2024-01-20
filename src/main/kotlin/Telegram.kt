import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    val urlGetMe = "https://api.telegram.org/bot$botToken/getMe"
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates"

    val client: HttpClient = HttpClient.newBuilder().build()

    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetMe)).build()

    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    // первый аргумент -- запрос, второй аргумент -- обработчик тела клиента

    println(response.body())
    // body() -- вызовет тело ответа в виде строки
}