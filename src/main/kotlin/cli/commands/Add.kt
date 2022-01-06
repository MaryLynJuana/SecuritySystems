package cli.commands

import context.Context
import fs.absolutePath
import journal.SecurityJournal
import users.canAppend
import users.canWrite
import java.nio.file.Files

class Add(private val file: String?, private val content: String?) : Command() {
    override fun execute(context: Context) {
        if (file == null) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Info,
                "add: user failed to specify file name"
            )
            println("Error: File not specified!")
            return
        }

        val path = context.resolvePath(file)
        val abs = absolutePath(path)
        val rights = context.rightsFor(path)

        if (Files.notExists(abs)) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "add: user tries to append non-existing file"
            )
            println("Error: file not exists")
            return
        }

        if (rights == null || !(rights.canAppend || rights.canWrite)) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "add: user tries to append file without proper permissions"
            )
            println("Error: permission denied")
            return
        }

        if (Files.isDirectory(abs)) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "add: user tries to execute command on directory"
            )
            println("Error: $file is a directory!")
            return
        }

        if (content.isNullOrBlank()) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "add: user tries to append file with empty content"
            )
            return
        }

        abs.toFile().appendText(content)
        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Info,
            "add: user tries to appended file $path"
        )
    }
}
