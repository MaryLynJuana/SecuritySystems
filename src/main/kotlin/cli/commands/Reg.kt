package cli.commands

import context.Context
import journal.SecurityJournal
import users.RegistrationJournal

object Reg : Command() {
    override fun execute(context: Context) {
        val isAdmin = context.user?.isAdmin == true

        if (!isAdmin) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "mkdir: permission denied"
            )
            println("Error: You must be admin to execute this command")
            return
        }

        if (context.registrationJournal.records.size >= 3) {
            println("Error: No more users allowed for system")
            return
        }

        print("Login: ")
        val username = readLine()?.trim() ?: ""
        if (!isUsernameValid(username)) {
            println("Error: username must contain only letters and digits")
            return
        }

        val console = System.console() ?: error("Cannot obtain console")
        val password = console.readPassword("Enter password: ").let(::String)
        if (!isPasswordValid(password)) {
            println("""
                Error: password must be 7 chars long, contain one digit,
                one lowercase letter, one uppercase letter, one special symbol.
            """.trimIndent())
            return
        }

        context.register(RegistrationJournal.Record(username, password))
        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Info,
            "reg: created user $username"
        )
    }

    private fun isUsernameValid(username: String) = username.isNotBlank() && username.all { it.isLetterOrDigit() }
    private fun isPasswordValid(password: String) = password.length == 7 &&
        password.contains(Regex("[A-Z]")) && password.contains(Regex("[a-z]")) &&
        password.contains(Regex("[0-9]")) && password.contains(Regex("[-_+*%$@\".,!~^&?=<>;'`]"))
}
