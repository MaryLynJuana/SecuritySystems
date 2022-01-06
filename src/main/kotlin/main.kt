import cli.login
import cli.startCli
import fs.initApp

fun main() {
    val context = initApp()
    val loggedId = context.login()
    if (loggedId) context.startCli()
}
