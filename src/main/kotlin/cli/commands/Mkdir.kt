package cli.commands

import context.Context
import fs.absolutePath
import fs.save
import journal.SecurityJournal
import users.Right
import users.RightRecord
import users.canControl
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

class Mkdir(private val dirname: String?) : Command() {
    override fun execute(context: Context) {
        if (dirname == null) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "mkdir: directory name not specified"
            )
            println("Error: Directory name not specified!")
            return
        }

        val path = context.resolvePath(dirname)
        val abs = absolutePath(path)
        val rights = context.rightsFor(path.substringBeforeLast('/'))

        if (rights == null || !rights.canControl) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "mkdir: permission denied or parent directory does not exist: $path"
            )
            println("Error: permission denied or parent directory does not exist")
            return
        }

        if (Files.exists(abs)) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "mkdir: $path already exists"
            )
            println("Error: $dirname already exists!")
            return
        }

        val user = context.user ?: error("User not specified")

        val perms = PosixFilePermissions.fromString("rwx------").let {
            PosixFilePermissions.asFileAttribute(it)
        }

        Files.createDirectory(abs, perms)

        context.rightMap[path] = listOf(
            RightRecord(
                user = user.name,
                rights = Right.FULL
            )
        )

        context.save()

        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Danger,
            "mkdir: created directory $path"
        )
    }
}
