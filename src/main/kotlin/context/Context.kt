package context

import cli.login
import fs.*
import journal.SecurityJournal
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import security.authenticator
import users.*
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import kotlin.system.exitProcess

@Serializable
class Context(
    @Transient var workDir: String = "/",
    @Transient var user: User? = null,
    val registrationJournal: RegistrationJournal,
    val rightMap: RightMap,
    val shared: MutableMap<String, Int>,
) {
    val admin: User get() = registrationJournal.records.first { it.isAdmin }.let { User.from(it) }
    val registrationRecord: RegistrationJournal.Record? get() = user?.let { currentUser ->
        registrationJournal.records.find { it.username == currentUser.name }
    }

    @Transient
    val authenticator = authenticator()

    @Transient
    private val securityJournal = SecurityJournal()


    companion object {
         val DEFAULT_CONTEXT: Context get() = Context(
            rightMap = mutableMapOf(
                "/$DISK_A" to listOf(RightRecord(user = "a", rights = Right.FULL)),
               "/$DISK_B" to listOf(RightRecord(user = "b", rights = Right.FULL)),
            ),
            registrationJournal = RegistrationJournal(
                mutableListOf(
                    RegistrationJournal.Record(
                        username = "a",
                        password = "a",
                        questions = mapOf(),
                        key = 0,
                    ),
                    RegistrationJournal.Record(
                        username = "b",
                        password = "b",
                        questions = mapOf(),
                        key = 0,
                    ),
                    RegistrationJournal.Record(
                        username = "admin",
                        password = "admin",
                        questions = mapOf(),
                        key = 0,
                        isAdmin = true
                    )
                )
            )

            val shared = mutableMapOf(
                "/$DISK_C" to Right.SHARED,
                "/$DISK_D" to Right.Read.right,
            )

            return Context(rightMap = rightMap, registrationJournal = journal, shared = shared)
        }
    }

    fun rightsFor(path: String) = if (user?.isAdmin == true) {
        Right.FULL
    } else {
        val userRights = rightMap.rightsFor(path, user?.name ?: error("User not defined"))
        val sharedRights = shared.keys.find { path.startsWith(it) }?.let { shared[it] }

        when {
            userRights != null && sharedRights != null -> userRights or sharedRights
            userRights != null -> userRights
            else -> sharedRights
        }
    }

    fun resolvePath(path: String): String = when {
        path.startsWith('/') -> path
        path == ".." -> workDir.substringBeforeLast('/').takeIf(String::isNotBlank) ?: "/"
        else -> workDir.let { if (it.endsWith('/')) it else "$it/" } + path
    }

    fun login(username: String, password: String) {
        user = registrationJournal.records.find {
            it.username == username && it.password == password
        }?.let {
            User(it.username, it.isAdmin)
        } ?: error("Invalid username or password")
    }

    fun findUser(username: String): User? = registrationJournal.records.find {
        it.username == username
    }?.let {
        User(it.username, it.isAdmin)
    }

    fun findOwner(path: String): User = registrationJournal.records
        .find { rightMap.rightsFor(path, it.username)?.canOwn == true }
        ?.let { User.from(it) }
        ?: admin

    fun register(record: RegistrationJournal.Record) {
        registrationJournal.records.add(record)

        val relative = "/${record.username}"
        val path = absolutePath(relative)

        val perms = PosixFilePermissions.fromString("rwx------").let {
            PosixFilePermissions.asFileAttribute(it)
        }

        Files.createDirectory(path, perms)

        rightMap[relative] = listOf(
            RightRecord(
                user = record.username,
                rights = Right.FULL
            )
        )

        save()
    }

    fun update(record: RegistrationJournal.Record) {
        val i = registrationJournal.records.indexOfFirst { it.username == record.username }
        registrationJournal.records[i] = record
        save()
    }

    fun logout() {
        user = null
        workDir = "/"
        val loggedIn = login()
        if (!loggedIn) exitProcess(0)
    }

    fun addError() {
        val record = registrationRecord ?: error("User is not logined")
        update(record.copy(errors = record.errors + 1))
    }

    fun writeLog(record: SecurityJournal.Record) {
        securityJournal.write(record)
    }

    fun writeLogForUser(securityLevel: SecurityJournal.SecurityLevel, message: String) {
        writeLog(
            SecurityJournal.Record(
                user = user ?: error("User is not logged in"),
                time = Clock.System.now(),
                securityLevel = securityLevel,
                message = message,
            )
        )
    }
}
