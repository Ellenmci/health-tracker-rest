package ie.setu.controllers

import ie.setu.domain.User
import ie.setu.domain.repository.UserDAO
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HealthTrackerControllerTestMockDB {

    // This is NOT used in the mock test, but kept for compatibility
    private val origin = "http://localhost:7000"

    // Helper function (not used, but included because lecturer said to copy it)
    private fun retrieveUserById(id: Int) =
        kong.unirest.core.Unirest.get("$origin/api/users/$id").asString()

    @Test
    fun `getting a user by id when id exists returns 200`() {

        // Arrange
        val validName = "Lisa Simpson"
        val validEmail = "lisa@simpson.com"
        val testUser = User(1234, validName, validEmail)

        // Create mock DAO
        val mockUserDAO = Mockito.mock(UserDAO::class.java)

        // Define mock behaviour
        `when`(mockUserDAO.save(testUser)).thenReturn(1234)
        `when`(mockUserDAO.findById(1234)).thenReturn(testUser)

        // FIX: unwrap Int? to Int using !!
        val addedUserId = mockUserDAO.save(testUser)!!

        // Act
        val retrievedUser = mockUserDAO.findById(addedUserId)

        // Assert
        assertEquals(testUser.email, retrievedUser?.email)
        assertEquals(testUser.name, retrievedUser?.name)
    }
}
