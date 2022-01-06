package fs

import context.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

const val BASE_DIR = "datastore"
const val DISK_A = "a"
const val DISK_B = "b"
const val DISK_C = "c"
const val DISK_D = "d"
const val SYSTEM_DIR = ".system"
const val CONTEXT_FILE = "context.json"

private fun loadFilesystem() {
    val dirPath = Path.of(BASE_DIR)

    if (Files.isDirectory(dirPath)) return

    val perms = PosixFilePermissions.fromString("rwx------").let {
        PosixFilePermissions.asFileAttribute(it)
    }

    Files.createDirectory(dirPath, perms)
    listOf(DISK_A, DISK_B, DISK_C, DISK_D, SYSTEM_DIR)
        .map(dirPath::resolve)
        .filterNot(Files::exists)
        .forEach(Files::createDirectory)

    val rightsPath = dirPath.resolve(SYSTEM_DIR).resolve(CONTEXT_FILE)
    if (Files.exists(rightsPath)) return

    Files.createFile(rightsPath)
    Files.writeString(rightsPath, "{}")
}

fun Context.save() {
    Files.writeString(absolutePath("$SYSTEM_DIR/$CONTEXT_FILE"), Json.encodeToString(this))
}

private fun loadContext(): Context = try {
    Json.decodeFromString<Context>(
        Files.readString(absolutePath("$SYSTEM_DIR/$CONTEXT_FILE"))
    ).apply { require(registrationJournal.isNotEmpty()) }
} catch (e: Exception) {
    Context.DEFAULT_CONTEXT.also(Context::save)
}

fun initApp(): Context {
    loadFilesystem()
    return loadContext()
}
