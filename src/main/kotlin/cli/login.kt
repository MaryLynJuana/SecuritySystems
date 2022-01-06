package cli

import context.Context
import security.setUpQuestions
import security.setUpSecretFun

fun Context.login(): Boolean {
    fun tryLogin(attemptsLeft: Int): Boolean {
        if (attemptsLeft <= 0) return false

        print("Enter username: ")
        val username = readLine() ?: error("Cannot obtain username")
        val console = System.console() ?: error("Cannot obtain console")
        val password = console.readPassword("Enter password: ").let(::String)

        return try {
            login(username, password)
            true
        } catch (e: IllegalStateException) {
            println(e.message)
            tryLogin(attemptsLeft - 1)
        }
    }

    if (!tryLogin(1)) {
        return false
    }

    var record = registrationRecord ?: run {
        println("Cannot find registration record")
        return false
    }

    if (record.isAdmin) {
        return true
    }

    if (record.errors >= 5) {
        println("This user need to be re-registered by the admin")
        return false
    }

    var changedRecord = false

    if (record.key == 0) {
        val a = setUpSecretFun() ?: return false
        record = record.copy(key = a)
        changedRecord = true
    }

    if (record.questions.isEmpty()) {
        val questions = setUpQuestions() ?: return false
        record = record.copy(questions = questions)
        changedRecord = true
    }

    if (changedRecord) update(record)

    return true
}
