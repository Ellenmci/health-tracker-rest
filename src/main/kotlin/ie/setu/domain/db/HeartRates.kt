package ie.setu.domain.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime

object HeartRates : Table("heartrates") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id")
    val bpm = integer("bpm")
    val measuredAt = datetime("measured_at")

    override val primaryKey = PrimaryKey(id)
}
