package ie.setu.utils

import ie.setu.domain.Step
import ie.setu.domain.db.Steps
import org.jetbrains.exposed.sql.ResultRow

fun mapToStep(row: ResultRow): Step {
    return Step(
        id = row[Steps.id],
        userId = row[Steps.userId],
        steps = row[Steps.steps],
        date = row[Steps.date]
    )
}
