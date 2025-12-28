package ie.setu.controllers

import ie.setu.domain.Step
import ie.setu.domain.repository.StepDAO
import ie.setu.utils.jsonToObject
import io.javalin.http.Context

class StepController {

    private val stepDao = StepDAO()

    fun addSteps(ctx: Context) {
        val step: Step = jsonToObject(ctx.body())

        // basic validation
        if (step.steps <= 0 || step.date.isBlank()) {
            ctx.status(400)
            return
        }

        val id = stepDao.save(step)

        if (id != null) {
            val createdStep = step.copy(id = id)
            ctx.json(createdStep)
            ctx.status(201)
        } else {
            ctx.status(400)
        }
    }

    fun updateStep(ctx: Context) {
        val id = ctx.pathParam("step-id").toInt()
        val updatedStep: Step = jsonToObject(ctx.body())

        // basic validation
        if (updatedStep.steps <= 0 || updatedStep.date.isBlank()) {
            ctx.status(400)
            return
        }

        val rows = stepDao.update(id, updatedStep)
        if (rows == 1) {
            ctx.status(204)
        } else {
            ctx.status(404)
        }
    }


    fun getStepsByUserId(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()
        val steps = stepDao.findByUserId(userId)

        if (steps.isNotEmpty()) {
            ctx.json(steps)
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun getStepSummary(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()
        val summary = stepDao.findSummary(userId)

        if (summary.isNotEmpty()) {
            ctx.json(summary)
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun deleteStepsByUserId(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()
        val deleted = stepDao.deleteByUserId(userId)

        if (deleted != 0) {
            ctx.status(204)
        } else {
            ctx.status(404)
        }
    }

    fun getStepById(ctx: Context) {
        val id = ctx.pathParam("step-id").toInt()
        val step = stepDao.findById(id)

        if (step != null) {
            ctx.json(step)
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun deleteStepById(ctx: Context) {
        val id = ctx.pathParam("step-id").toInt()
        val rows = stepDao.deleteById(id)

        if (rows == 1) {
            ctx.status(204)
        } else {
            ctx.status(404)
        }
    }
}
