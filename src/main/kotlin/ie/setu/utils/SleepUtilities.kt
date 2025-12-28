package ie.setu.utils

import ie.setu.domain.Sleep
import ie.setu.domain.db.Sleeps
import org.jetbrains.exposed.sql.ResultRow

fun mapToSleep(row: ResultRow): Sleep {
    return Sleep(
        id = row[Sleeps.id],
        userId = row[Sleeps.userId],
        duration = row[Sleeps.duration],
        quality = row[Sleeps.quality],
        date = row[Sleeps.date]
    )
}
