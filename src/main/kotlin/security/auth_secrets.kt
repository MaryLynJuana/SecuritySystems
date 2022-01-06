package security

import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.tan

fun secretFun(a: Int, x: Int) = tan(a.toDouble() * x)

fun loadQuestions() = Files.readString(Path.of("res/questions.txt")).split('\n')

fun setUpSecretFun(): Int? {
    println(
        """
            You need to setup parameter ${"\u001B[1ma\u001B[0m"} for secret function
                ${"\u001B[1msqrt(x / a)\u001B[0m"}
            This parameter must be an integer and it must not be 0.
        """.trimIndent()
    )

    repeat(3) {
        print("Enter parameter a: ")
        val a = readLine()?.toIntOrNull()?.takeIf { it != 0 }

        if (a == null || a == 0) {
            println("Error, try again")
            return@repeat
        }

        return a
    }

    return null
}

fun setUpQuestions(): Map<String, String>? {
    println(
        """
            You need to setup secret questions (at least 2)
            Select the question from list below by typing it's number 
            and then type your answer.
            
            Type ${"\u001B[1mok\u001B[0m"} to confirm your choice.
        """.trimIndent()
    )

    val questions = loadQuestions().toMutableList()

    fun printQuestions() {
        println(questions.mapIndexed { i, q -> "(${i + 1}) $q" }.joinToString("\n"))
    }

    printQuestions()

    val questionsAndAnswers = mutableMapOf<String, String>()

    while (questions.isNotEmpty()) {
        print("Enter question number: ")
        val input = readLine()

        if (input == "ok") {
            if (questionsAndAnswers.size >= 2) {
                return questionsAndAnswers
            } else {
                println("Must set up at least 2 questions!")
                continue
            }
        }

        val n = input?.toIntOrNull()

        if (n == null || n !in 1..questions.size) {
            println("Error, try again.")
            continue
        }

        val q = questions[n - 1]

        println(q)
        print("\nEnter answer: ")
        val a = readLine()?.takeIf { it.isNotEmpty() }

        if (a == null) {
            println("Error, try again.")
            continue
        }

        questionsAndAnswers[q] = a

        questions.remove(q)

        if (questions.isNotEmpty()) {
            printQuestions()
        } else {
            return questionsAndAnswers
        }
    }

    return null
}
