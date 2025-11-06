package com.example.graphqldemo

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.util.UUID

@SpringBootApplication
class GraphqldemoApplication

fun main(args: Array<String>) {
    runApplication<GraphqldemoApplication>(*args)
}

data class Book(val isbn: String, val title: String, val author: String)

data class BookPayload(val title: String, val author: String)

@Component
class BookQuery(val bookRepository: BookRepository) : Query {
    fun getBook(id: String): Book? = bookRepository.books[id]

    fun getBooks(): List<Book> = bookRepository.books.values.toList()
}

@Component
class BookMutation(val bookRepository: BookRepository) : Mutation {
    fun createBook(payload: BookPayload): Book {
        val id = UUID.randomUUID().toString()
        val book = Book(id, payload.title, payload.author)
        bookRepository.books[id] = book
        return book
    }
}

@Component
class BookRepository {
    val books = mutableMapOf<String, Book>(Pair("1", Book(isbn = "1", title = "foo", author = "Bar")))
}

