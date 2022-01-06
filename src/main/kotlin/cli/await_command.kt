package cli

import cli.commands.*
import context.Context

private fun awaitCommand(): Command? {
    print("> ")
    val command = readLine()
        ?.split(' ')
        ?.filter(String::isNotBlank)
        ?: return null

    val name = command.first()
    val args = command.drop(1)

    return when (name) {
        // GENERAL
        "whoami" -> WhoAmI
        "pwd" -> Pwd
        "logout" -> Logout
        "exit" -> Exit
        "reg" -> Reg

        // R
        "ls" -> Ls(args.firstOrNull())
        "cat" -> Cat(args.firstOrNull())

        // E
        "cd" -> Cd(args.firstOrNull())
        "run" -> Run(args.firstOrNull())

        // W
        "set" -> Write(args.firstOrNull(), args.drop(1).joinToString(" "))

        // A
        "add" -> Add(args.firstOrNull(), args.drop(1).joinToString(" "))

        // C
        "mkdir" -> Mkdir(args.firstOrNull())
        "share" -> Share(args.firstOrNull())

        // O
        "chmod" -> Chmod(args.getOrNull(0), args.getOrNull(1), args.getOrNull(2))

        else -> Unknown
    }
}

fun Context.startCli() {
    while (true) {
        awaitCommand()?.let {
            val isAuthenticated = authenticator.authenticate(this)

            if (isAuthenticated) {
                it.execute(this)
            } else {
                addError()
                logout()
            }
        } ?: run {
            println("Cannot read command input, exiting")
            return
        }
    }
}
