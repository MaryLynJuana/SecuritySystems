package cli.commands

import context.Context
import kotlin.system.exitProcess

object Exit : Command() {
    override fun execute(context: Context) {
        exitProcess(0)
    }
}
