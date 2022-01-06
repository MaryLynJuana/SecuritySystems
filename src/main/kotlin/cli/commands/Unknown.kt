package cli.commands

import context.Context

object Unknown : Command() {
    override fun execute(context: Context) {
        println("Error: Unknown command!")
    }
}
