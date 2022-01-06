package cli.commands

import context.Context
import fs.CONTEXT_FILE
import fs.SYSTEM_DIR
import fs.absolutePath
import fs.relativePath
import journal.SecurityJournal
import users.*
import java.nio.file.Files

class Ls(private val directory: String? = null) : Command() {
    override fun execute(context: Context) {
        val dir = directory?.let(context::resolvePath) ?: context.workDir
        val path = absolutePath(dir)

        if (!Files.isDirectory(path)) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "ls: $dir is not a directory"
            )
            println("Error: $dir is not a directory!")
            return
        }

        if (dir == context.workDir) {
            val currentDirRights = context.rightsFor(dir)
            println(fileString(context, context.workDir, currentDirRights))
        }

        Files.list(path)
            .map(::relativePath)
            .filter { it != "/$SYSTEM_DIR" }
            .map { it to context.rightsFor(it) }
            .let { files ->
                if (context.user?.isAdmin != true) {
                    files.filter { (_, r) -> r?.canRead == true }
                } else {
                    files
                }
            }.sorted { o1, o2 ->
                o1.first.compareTo(o2.first)
            }.forEach { (file, rights) ->
                println(fileString(context, file, rights))
            }

        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Info,
            "ls: $dir"
        )
    }

    private fun Context.normalizeUsername(username: String) = " ".repeat(
        registrationJournal.records.maxOf { it.username.length } - username.length
    ) + username

    private fun fileString(context: Context, file: String, rights: Int?): String {
        val owner = context.findOwner(file)
        val normalUsername = context.normalizeUsername(owner.name)

        val rightsString = rights?.rightString ?: if (context.user?.isAdmin == true) {
            Right.FULL.rightString
        } else {
            Right.EMPTY.rightString
        }

        val fileName = if (file == context.workDir) "." else file.substringAfterLast('/')
        val type = if (Files.isDirectory(absolutePath(file))) "\u001B[1m" else ""

        return "$type$rightsString\t$normalUsername\t$fileName\u001B[0m"
    }
}
