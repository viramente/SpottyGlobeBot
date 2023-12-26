package stage01

import java.io.File

fun main() {

    val vocabulary: File = File("words.txt")
    vocabulary.writeText("""
            hello привет
            dog собака
            cat кошка
        """.trimIndent())

    vocabulary.readLines().forEach { println(it) }
}
