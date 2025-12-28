package ie.setu.config

import ie.setu.controllers.ActivityController
import ie.setu.controllers.HeartRateController
import ie.setu.controllers.SleepController
import ie.setu.controllers.StepController
import ie.setu.controllers.UserController
import ie.setu.utils.jsonObjectMapper
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.vue.VueComponent

class JavalinConfig {

    val app = Javalin.create(
        { config ->
            config.jsonMapper(JavalinJackson(jsonObjectMapper()))
            config.staticFiles.enableWebjars()
            config.vue.vueInstanceNameInJs = "app"

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
        val heartRateController = HeartRateController()
        val stepController = StepController()
        val sleepController = SleepController()

//Vue routes
        app.get("/", VueComponent("<home-page></home-page>"))
        app.get("/users", VueComponent("<user-overview></user-overview>"))
        app.get("/users/{user-id}", VueComponent("<user-profile></user-profile>"))
        app.get("/users/{user-id}/activities", VueComponent("<user-activity-overview></user-activity-overview>"))

//HeartRate routes
        app.post("/api/users/{user-id}/heartrates", heartRateController::addHeartRate)
        app.get("/api/users/{user-id}/heartrates", heartRateController::getHeartRatesByUserId)
        app.get("/api/users/{user-id}/heartrates/summary", heartRateController::getHeartRateSummary)

//Step routes
// user-scoped step routes
        app.post("/api/users/{user-id}/steps", stepController::addSteps)
        app.get("/api/users/{user-id}/steps", stepController::getStepsByUserId)
        app.get("/api/users/{user-id}/steps/summary", stepController::getStepSummary)
        app.delete("/api/users/{user-id}/steps", stepController::deleteStepsByUserId)
// global step routes (by step id)
        app.get("/api/steps/{step-id}", stepController::getStepById)
        app.patch("/api/steps/{step-id}", stepController::updateStep)
        app.delete("/api/steps/{step-id}", stepController::deleteStepById)


//sleep routes
// user-scoped
        app.post("/api/users/{user-id}/sleep", sleepController::addSleep)
        app.get("/api/users/{user-id}/sleep", sleepController::getSleepByUserId)
        app.get("/api/users/{user-id}/sleep/summary", sleepController::getSleepSummary)
        app.delete("/api/users/{user-id}/sleep", sleepController::deleteSleepByUserId)

// global CRUD
        app.get("/api/sleep/{sleep-id}", sleepController::getSleepById)
        app.patch("/api/sleep/{sleep-id}", sleepController::updateSleep)
        app.delete("/api/sleep/{sleep-id}", sleepController::deleteSleepById)


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
        app.get("/api/activities/{activity-id}", activityController::getActivitiesByActivityId)
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


