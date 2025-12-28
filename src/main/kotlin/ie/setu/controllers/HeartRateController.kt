package ie.setu.controllers

import ie.setu.domain.HeartRate
import ie.setu.domain.repository.HeartRateDAO
import ie.setu.domain.repository.UserDAO
import ie.setu.utils.jsonToObject
import io.javalin.http.Context

class HeartRateController {

    private val heartRateDAO = HeartRateDAO()
    private val userDao = UserDAO()

    fun getAllHeartRates(ctx: Context) {
        val heartRates = heartRateDAO.getAll()
        if (heartRates.isNotEmpty()) {
            ctx.status(200)
        } else {
            ctx.status(404)
        }
        ctx.json(heartRates)
    }

    fun getHeartRateSummary(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()
        val summary = heartRateDAO.findSummary(userId)

        if (summary.isEmpty()) {
            ctx.status(404)
        } else {
            ctx.json(summary)
            ctx.status(200)
        }
    }

    fun getHeartRatesByUserId(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()

        if (userDao.findById(userId) != null) {
            val heartRates = heartRateDAO.findByUserId(userId)
            if (heartRates.isNotEmpty()) {
                ctx.json(heartRates)
                ctx.status(200)
            } else {
                ctx.status(404)
            }
        } else {
            ctx.status(404)
        }
    }

    fun getHeartRateById(ctx: Context) {
        val heartRate = heartRateDAO.findById(ctx.pathParam("heartrate-id").toInt())
        if (heartRate != null) {
            ctx.json(heartRate)
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun addHeartRate(ctx: Context) {
        val heartRate: HeartRate = jsonToObject(ctx.body())
        val user = userDao.findById(heartRate.userId)

        if (user != null) {
            val heartRateId = heartRateDAO.save(heartRate)
            if (heartRateId != null) {
                heartRate.id = heartRateId
                ctx.json(heartRate)
                ctx.status(201)
            } else {
                ctx.status(400)
            }
        } else {
            ctx.status(404)
        }
    }

    fun updateHeartRate(ctx: Context) {
        val heartRate: HeartRate = jsonToObject(ctx.body())
        if (heartRateDAO.update(
                ctx.pathParam("heartrate-id").toInt(),
                heartRate
            ) != 0
        ) {
            ctx.status(204)
        } else {
            ctx.status(404)
        }
    }

    fun deleteHeartRateById(ctx: Context) {
        if (heartRateDAO.delete(ctx.pathParam("heartrate-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    fun deleteHeartRatesByUserId(ctx: Context) {
        if (heartRateDAO.deleteByUserId(ctx.pathParam("user-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }
}
