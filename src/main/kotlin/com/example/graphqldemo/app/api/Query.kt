package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.Repository
import com.example.graphqldemo.app.Service
import com.expediagroup.graphql.server.operations.Query
import org.springframework.stereotype.Component

@Component
class BookQuery(
    private val repository: Repository,
    private val service: Service,
) : Query {
    fun getBook(id: String): BookResponse? {
        println("getBook ****************************")
        val book = repository.books[id] ?: return null
        return BookResponse(book, repository, service)
    }

    fun getBooks(): List<BookResponse> {
        println("getBooks ***************************")
        return repository.books.values.map { BookResponse(it, repository, service) }
    }
}
