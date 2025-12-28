package ie.setu.utils

import ie.setu.domain.HeartRate
import ie.setu.domain.db.HeartRates
import org.jetbrains.exposed.sql.ResultRow

fun mapToHeartRate(row: ResultRow): HeartRate =
    HeartRate(
        id = row[HeartRates.id],
        userId = row[HeartRates.userId],
        bpm = row[HeartRates.bpm],
        measuredAt = row[HeartRates.measuredAt]
    )

