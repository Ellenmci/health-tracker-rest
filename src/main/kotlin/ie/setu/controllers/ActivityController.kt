package ie.setu.controllers

import ie.setu.domain.Activity
import ie.setu.domain.repository.ActivityDAO
import ie.setu.domain.repository.UserDAO
import ie.setu.utils.jsonToObject
import io.javalin.http.Context

class ActivityController {

    private val activityDAO = ActivityDAO()
    private val userDao = UserDAO()

    fun getAllActivities(ctx: Context) {
        val activities = activityDAO.getAll()
        if (activities.isNotEmpty()) {
            ctx.status(200)
        } else {
            ctx.status(404)
        }
        ctx.json(activities)
    }

    fun getActivitiesByUserId(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()

        if (userDao.findById(userId) != null) {
            val activities = activityDAO.findByUserId(userId)
            if (activities.isNotEmpty()) {
                ctx.json(activities)
                ctx.status(200)
            } else {
                ctx.status(404)
            }
        } else {
            ctx.status(404)
        }
    }

    fun getActivitiesByActivityId(ctx: Context) {
        val activity = activityDAO.findByActivityId(ctx.pathParam("activity-id").toInt())
        if (activity != null) {
            ctx.json(activity)
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun addActivity(ctx: Context) {
        val activity: Activity = jsonToObject(ctx.body())
        val user = userDao.findById(activity.userId)

        if (user != null) {
            val activityId = activityDAO.save(activity)
            activity.id = activityId
            ctx.json(activity)
            ctx.status(201)
        } else {
            ctx.status(404)
        }
    }

    fun deleteActivityByActivityId(ctx: Context) {
        if (activityDAO.deleteByActivityId(ctx.pathParam("activity-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    fun deleteActivityByUserId(ctx: Context) {
        if (activityDAO.deleteByUserId(ctx.pathParam("user-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    fun updateActivity(ctx: Context) {
        val activity: Activity = jsonToObject(ctx.body())
        if (activityDAO.updateByActivityId(
                ctx.pathParam("activity-id").toInt(),
                activity
            ) != 0
        ) {
            ctx.status(204)
        } else {
            ctx.status(404)
        }
    }
}
