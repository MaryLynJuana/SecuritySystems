package cli.commands

import context.Context

sealed class Command {
    abstract fun execute(context: Context)
}
