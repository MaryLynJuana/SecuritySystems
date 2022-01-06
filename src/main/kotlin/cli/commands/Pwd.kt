package cli.commands

import context.Context
import journal.SecurityJournal
import users.canRead

object Pwd : Command() {
    override fun execute(context: Context) {
        val rights = context.rightsFor(context.workDir)

        if (rights?.canRead == true) {
            println(context.workDir)
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Info,
                "pwd: ${context.workDir}"
            )
        } else {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "pwd: permission denied: ${context.workDir}"
            )
            println("Permission denied")
        }
    }
}
