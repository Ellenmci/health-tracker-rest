package ie.setu.controllers

import ie.setu.domain.User
import ie.setu.domain.repository.UserDAO
import ie.setu.utils.jsonToObject
import io.javalin.http.Context

class UserController {

    private val userDao = UserDAO()

    fun getAllUsers(ctx: Context) {
        val users = userDao.getAll()
        if (users.isNotEmpty()) {
            ctx.status(200)
        } else {
            ctx.status(404)
        }
        ctx.json(users)
    }

    fun getUserByUserId(ctx: Context) {
        val user = userDao.findById(ctx.pathParam("user-id").toInt())
        if (user != null) {
            ctx.json(user)
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun getUserByEmail(ctx: Context) {
        val user = userDao.findByEmail(ctx.pathParam("email"))
        if (user != null) {
            ctx.json(user)
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun addUser(ctx: Context) {
        var user: User = jsonToObject(ctx.body())   // ← must be var
        val userId = userDao.save(user)

        if (userId != null) {
            user.id = userId
            ctx.json(user)
            ctx.status(201)
        }
    }

    fun deleteUser(ctx: Context) {
        if (userDao.delete(ctx.pathParam("user-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    fun updateUser(ctx: Context) {
        val foundUser: User = jsonToObject(ctx.body())
        if (userDao.update(ctx.pathParam("user-id").toInt(), foundUser) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }
}
