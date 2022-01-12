package users

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationJournal(val records: MutableList<Record>) {
    @Serializable
    data class Record(
        val username: String,
        val password: String,
        val secret: String,
        val isAdmin: Boolean = false,
        val questions: Map<String, String> = mapOf(),
        val key: Int = 0,
        val errors: Int = 0,
        val timestamp: Instant = Clock.System.now(),
    )

    fun isNotEmpty() = records.isNotEmpty()
}
