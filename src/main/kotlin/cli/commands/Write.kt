package cli.commands

import context.Context
import fs.absolutePath
import fs.save
import journal.SecurityJournal
import users.Right
import users.RightRecord
import users.canWrite
import java.nio.file.Files

class Write(private val file: String?, private val content: String?) : Command() {
    override fun execute(context: Context) {
        if (file == null) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "write: file not specified"
            )
            println("Error: File not specified!")
            return
        }

        val path = context.resolvePath(file)
        val abs = absolutePath(path)
        val rights = context.rightsFor(path.substringBeforeLast('/'))

        if (rights == null || !rights.canWrite) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "write: permission denied or parent directory does not exist"
            )
            println("Error: permission denied or parent directory does not exist")
            return
        }

        if (Files.isDirectory(abs)) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "write: $file is a directory"
            )
            println("Error: $file is a directory!")
            return
        }

        if (Files.notExists(abs)) {
            val user = context.user ?: error("User not specified")
            context.rightMap[path] = listOf(
                RightRecord(
                    user = user.name,
                    rights = Right.FULL
                )
            )

            abs.toFile().apply {
                createNewFile()
                setReadable(true)
                setExecutable(true)
                setWritable(true)
            }
            context.save()
        }

        abs.toFile().writeText(content ?: "")
        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Danger,
            "write: $path"
        )
    }
}
