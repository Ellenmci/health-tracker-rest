package ie.setu.domain.repository

import ie.setu.domain.HeartRate
import ie.setu.domain.db.HeartRates
import ie.setu.utils.mapToHeartRate
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class HeartRateDAO {

    // Get all heart rate readings in the database
    fun getAll(): ArrayList<HeartRate> {
        val heartRateList: ArrayList<HeartRate> = arrayListOf()
        transaction {
            HeartRates.selectAll().map {
                heartRateList.add(mapToHeartRate(it))
            }
        }
        return heartRateList
    }

    // Summary for a user's heart rate readings
    fun findSummary(userId: Int): Map<String, Any> {
        val readings = findByUserId(userId)
        if (readings.isEmpty()) return emptyMap()

        val avg = readings.map { it.bpm }.average()
        val min = readings.minOf { it.bpm }
        val max = readings.maxOf { it.bpm }

        return mapOf(
            "averageBpm" to avg,
            "minBpm" to min,
            "maxBpm" to max,
            "count" to readings.size
        )
    }


    // Find a specific heart rate reading by id
    fun findById(id: Int): HeartRate? {
        return transaction {
            HeartRates
                .selectAll().where { HeartRates.id eq id }
                .map { mapToHeartRate(it) }
                .firstOrNull()
        }
    }

    // Find all heart rate readings for a specific user id
    fun findByUserId(userId: Int): List<HeartRate> {
        return transaction {
            HeartRates
                .selectAll().where { HeartRates.userId eq userId }
                .map { mapToHeartRate(it) }
        }
    }

    // Save a heart rate reading to the database
    fun save(heartRate: HeartRate): Int? {
        return transaction {
            HeartRates.insert {
                it[userId] = heartRate.userId
                it[bpm] = heartRate.bpm
                it[measuredAt] = heartRate.measuredAt
            } get HeartRates.id
        }
    }

    // Update a heart rate reading by id
    fun update(id: Int, heartRate: HeartRate): Int {
        return transaction {
            HeartRates.update({ HeartRates.id eq id }) {
                it[userId] = heartRate.userId
                it[bpm] = heartRate.bpm
                it[measuredAt] = heartRate.measuredAt
            }
        }
    }

    // Delete a heart rate reading by id
    fun delete(id: Int): Int {
        return transaction {
            HeartRates.deleteWhere { HeartRates.id eq id }
        }
    }

    // Delete all heart rate readings for a user
    fun deleteByUserId(userId: Int): Int {
        return transaction {
            HeartRates.deleteWhere { HeartRates.userId eq userId }
        }
    }
}
