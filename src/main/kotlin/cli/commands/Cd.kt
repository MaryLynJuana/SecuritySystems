package cli.commands

import context.Context
import fs.SYSTEM_DIR
import fs.absolutePath
import journal.SecurityJournal
import users.canExecute
import java.nio.file.Files

class Cd(private val directory: String?) : Command() {
    override fun execute(context: Context) {
        if (directory == "." || directory == null) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "cd: user specified invalid directory: $directory"
            )
            return
        }

        val path = context.resolvePath(directory)
        val rights = context.rightsFor(path)

        if (path != "/" && (rights == null || !rights.canExecute) || path == "/$SYSTEM_DIR") {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "cd: user specified danger directory: $path"
            )
            println("Error: no such directory: $directory")
            return
        }

        if (!Files.isDirectory(absolutePath(path))) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "cd: user specified not a directory: $path"
            )
            println("Error: $directory is not a directory!")
            return
        }

        context.workDir = path
        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Info,
            "cd: user changed directory to $path"
        )
    }
}
