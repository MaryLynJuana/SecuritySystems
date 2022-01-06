package cli.commands

import context.Context
import fs.absolutePath
import fs.save
import journal.SecurityJournal
import users.*
import java.nio.file.Files

class Chmod(
    private val file: String?,
    private val username: String?,
    private val rights: String?
) : Command() {
    override fun execute(context: Context) {
        if (file == null || username == null) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "chmod: invalid input: username=$username, file=$file, rights=$rights"
            )
            println("Error: invalid format\nUsage: chown file username REWACO")
            return
        }

        if (context.findUser(username) == null) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "chmod: specified user not exists: $username"
            )
            println("Error: user $username does not exist!")
            return
        }

        val path = context.resolvePath(file)
        val abs = absolutePath(path)
        val fileRights = context.rightsFor(path)

        if (!Files.exists(abs)) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "chmod: specified path does not exist: $path"
            )
            println("Error: $path does not exist!")
            return
        }

        if (fileRights?.canOwn != true) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "chmod: permission denied for $path"
            )
            println("Error: permission denied")
            return
        }

        val rights = parseRights(rights)
        val r = RightRecord(username, rights)
        context.rightMap[path] = (context.rightMap[path]?.filterNot { it.user == username } ?: listOf()) + r
        context.save()

        val parent = path.substringBeforeLast('/')
        val parentRights = if (parent == "/" || parent == "") {
            Right.FULL
        } else {
            context.rightMap[parent]?.find { it.user == username }?.rights
        }

        if (parentRights == null || !parentRights.canRead || !parentRights.canExecute) {
            println("Granting RE permissions for $username to $parent")
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Info,
                "chmod: granting RE permission for $username to $parent"
            )
            Chmod(parent, username, "RE").execute(context)
            return
        }

        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Info,
            "chmod: permission changed: user=$username, path=$path, rights=$rights"
        )
    }
}
