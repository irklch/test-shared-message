package com.irklch.message

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class MessageResponse(val text: String)

@Serializable
data class OperationResult(val success: Boolean, val error: String? = null)

fun Application.configureRouting() {
    connectToDatabase()

    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/message") {
            val id: Int? = call.request.queryParameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, OperationResult(false, "Некорректный id"))
                return@get
            }

            val foundText: MessageResponse? = transaction {
                Messages.selectAll().where { Messages.id eq id }
                    .map { MessageResponse(it[Messages.text]) }
                    .firstOrNull()
            }

            if (foundText == null) {
                call.respond(HttpStatusCode.NotFound, OperationResult(false, "Запись не найдена"))
                return@get
            }

            call.respond(foundText)
        }

        get("/messages") {
            val allTextes: List<MessageResponse> = transaction {
                Messages.selectAll()
                    .orderBy(Messages.id, SortOrder.ASC)
                    .map { MessageResponse(it[Messages.text]) }
            }
            call.respond(allTextes)
        }

        post("/message") {
            val newMessage = call.receive<MessageResponse>()
            val success = try {
                transaction {
                    Messages.insert {
                        it[text] = newMessage.text
                    }
                }
                true
            } catch (_: Exception) {
                false
            }

            call.respond(OperationResult(success))
        }
    }
}