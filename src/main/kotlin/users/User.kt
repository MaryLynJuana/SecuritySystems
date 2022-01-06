package users

data class User(
    val name: String,
    val isAdmin: Boolean = false
) {
    companion object {
        fun from(record: RegistrationJournal.Record) = User(record.username, record.isAdmin)
    }
}
