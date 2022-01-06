package cli.commands

import context.Context
import fs.absolutePath
import journal.SecurityJournal
import users.canExecute
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

class Run(private val file: String?) : Command() {
    override fun execute(context: Context) {
        if (file == null) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "run: file not specified"
            )
            println("Error: File not specified!")
            return
        }

        val path = context.resolvePath(file)
        val abs = absolutePath(path)
        val rights = context.rightsFor(path)

        if (rights == null) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "run: file not exists or permission denied: $path"
            )
            println("Error: file does not exist")
            return
        }

        if (!rights.canExecute) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "run: permission denied: $path"
            )
            println("Error: permission denied")
            return
        }

        if (Files.isDirectory(abs)) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "run: $file is a directory"
            )
            println("Error: $file is a directory!")
            return
        }

        val perms = PosixFilePermissions.fromString("rwx------").let {
            PosixFilePermissions.asFileAttribute(it)
        }

        Files.setPosixFilePermissions(abs, perms.value())
        val command = "bash $abs"
        val process = Runtime.getRuntime().exec(command)
        val reader = process.inputStream.bufferedReader()
        reader.use {
            do { val line = it.readLine()?.let(::println) } while (line != null)
        }

        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Info,
            "run: $file"
        )
    }
}
