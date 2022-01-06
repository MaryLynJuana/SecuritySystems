package fs

import java.nio.file.Path

fun absolutePath(path: String): Path = path.trimStart('/').let { Path.of("$BASE_DIR/$it") }
fun relativePath(path: Path): String = path.toString().substringAfter(BASE_DIR)
