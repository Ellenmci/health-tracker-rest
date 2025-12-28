package ie.setu.helpers

import ie.setu.domain.db.Users
import ie.setu.domain.db.Activities
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import ie.setu.domain.db.HeartRates
import ie.setu.domain.db.Sleeps
import ie.setu.domain.db.Steps


object TestDatabaseConfig {

    private var initialised = false

    fun connect() {
        if (!initialised) {
            Database.connect(
                url = "jdbc:h2:mem:test-db;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver",
                user = "sa",
                password = ""
            )
            initialised = true

            transaction {
                SchemaUtils.create(Users, Activities, HeartRates, Steps, Sleeps)
            }
        }
    }

    fun reset() {
        transaction {
            SchemaUtils.drop(Users, Activities, HeartRates, Steps, Sleeps)
            SchemaUtils.create(Users, Activities, HeartRates, Steps, Sleeps)
        }
    }
}
