package ie.setu.controllers

import ie.setu.domain.Sleep
import ie.setu.domain.repository.SleepDAO
import ie.setu.utils.jsonToObject
import io.javalin.http.Context

class SleepController {

    private val sleepDao = SleepDAO()

    fun addSleep(ctx: Context) {
        val sleep: Sleep = jsonToObject(ctx.body())

        // basic validation
        if (sleep.duration <= 0 ||
            sleep.quality !in 1..10 ||
            sleep.date.isBlank()
        ) {
            ctx.status(400)
            return
        }

        val id = sleepDao.save(sleep)

        if (id != null) {
            ctx.json(sleep.copy(id = id))
            ctx.status(201)
        } else {
            ctx.status(400)
        }
    }

    fun updateSleep(ctx: Context) {
        val id = ctx.pathParam("sleep-id").toInt()
        val updatedSleep: Sleep = jsonToObject(ctx.body())

        // basic validation
        if (updatedSleep.duration <= 0 ||
            updatedSleep.quality !in 1..10 ||
            updatedSleep.date.isBlank()
        ) {
            ctx.status(400)
            return
        }

        val rows = sleepDao.update(id, updatedSleep)
        if (rows == 1) ctx.status(204)
        else ctx.status(404)
    }


    fun getSleepByUserId(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()
        val sleeps = sleepDao.findByUserId(userId)

        if (sleeps.isNotEmpty()) {
            ctx.json(sleeps)
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun getSleepSummary(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()
        val summary = sleepDao.findSummary(userId)

        if (summary.isNotEmpty()) {
            ctx.json(summary)
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun deleteSleepByUserId(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()
        val deleted = sleepDao.deleteByUserId(userId)

        if (deleted != 0) ctx.status(204)
        else ctx.status(404)
    }

    fun getSleepById(ctx: Context) {
        val id = ctx.pathParam("sleep-id").toInt()
        val sleep = sleepDao.findById(id)

        if (sleep != null) {
            ctx.json(sleep)
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun deleteSleepById(ctx: Context) {
        val id = ctx.pathParam("sleep-id").toInt()
        val rows = sleepDao.deleteById(id)

        if (rows == 1) ctx.status(204)
        else ctx.status(404)
    }
}
