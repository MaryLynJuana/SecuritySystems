package cli.commands

import context.Context
import fs.absolutePath
import journal.SecurityJournal
import users.canRead
import java.nio.file.Files

class Cat(private val file: String?) : Command() {
    override fun execute(context: Context) {
        if (file == null) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "cat: user have not specified path"
            )
            println("Error: File not specified!")
            return
        }

        val path = context.resolvePath(file)
        val abs = absolutePath(path)
        val rights = context.rightsFor(path)

        if (rights == null || Files.notExists(abs)) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "cat: user tries read non-existent file or file without proper permissions: $path"
            )
            println("Error: file does not exist")
            return
        }

        if (!rights.canRead) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "cat: user tries read file without R permission"
            )
            println("Error: permission denied")
            return
        }

        if (Files.isDirectory(abs)) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "cat: user tries to read directory"
            )
            println("Error: $file is a directory!")
            return
        }

        println(Files.readString(abs))
        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Info,
            "cat: user read file $path"
        )
    }
}
