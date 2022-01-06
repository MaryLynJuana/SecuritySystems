package cli.commands

import context.Context
import journal.SecurityJournal

object WhoAmI : Command() {
    override fun execute(context: Context) {
        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Info,
            "whoami"
        )
        println(context.user?.name ?: error("User not defined"))
    }
}
