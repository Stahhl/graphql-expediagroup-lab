package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.Book
import com.example.graphqldemo.app.Repository
import com.example.graphqldemo.app.Service
import com.expediagroup.graphql.server.operations.Mutation
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.collections.set

@Component
class BookMutation(
    private val repository: Repository,
) : Mutation {
    fun createBook(payload: BookPayload): BookResponse {
        println("createBook *******************************")
        val id = UUID.randomUUID().toString()
        val book = Book(id, payload.title, payload.authorId)
        repository.books[id] = book
        return BookResponse(book)
    }
}
