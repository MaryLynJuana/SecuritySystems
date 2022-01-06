package cli.commands

import context.Context
import fs.save
import journal.SecurityJournal
import users.*

class Share(
    private val directory: String? = null,
    private val rights: String? = null,
) : Command() {
    override fun execute(context: Context) {
        val dir = directory?.let(context::resolvePath) ?: context.workDir

        val dirRights = context.rightsFor(dir)
        if (dirRights == null || !dirRights.canControl) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Danger,
                "share: $dir doest not exist or permission denied"
            )
            println("Error: Path does not exist or permission denied")
            return
        }

        if (dir.trim('/').contains('/')) {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "share: can share only discs in root: $dir"
            )
            println("Error: Can share only discs in root")
            return
        }

        context.sharedParent(dir)?.let {
            context.writeLogForUser(
                SecurityJournal.SecurityLevel.Warning,
                "share: $dir is already shared because $it is shared"
            )
            println("$dir is already shared because $it is shared")
            return
        }

        context.share(dir, parseRights(rights))
        context.writeLogForUser(
            SecurityJournal.SecurityLevel.Info,
            "share: $dir"
        )
    }

    private fun Context.sharedParent(path: String) = shared.keys.find { path.startsWith(it) }

    private fun Context.share(path: String, rights: Int) {
        shared.filterKeys { it.startsWith(path) }.forEach { (k, _) -> shared.remove(k) }
        shared[path] = rights
        save()
    }
}
