package ie.setu.domain

import org.joda.time.DateTime

data class HeartRate(
    var id: Int,
    var userId: Int,
    var bpm: Int,
    var measuredAt: DateTime
)
