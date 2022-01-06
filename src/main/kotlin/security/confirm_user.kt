package security

import context.Context
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

fun interface Authenticator {
    fun authenticate(context: Context): Boolean
}

fun authenticator(): Authenticator {
    var lastTime = Clock.System.now()

    return Authenticator {
        if (it.user?.isAdmin == true) {
            return@Authenticator true
        }

        val minutes = lastTime.until(Clock.System.now(), DateTimeUnit.MINUTE)

        if (minutes < 3) {
            true
        } else {
            val result = if (Random(lastTime.epochSeconds).nextBoolean()) {
                confirmSecretFunction(it)
            } else {
                confirmSecretQuestion(it)
            }

            if (result) {
                lastTime = Clock.System.now()
            }

            result
        }
    }
}

fun confirmSecretFunction(context: Context): Boolean {
    val key = context.registrationRecord?.key ?: error("User is not logged in")
    val argument = (-100_000..100_000).random()
    val expectedAnswer = sqrt(key.toDouble() / argument)

    repeat(2) {
        print("Authentication needed. Enter f($argument): ")
        val answer = readLine()?.toDoubleOrNull() ?: run {
            println("Invalid answer, try again")
            return@repeat
        }

        if (abs(expectedAnswer - answer) <= 0.01) {
            return true
        }

        println("Invalid answer, try again")
    }

    return false
}

fun confirmSecretQuestion(context: Context): Boolean {
    val questions = context.registrationRecord?.questions ?: error("User is not logged in or answers are not set")

    val q = questions.keys.random()
    val expectedAnswer = questions[q] ?: error("Answer is null")

    println("Authentication needed. Enter answer to following question: ")
    println(q)

    repeat(2) {
        print("Answer: ")
        val answer = readLine()

        if (answer == expectedAnswer) {
            return true
        }

        println("Invalid answer, try again.")
    }

    return false
}
