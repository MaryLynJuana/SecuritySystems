package cli.commands

import context.Context
import journal.SecurityJournal

object Logout : Command() {
    override fun execute(context: Context) {
        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Info,
            "logout"
        )
        context.logout()
    }
}
