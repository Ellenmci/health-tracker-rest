package ie.setu.domain.repository

import ie.setu.domain.Sleep
import ie.setu.domain.db.Sleeps
import ie.setu.utils.mapToSleep
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class SleepDAO {

    fun getAll(): List<Sleep> =
        transaction {
            Sleeps.selectAll().map { mapToSleep(it) }
        }

    fun findByUserId(userId: Int): List<Sleep> =
        transaction {
            Sleeps.selectAll()
                .where { Sleeps.userId eq userId }
                .map { mapToSleep(it) }
        }

    fun findById(id: Int): Sleep? =
        transaction {
            Sleeps.selectAll()
                .where { Sleeps.id eq id }
                .map { mapToSleep(it) }
                .firstOrNull()
        }

    fun save(sleep: Sleep): Int? =
        transaction {
            Sleeps.insert {
                it[userId] = sleep.userId
                it[duration] = sleep.duration
                it[quality] = sleep.quality
                it[date] = sleep.date
            } get Sleeps.id
        }

    fun update(id: Int, sleep: Sleep): Int =
        transaction {
            Sleeps.update({ Sleeps.id eq id }) {
                it[userId] = sleep.userId
                it[duration] = sleep.duration
                it[quality] = sleep.quality
                it[date] = sleep.date
            }
        }

    fun deleteByUserId(userId: Int): Int =
        transaction {
            Sleeps.deleteWhere { Sleeps.userId eq userId }
        }

    fun deleteById(id: Int): Int =
        transaction {
            Sleeps.deleteWhere { Sleeps.id eq id }
        }

    fun findSummary(userId: Int): Map<String, Any> {
        val entries = findByUserId(userId)
        if (entries.isEmpty()) return emptyMap()

        val totalHours = entries.sumOf { it.duration }
        val avgHours = entries.map { it.duration }.average()
        val avgQuality = entries.map { it.quality }.average()
        val minHours = entries.minOf { it.duration }
        val maxHours = entries.maxOf { it.duration }

        return mapOf(
            "totalHours" to totalHours,
            "averageHours" to avgHours,
            "averageQuality" to avgQuality,
            "minHours" to minHours,
            "maxHours" to maxHours,
            "count" to entries.size
        )
    }
}
