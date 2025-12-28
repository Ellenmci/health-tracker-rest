package ie.setu.domain.repository

import ie.setu.domain.Step
import ie.setu.domain.db.Steps
import ie.setu.utils.mapToStep
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class StepDAO {

    fun getAll(): List<Step> =
        transaction {
            Steps.selectAll()
                .map { mapToStep(it) }
        }

    fun findByUserId(userId: Int): List<Step> =
        transaction {
            Steps.selectAll()
                .where { Steps.userId eq userId }
                .map { mapToStep(it) }
        }

    fun findById(id: Int): Step? =
        transaction {
            Steps.selectAll()
                .where { Steps.id eq id }
                .map { mapToStep(it) }
                .firstOrNull()
        }

    fun save(step: Step): Int? =
        transaction {
            Steps.insert {
                it[userId] = step.userId
                it[steps] = step.steps
                it[date] = step.date
            } get Steps.id
        }

    fun update(id: Int, step: Step): Int =
        transaction {
            Steps.update({ Steps.id eq id }) {
                it[userId] = step.userId
                it[steps] = step.steps
                it[date] = step.date
            }
        }

    fun deleteByUserId(userId: Int): Int =
        transaction {
            Steps.deleteWhere { Steps.userId eq userId }
        }

    fun deleteById(id: Int): Int =
        transaction {
            Steps.deleteWhere { Steps.id eq id }
        }

    fun findSummary(userId: Int): Map<String, Any> {
        val entries = findByUserId(userId)
        if (entries.isEmpty()) return emptyMap()

        val total = entries.sumOf { it.steps }
        val avg = entries.map { it.steps }.average()
        val min = entries.minOf { it.steps }
        val max = entries.maxOf { it.steps }

        return mapOf(
            "totalSteps" to total,
            "averageSteps" to avg,
            "minSteps" to min,
            "maxSteps" to max,
            "count" to entries.size
        )
    }
}
