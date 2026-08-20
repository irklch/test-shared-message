package com.irklch.message

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicReference

@Serializable
data class MessageResponse(val text: String)

private var sharedText = AtomicReference("Привет! Это текстовое сообщение")

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/message") {
            call.respond(MessageResponse(sharedText.get()))
        }

        put("/message") {
           val newMessage = call.receive<MessageResponse>()
            sharedText.set(newMessage.text)
            call.respond(MessageResponse(sharedText.get()))
        }
    }
}