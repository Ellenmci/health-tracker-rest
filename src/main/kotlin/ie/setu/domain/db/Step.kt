package ie.setu.domain.db

import org.jetbrains.exposed.sql.Table

object Steps : Table("steps") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id")
    val steps = integer("steps")
    val date = varchar("date", 50)

    override val primaryKey = PrimaryKey(id)
}
