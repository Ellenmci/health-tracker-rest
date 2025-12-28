package ie.setu.domain.db

import org.jetbrains.exposed.sql.Table

object Sleeps : Table("sleeps") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id")
    val duration = double("duration")
    val quality = integer("quality")
    val date = varchar("date", 50)

    override val primaryKey = PrimaryKey(id)
}
