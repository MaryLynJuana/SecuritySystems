package journal

import fs.BASE_DIR
import fs.SYSTEM_DIR
import kotlinx.datetime.Instant
import users.User
import java.io.File

class SecurityJournal(private val path: String = "$BASE_DIR/$SYSTEM_DIR/journal.log") {
    private val file = File(path)

    class Record(
        val user: User,
        val time: Instant,
        val securityLevel: SecurityLevel,
        val message: String
    ) {
        override fun toString(): String {
            return "$time [${securityLevel.name.uppercase()}] ${user.name}: $message\n"
        }
    }

    enum class SecurityLevel {
        Info, Warning, Danger
    }

    fun write(record: Record) {
        file.appendText(record.toString())
    }
}
