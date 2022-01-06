package users

enum class Right(val right: Int) {
    Read(0b000001),
    Execute(0b000010),
    Append(0b000100),
    Write(0b001000),
    Control(0b010000),
    Ownership(0b100000);

    companion object {
        const val FULL: Int = 0b111111
        const val SHARED: Int = 0b000011
        const val EMPTY: Int = 0b000000
    }
}

operator fun Int.contains(right: Right) = this and right.right == right.right

fun Int.toRights(): List<Right> = Right.values().filter { it in this }
fun Collection<Right>.toInt() = map(Right::right).reduce(Int::or)

val Int.canRead get() = Right.Read in this
val Int.canExecute get() = Right.Execute in this
val Int.canAppend get() = Right.Append in this
val Int.canWrite get() = Right.Write in this
val Int.canControl get() = Right.Control in this
val Int.canOwn get() = Right.Ownership in this

val Int.rightString get() = buildString {
    if (canRead) append("R") else append("_")
    if (canExecute) append("E") else append("_")
    if (canWrite) append("W") else append("_")
    if (canAppend) append("A") else append("_")
    if (canControl) append("C") else append("_")
    if (canOwn) append("O") else append("_")
}

fun parseRights(rights: String?): Int {
    if (rights == null) return Right.EMPTY

    val int = rights.toIntOrNull()

    if (int != null) return int

    val list = mutableListOf<Right>()
    val lower = rights.lowercase()
    if (lower.contains('r')) list.add(Right.Read)
    if (lower.contains('e')) list.add(Right.Execute)
    if (lower.contains('w')) list.add(Right.Write)
    if (lower.contains('a')) list.add(Right.Append)
    if (lower.contains('c')) list.add(Right.Control)
    if (lower.contains('o')) list.add(Right.Ownership)

    return list.toInt()
}
