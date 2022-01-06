package users

import kotlinx.serialization.Serializable

typealias RightMap = MutableMap<String, List<RightRecord>>

@Serializable
data class RightRecord(
    val user: String,
    val rights: Int
)

fun RightMap.rightsFor(path: String, user: String): Int? = get(path)?.find { it.user == user }?.rights
