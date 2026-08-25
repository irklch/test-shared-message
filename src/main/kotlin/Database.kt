package com.irklch.message

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Messages: Table("messages") {
    val id = integer("id").autoIncrement()
    val text = varchar("text", 255)
    override val primaryKey= PrimaryKey(id)
}

fun connectToDatabase() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "testpassword"
    )

    transaction {
        SchemaUtils.create(Messages)
        if (Messages.selectAll().empty()) {
            Messages.insert {
                it[text] = "Привет! Это тестовое сообщение."
            }
        }
    }
}