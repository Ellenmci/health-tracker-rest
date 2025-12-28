package ie.setu.controllers

import ie.setu.domain.Activity
import ie.setu.domain.HeartRate
import ie.setu.domain.Sleep
import ie.setu.domain.Step
import ie.setu.domain.User
import ie.setu.helpers.ServerContainer
import ie.setu.helpers.TestDatabaseConfig
import ie.setu.helpers.activities
import ie.setu.helpers.nonExistingEmail
import ie.setu.helpers.populateUserTable
import ie.setu.helpers.updatedCalories
import ie.setu.helpers.updatedDescription
import ie.setu.helpers.updatedDuration
import ie.setu.helpers.updatedStarted
import ie.setu.helpers.users
import ie.setu.helpers.validEmail
import ie.setu.helpers.validName
import ie.setu.utils.jsonNodeToObject
import ie.setu.utils.jsonToObject
import kong.unirest.core.HttpResponse
import kong.unirest.core.JsonNode
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import kong.unirest.core.Unirest
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertNotEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HealthTrackerTest {

    companion object {

        private val app = ServerContainer.instance
        private val origin = "http://localhost:" + app.port()

        @BeforeAll
        @JvmStatic
        fun setupInMemoryDatabase() {
            TestDatabaseConfig.connect()
        }
    }

    @BeforeEach
    fun resetDatabase() {
        TestDatabaseConfig.reset()
    }

    @Nested
    inner class CreateUsers {
        @Test
        fun `add a user with correct details returns a 201 response`() {

            //Arrange & Act & Assert
            //    add the user and verify return code (using fixture data)
            val addResponse = addUser(validName, validEmail)
            assertEquals(201, addResponse.status)

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse = retrieveUserByEmail(validEmail)
            assertEquals(200, retrieveResponse.status)

            //Assert - verify the contents of the retrieved user
            val retrievedUser: User = jsonToObject(addResponse.body.toString())
            assertEquals(validEmail, retrievedUser.email)
            assertEquals(validName, retrievedUser.name)

            //After - restore the db to previous state by deleting the added user
            val deleteResponse = deleteUser(retrievedUser.id)
            assertEquals(204, deleteResponse.status)
        }

        @Test
        fun `multiple users added to table can be retrieved successfully`() {
            transaction {
                val userDAO = populateUserTable()

                assertEquals(3, userDAO.getAll().size)
                assertEquals(users[0].name, userDAO.findByEmail(users[0].email)?.name)
                assertEquals(users[1].name, userDAO.findByEmail(users[1].email)?.name)
                assertEquals(users[2].name, userDAO.findByEmail(users[2].email)?.name)
            }
        }
    }

    @Nested
    inner class ReadUsers {

        @Test
        fun `get all users from the database returns 200 or 404 response`() {
            val response = Unirest.get(origin + "/api/users/").asString()
            if (response.status == 200) {
                val retrievedUsers: ArrayList<User> = jsonToObject(response.body.toString())
                assertNotEquals(0, retrievedUsers.size)
            }
            else {
                assertEquals(404, response.status)
            }
        }


        @Test
        fun `get user by id when user does not exist returns 404 response`() {

            //Arrange - test data for user id
            val id = Integer.MIN_VALUE

            // Act - attempt to retrieve the non-existent user from the database
            val retrieveResponse = Unirest.get(origin + "/api/users/${id}").asString()

            // Assert -  verify return code
            assertEquals(404, retrieveResponse.status)
        }

        @Test
        fun `get user by email when user does not exist returns 404 response`() {
            // Arrange & Act - attempt to retrieve the non-existent user from the database
            val retrieveResponse = Unirest.get(origin + "/api/users/email/${nonExistingEmail}").asString()
            // Assert -  verify return code
            assertEquals(404, retrieveResponse.status)
        }

        @Test
        fun `getting a user by id when id exists, returns a 200 response`() {

            //Arrange - add the user
            val addResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse = retrieveUserById(addedUser.id)
            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        @Test
        fun `getting a user by email when email exists, returns a 200 response`() {

            //Arrange - add the user
            addUser(validName, validEmail)

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse = retrieveUserByEmail(validEmail)
            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            val retrievedUser : User = jsonToObject(retrieveResponse.body.toString())
            deleteUser(retrievedUser.id)
        }

    }

    @Nested
    inner class UpdateUsers {
        @Test
        fun `updating a user when it exists, returns a 204 response`() {

            //Arrange - add the user that we plan to do an update on
            val updatedName = "Updated Name"
            val updatedEmail = "Updated Email"
            val addedResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addedResponse.body.toString())

            //Act & Assert - update the email and name of the retrieved user and assert 204 is returned
            assertEquals(204, updateUser(addedUser.id, updatedName, updatedEmail).status)

            //Act & Assert - retrieve updated user and assert details are correct
            val updatedUserResponse = retrieveUserById(addedUser.id)
            val updatedUser : User = jsonToObject(updatedUserResponse.body.toString())
            assertEquals(updatedName, updatedUser.name)
            assertEquals(updatedEmail, updatedUser.email)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        @Test
        fun `updating a user when it doesn't exist, returns a 404 response`() {

            //Arrange - creating some text fixture data
            val updatedName = "Updated Name"
            val updatedEmail = "Updated Email"

            //Act & Assert - attempt to update the email and name of user that doesn't exist
            assertEquals(404, updateUser(-1, updatedName, updatedEmail).status)
        }

    }

    @Nested
    inner class DeleteUsers {
        @Test
        fun `deleting a user when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteUser(-1).status)
        }

        @Test
        fun `deleting a user when it exists, returns a 204 response`() {

            //Arrange - add the user that we plan to do a delete on
            val addedResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addedResponse.body.toString())

            //Act & Assert - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)

            //Act & Assert - attempt to retrieve the deleted user --> 404 response
            assertEquals(404, retrieveUserById(addedUser.id).status)
        }


    }

    @Nested
    inner class CreateActivities {

        @Test
        fun `add an activity when a user exists for it, returns a 201 response`() {

            //Arrange - add a user and an associated activity that we plan to do a delete on
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val addActivityResponse = addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id
            )
            assertEquals(201, addActivityResponse.status)

            //After - delete the user (Activity will cascade delete in the database)
            deleteUser(addedUser.id)
        }

        @Test
        fun `add an activity when no user exists for it, returns a 404 response`() {

            //Arrange - check there is no user for -1 id
            val userId = -1
            assertEquals(404, retrieveUserById(userId).status)

            val addActivityResponse = addActivity(
                activities.get(0).description, activities.get(0).duration,
                activities.get(0).calories, activities.get(0).started, userId
            )
            assertEquals(404, addActivityResponse.status)
        }
    }

    @Nested
    inner class ReadActivities {

        @Test
        fun `get all activities from the database returns 200 or 404 response`() {
            val response = retrieveAllActivities()
            if (response.status == 200){
                val retrievedActivities = jsonNodeToObject<Array<Activity>>(response)
                assertNotEquals(0, retrievedActivities.size)
            }
            else{
                assertEquals(404, response.status)
            }
        }

        @Test
        fun `get all activities by user id when user and activities exists returns 200 response`() {
            //Arrange - add a user and 3 associated activities that we plan to retrieve
            val addedUser : User = jsonToObject(addUser(validName, validEmail).body.toString())
            addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id)
            addActivity(
                activities[1].description, activities[1].duration,
                activities[1].calories, activities[1].started, addedUser.id)
            addActivity(
                activities[2].description, activities[2].duration,
                activities[2].calories, activities[2].started, addedUser.id)

            //Assert and Act - retrieve the three added activities by user id
            val response = retrieveActivitiesByUserId(addedUser.id)
            assertEquals(200, response.status)
            val retrievedActivities = jsonNodeToObject<Array<Activity>>(response)
            assertEquals(3, retrievedActivities.size)

            //After - delete the added user and assert a 204 is returned (activities are cascade deleted)
            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all activities by user id when no activities exist returns 404 response`() {
            //Arrange - add a user
            val addedUser : User = jsonToObject(addUser(validName, validEmail).body.toString())

            //Assert and Act - retrieve the activities by user id
            val response = retrieveActivitiesByUserId(addedUser.id)
            assertEquals(404, response.status)

            //After - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all activities by user id when no user exists returns 404 response`() {
            //Arrange
            val userId = -1

            //Assert and Act - retrieve activities by user id
            val response = retrieveActivitiesByUserId(userId)
            assertEquals(404, response.status)
        }

        @Test
        fun `get activity by activity id when no activity exists returns 404 response`() {
            //Arrange
            val activityId = -1
            //Assert and Act - attempt to retrieve the activity by activity id
            val response = retrieveActivityByActivityId(activityId)
            assertEquals(404, response.status)
        }


        @Test
        fun `get activity by activity id when activity exists returns 200 response`() {
            //Arrange - add a user and associated activity
            val addedUser : User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse = addActivity(
                activities[0].description,
                activities[0].duration, activities[0].calories,
                activities[0].started, addedUser.id)
            assertEquals(201, addActivityResponse.status)
            val addedActivity = jsonNodeToObject<Activity>(addActivityResponse)

            //Act & Assert - retrieve the activity by activity id
            val response = retrieveActivityByActivityId(addedActivity.id)
            assertEquals(200, response.status)

            //After - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)
        }

    }

    @Nested
    inner class UpdateActivities {

        @Test
        fun `updating an activity by activity id when it doesn't exist, returns a 404 response`() {
            val userId = -1
            val activityID = -1

            //Arrange - check there is no user for -1 id
            assertEquals(404, retrieveUserById(userId).status)

            //Act & Assert - attempt to update the details of an activity/user that doesn't exist
            assertEquals(
                404, updateActivity(
                    activityID, updatedDescription, updatedDuration,
                    updatedCalories, updatedStarted, userId
                ).status
            )
        }

        @Test
        fun `updating an activity by activity id when it exists, returns 204 response`() {

            //Arrange - add a user and an associated activity that we plan to do an update on
            val addedUser : User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse = addActivity(
                activities[0].description,
                activities[0].duration, activities[0].calories,
                activities[0].started, addedUser.id)
            assertEquals(201, addActivityResponse.status)
            val addedActivity = jsonNodeToObject<Activity>(addActivityResponse)

            //Act & Assert - update the added activity and assert a 204 is returned
            val updatedActivityResponse = updateActivity(addedActivity.id, updatedDescription,
                updatedDuration, updatedCalories, updatedStarted, addedUser.id)
            assertEquals(204, updatedActivityResponse.status)

            //Assert that the individual fields were all updated as expected
            val retrievedActivityResponse = retrieveActivityByActivityId(addedActivity.id)
            val updatedActivity = jsonNodeToObject<Activity>(retrievedActivityResponse)
            assertEquals(updatedDescription,updatedActivity.description)
            assertEquals(updatedDuration, updatedActivity.duration, 0.1)
            assertEquals(updatedCalories, updatedActivity.calories)
            assertEquals(updatedStarted, updatedActivity.started )

            //After - delete the user
            deleteUser(addedUser.id)
        }
    }

    @Nested
    inner class DeleteActivities {

        @Test
        fun `deleting an activity by activity id when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteActivityByActivityId(-1).status)
        }

        @Test
        fun `deleting activities by user id when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteActivitiesByUserId(-1).status)
        }

        @Test
        fun `deleting an activity by id when it exists, returns a 204 response`() {

            //Arrange - add a user and an associated activity that we plan to do a delete on
            val addedUser : User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse = addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id)
            assertEquals(201, addActivityResponse.status)

            //Act & Assert - delete the added activity and assert a 204 is returned
            val addedActivity = jsonNodeToObject<Activity>(addActivityResponse)
            assertEquals(204, deleteActivityByActivityId(addedActivity.id).status)

            //After - delete the user
            deleteUser(addedUser.id)
        }

        @Test
        fun `deleting all activities by userid when it exists, returns a 204 response`() {

            //Arrange - add a user and 3 associated activities that we plan to do a cascade delete
            val addedUser : User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse1 = addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id)
            assertEquals(201, addActivityResponse1.status)
            val addActivityResponse2 = addActivity(
                activities[1].description, activities[1].duration,
                activities[1].calories, activities[1].started, addedUser.id)
            assertEquals(201, addActivityResponse2.status)
            val addActivityResponse3 = addActivity(
                activities[2].description, activities[2].duration,
                activities[2].calories, activities[2].started, addedUser.id)
            assertEquals(201, addActivityResponse3.status)

            //Act & Assert - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)

            //Act & Assert - attempt to retrieve the deleted activities
            val addedActivity1 = jsonNodeToObject<Activity>(addActivityResponse1)
            val addedActivity2 = jsonNodeToObject<Activity>(addActivityResponse2)
            val addedActivity3 = jsonNodeToObject<Activity>(addActivityResponse3)
            assertEquals(404, retrieveActivityByActivityId(addedActivity1.id).status)
            assertEquals(404, retrieveActivityByActivityId(addedActivity2.id).status)
            assertEquals(404, retrieveActivityByActivityId(addedActivity3.id).status)
        }
    }

    @Nested
    inner class HeartRateTests {

        @Test
        fun `adding a heart rate reading returns 201 response`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val response = Unirest.post("$origin/api/users/${addedUser.id}/heartrates")
                .body("""
                {
                    "id": 0,
                    "userId": ${addedUser.id},
                    "bpm": 72,
                    "measuredAt": "2025-01-01T10:00:00.000Z"
                }
            """.trimIndent())
                .asJson()

            assertEquals(201, response.status)
            deleteUser(addedUser.id)
        }

        @Test
        fun `retrieving heart rates by user id returns 200 when data exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            listOf(70, 85).forEach { bpm ->
                Unirest.post("$origin/api/users/${addedUser.id}/heartrates")
                    .body("""
                    {
                        "id": 0,
                        "userId": ${addedUser.id},
                        "bpm": $bpm,
                        "measuredAt": "2025-01-01T10:00:00.000Z"
                    }
                """.trimIndent())
                    .asJson()
            }

            val response = Unirest.get("$origin/api/users/${addedUser.id}/heartrates").asJson()
            assertEquals(200, response.status)
            val readings = jsonNodeToObject<Array<HeartRate>>(response)
            assertEquals(2, readings.size)

            deleteUser(addedUser.id)
        }

        @Test
        fun `retrieving heart rates by user id returns 404 when no data exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val response = Unirest.get("$origin/api/users/${addedUser.id}/heartrates").asJson()
            assertEquals(404, response.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `heart rate summary returns correct statistics`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            listOf(60, 80, 100).forEach { bpm ->
                Unirest.post("$origin/api/users/${addedUser.id}/heartrates")
                    .body("""
                    {
                        "id": 0,
                        "userId": ${addedUser.id},
                        "bpm": $bpm,
                        "measuredAt": "2025-01-01T10:00:00.000Z"
                    }
                """.trimIndent())
                    .asJson()
            }

            val response = Unirest.get("$origin/api/users/${addedUser.id}/heartrates/summary").asJson()
            assertEquals(200, response.status)
            val json = response.body.`object`

            assertEquals(80.0, json.getDouble("averageBpm"))
            assertEquals(60, json.getInt("minBpm"))
            assertEquals(100, json.getInt("maxBpm"))
            assertEquals(3, json.getInt("count"))

            deleteUser(addedUser.id)
        }

        @Test
        fun `heart rate summary returns 404 when no readings exist`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val response = Unirest.get("$origin/api/users/${addedUser.id}/heartrates/summary").asJson()
            assertEquals(404, response.status)

            deleteUser(addedUser.id)
        }
    }

    @Nested
    inner class StepTests {

        @Test
        fun `adding steps returns 201 response`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val response = addSteps(addedUser.id, 5000, "2025-01-01")
            assertEquals(201, response.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `retrieving steps by user id returns 200 when data exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            addSteps(addedUser.id, 4000, "2025-01-01")
            addSteps(addedUser.id, 6000, "2025-01-02")

            val response = retrieveStepsByUserId(addedUser.id)
            assertEquals(200, response.status)

            val steps = jsonNodeToObject<Array<Step>>(response)
            assertEquals(2, steps.size)

            deleteUser(addedUser.id)
        }

        @Test
        fun `retrieving steps by user id returns 404 when no data exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val response = retrieveStepsByUserId(addedUser.id)
            assertEquals(404, response.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `step summary returns correct statistics`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            addSteps(addedUser.id, 3000, "2025-01-01")
            addSteps(addedUser.id, 7000, "2025-01-02")

            val response = retrieveStepSummary(addedUser.id)
            assertEquals(200, response.status)

            val json = response.body.`object`
            assertEquals(10000, json.getInt("totalSteps"))
            assertEquals(2, json.getInt("count"))
            assertEquals(3000, json.getInt("minSteps"))
            assertEquals(7000, json.getInt("maxSteps"))

            deleteUser(addedUser.id)
        }

        @Test
        fun `step summary returns 404 when no entries exist`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val response = retrieveStepSummary(addedUser.id)
            assertEquals(404, response.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `get step by id returns 200 when exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val addResponse = addSteps(addedUser.id, 5000, "2025-01-01")
            val addedStep = jsonNodeToObject<Step>(addResponse)

            val response = retrieveStepById(addedStep.id)
            assertEquals(200, response.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `get step by id returns 404 when not found`() {
            val response = retrieveStepById(-1)
            assertEquals(404, response.status)
        }

        @Test
        fun `updating a step returns 204 when exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val addResponse = addSteps(addedUser.id, 4000, "2025-01-01")
            val addedStep = jsonNodeToObject<Step>(addResponse)

            val updateResponse = updateStep(
                addedStep.id,
                addedUser.id,
                9000,
                "2025-01-02"
            )
            assertEquals(204, updateResponse.status)

            val retrieved = jsonNodeToObject<Step>(retrieveStepById(addedStep.id))
            assertEquals(9000, retrieved.steps)

            deleteUser(addedUser.id)
        }

        @Test
        fun `updating a step returns 404 when not found`() {
            val response = updateStep(-1, 1, 9000, "2025-01-02")
            assertEquals(404, response.status)
        }

        @Test
        fun `deleting a step by id returns 204 when exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val addResponse = addSteps(addedUser.id, 3000, "2025-01-01")
            val addedStep = jsonNodeToObject<Step>(addResponse)

            val deleteResponse = deleteStepById(addedStep.id)
            assertEquals(204, deleteResponse.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `deleting a step by id returns 404 when not found`() {
            val response = deleteStepById(-1)
            assertEquals(404, response.status)
        }
    }

    @Nested
    inner class SleepTests {

        @Test
        fun `adding sleep returns 201 response`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val response = addSleep(addedUser.id, 7.5, 8, "2025-01-01")
            assertEquals(201, response.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `retrieving sleep by user id returns 200 when data exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            addSleep(addedUser.id, 6.0, 7, "2025-01-01")
            addSleep(addedUser.id, 8.0, 9, "2025-01-02")

            val response = retrieveSleepByUserId(addedUser.id)
            assertEquals(200, response.status)

            val sleeps = jsonNodeToObject<Array<Sleep>>(response)
            assertEquals(2, sleeps.size)

            deleteUser(addedUser.id)
        }

        @Test
        fun `retrieving sleep by user id returns 404 when no data exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val response = retrieveSleepByUserId(addedUser.id)
            assertEquals(404, response.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `sleep summary returns correct statistics`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            addSleep(addedUser.id, 6.0, 7, "2025-01-01")
            addSleep(addedUser.id, 8.0, 9, "2025-01-02")

            val response = retrieveSleepSummary(addedUser.id)
            assertEquals(200, response.status)

            val json = response.body.`object`
            assertEquals(14.0, json.getDouble("totalHours"))
            assertEquals(2, json.getInt("count"))
            assertEquals(6.0, json.getDouble("minHours"))
            assertEquals(8.0, json.getDouble("maxHours"))
            assertEquals(8.0, json.getDouble("averageQuality"))

            deleteUser(addedUser.id)
        }

        @Test
        fun `sleep summary returns 404 when no entries exist`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val response = retrieveSleepSummary(addedUser.id)
            assertEquals(404, response.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `get sleep by id returns 200 when exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val addResponse = addSleep(addedUser.id, 7.0, 8, "2025-01-01")
            val addedSleep = jsonNodeToObject<Sleep>(addResponse)

            val response = retrieveSleepById(addedSleep.id)
            assertEquals(200, response.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `get sleep by id returns 404 when not found`() {
            val response = retrieveSleepById(-1)
            assertEquals(404, response.status)
        }

        @Test
        fun `updating sleep returns 204 when exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val addResponse = addSleep(addedUser.id, 6.0, 7, "2025-01-01")
            val addedSleep = jsonNodeToObject<Sleep>(addResponse)

            val updateResponse = updateSleep(
                addedSleep.id,
                addedUser.id,
                9.0,
                10,
                "2025-01-02"
            )
            assertEquals(204, updateResponse.status)

            val retrieved = jsonNodeToObject<Sleep>(retrieveSleepById(addedSleep.id))
            assertEquals(9.0, retrieved.duration)
            assertEquals(10, retrieved.quality)

            deleteUser(addedUser.id)
        }

        @Test
        fun `updating sleep returns 404 when not found`() {
            val response = updateSleep(-1, 1, 9.0, 10, "2025-01-02")
            assertEquals(404, response.status)
        }

        @Test
        fun `deleting sleep by id returns 204 when exists`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val addResponse = addSleep(addedUser.id, 7.0, 8, "2025-01-01")
            val addedSleep = jsonNodeToObject<Sleep>(addResponse)

            val deleteResponse = deleteSleepById(addedSleep.id)
            assertEquals(204, deleteResponse.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `deleting sleep by id returns 404 when not found`() {
            val response = deleteSleepById(-1)
            assertEquals(404, response.status)
        }
    }

    private fun addSleep(userId: Int, duration: Double, quality: Int, date: String): HttpResponse<JsonNode> {
        return Unirest.post("$origin/api/users/$userId/sleep")
            .body("""
            {
                "id": 0,
                "userId": $userId,
                "duration": $duration,
                "quality": $quality,
                "date": "$date"
            }
        """.trimIndent())
            .asJson()
    }

    private fun retrieveSleepByUserId(userId: Int): HttpResponse<JsonNode> {
        return Unirest.get("$origin/api/users/$userId/sleep").asJson()
    }

    private fun retrieveSleepSummary(userId: Int): HttpResponse<JsonNode> {
        return Unirest.get("$origin/api/users/$userId/sleep/summary").asJson()
    }

    private fun retrieveSleepById(id: Int): HttpResponse<JsonNode> {
        return Unirest.get("$origin/api/sleep/$id").asJson()
    }

    private fun updateSleep(id: Int, userId: Int, duration: Double, quality: Int, date: String): HttpResponse<JsonNode> {
        return Unirest.patch("$origin/api/sleep/$id")
            .body("""
            {
                "id": $id,
                "userId": $userId,
                "duration": $duration,
                "quality": $quality,
                "date": "$date"
            }
        """.trimIndent())
            .asJson()
    }

    private fun deleteSleepById(id: Int): HttpResponse<String> {
        return Unirest.delete("$origin/api/sleep/$id").asString()
    }


    //helper function to add heartrate readings
    private fun addHeartRate(userId: Int, bpm: Int): HttpResponse<JsonNode> {
        return Unirest.post("$origin/api/users/$userId/heartrates")
            .body("""
            {
                "id": 0,
                "userId": $userId,
                "bpm": $bpm,
                "measuredAt": "2025-01-01T10:00:00.000Z"
            }
        """.trimIndent())
            .asJson()
    }

    // helper function to get all heart rate readings for a user
    private fun retrieveHeartRatesByUserId(userId: Int): HttpResponse<JsonNode> {
        return Unirest.get("$origin/api/users/$userId/heartrates").asJson()
    }

    // helper function to get heart rate summary for a user
    private fun retrieveHeartRateSummary(userId: Int): HttpResponse<JsonNode> {
        return Unirest.get("$origin/api/users/$userId/heartrates/summary").asJson()
    }

    // helper function to add steps
    private fun addSteps(userId: Int, steps: Int, date: String): HttpResponse<JsonNode> {
        return Unirest.post("$origin/api/users/$userId/steps")
            .body("""
            {
                "id": 0,
                "userId": $userId,
                "steps": $steps,
                "date": "$date"
            }
        """.trimIndent())
            .asJson()
    }

    // helper function to retrieve steps by user id
    private fun retrieveStepsByUserId(userId: Int): HttpResponse<JsonNode> {
        return Unirest.get("$origin/api/users/$userId/steps").asJson()
    }

    // helper function to retrieve step summary
    private fun retrieveStepSummary(userId: Int): HttpResponse<JsonNode> {
        return Unirest.get("$origin/api/users/$userId/steps/summary").asJson()
    }

    // helper function to retrieve step by id
    private fun retrieveStepById(id: Int): HttpResponse<JsonNode> {
        return Unirest.get("$origin/api/steps/$id").asJson()
    }

    // helper function to update a step
    private fun updateStep(id: Int, userId: Int, steps: Int, date: String): HttpResponse<JsonNode> {
        return Unirest.patch("$origin/api/steps/$id")
            .body("""
            {
                "id": $id,
                "userId": $userId,
                "steps": $steps,
                "date": "$date"
            }
        """.trimIndent())
            .asJson()
    }

    // helper function to delete a step by id
    private fun deleteStepById(id: Int): HttpResponse<String> {
        return Unirest.delete("$origin/api/steps/$id").asString()
    }

    //helper function to add a test user to the database
    private fun addUser (name: String, email: String): HttpResponse<JsonNode> {
        return Unirest.post(origin + "/api/users")
            .body("{\"name\":\"$name\", \"email\":\"$email\"}")
            .asJson()
    }

    //helper function to delete a test user from the database
    private fun deleteUser (id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/users/$id").asString()
    }

    //helper function to retrieve a test user from the database by email
    private fun retrieveUserByEmail(email : String) : HttpResponse<String> {
        return Unirest.get(origin + "/api/users/email/${email}").asString()
    }

    //helper function to retrieve a test user from the database by id
    private fun retrieveUserById(id: Int) : HttpResponse<String> {
        return Unirest.get(origin + "/api/users/${id}").asString()
    }

    //helper function to update a test user to the database
    private fun updateUser (id: Int, name: String, email: String): HttpResponse<JsonNode> {
        return Unirest.patch(origin + "/api/users/$id")
            .body("{\"name\":\"$name\", \"email\":\"$email\"}")
            .asJson()
    }

    //helper function to retrieve all activities
    private fun retrieveAllActivities(): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/activities").asJson()
    }

    //helper function to retrieve activities by user id
    private fun retrieveActivitiesByUserId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/users/${id}/activities").asJson()
    }

    //helper function to retrieve activity by activity id
    private fun retrieveActivityByActivityId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/activities/${id}").asJson()
    }

    //helper function to delete an activity by activity id
    private fun deleteActivityByActivityId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/activities/$id").asString()
    }

    //helper function to delete activities by user id
    private fun deleteActivitiesByUserId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/users/$id/activities").asString()
    }

    //helper function to update an activity
    private fun updateActivity(id: Int, description: String, duration: Double, calories: Int,
                               started: DateTime, userId: Int): HttpResponse<JsonNode> {
        return Unirest.patch(origin + "/api/activities/$id")
            .body("""
                {
                  "description":"$description",
                  "duration":$duration,
                  "calories":$calories,
                  "started":"$started",
                  "userId":$userId
                }
            """.trimIndent()).asJson()
    }

    //helper function to add an activity
    private fun addActivity(description: String, duration: Double, calories: Int,
                            started: DateTime, userId: Int): HttpResponse<JsonNode> {
        return Unirest.post(origin + "/api/activities")
            .body("""
                {
                   "description":"$description",
                   "duration":$duration,
                   "calories":$calories,
                   "started":"$started",
                   "userId":$userId
                }
            """.trimIndent())
            .asJson()
    }
}
