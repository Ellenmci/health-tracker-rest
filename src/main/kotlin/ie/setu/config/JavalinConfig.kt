package ie.setu.config

import ie.setu.controllers.ActivityController
import ie.setu.controllers.UserController
import ie.setu.utils.jsonObjectMapper
import io.javalin.Javalin
import io.javalin.json.JavalinJackson

class JavalinConfig {

    val app = Javalin.create(
        { config ->
            config.jsonMapper(JavalinJackson(jsonObjectMapper()))
        }
    ).apply {
        exception(Exception::class.java) { e, ctx -> e.printStackTrace() }
        error(404) { ctx -> ctx.json("404 - Not Found") }
    }


    fun startJavalinService(): Javalin {
        app.start(getRemoteAssignedPort())
        registerRoutes(app)
        return app
    }

    private fun registerRoutes(app: Javalin) {
        val userController = UserController()
        val activityController = ActivityController()

// USER routes
        app.get("/api/users", userController::getAllUsers)
        app.get("/api/users/{user-id}", userController::getUserByUserId)
        app.post("/api/users", userController::addUser)
        app.delete("/api/users/{user-id}", userController::deleteUser)
        app.patch("/api/users/{user-id}", userController::updateUser)
        app.get("/api/users/email/{email}", userController::getUserByEmail)

// ACTIVITY routes
        app.get("/api/activities", activityController::getAllActivities)
        app.post("/api/activities", activityController::addActivity)
        app.get("/api/users/{user-id}/activities", activityController::getActivitiesByUserId)
        app.delete("/api/users/{user-id}/activities", activityController::deleteActivityByUserId)
        app.delete("/api/activities/{activity-id}", activityController::deleteActivityByActivityId)
        app.patch("/api/activities/{activity-id}", activityController::updateActivity)
    }

    private fun getRemoteAssignedPort(): Int {
        val remotePort = System.getenv("PORT")
        return if (remotePort != null) {
            Integer.parseInt(remotePort)
        } else 8080
    }

    fun getJavalinService(): Javalin {
        registerRoutes(app)
        return app
    }

}


