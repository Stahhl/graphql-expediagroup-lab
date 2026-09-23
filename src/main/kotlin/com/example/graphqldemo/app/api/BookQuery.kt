package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.infra.Database
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import org.springframework.stereotype.Component

@Component
class BookQuery(
    private val database: Database,
) : Query {
    @GraphQLDescription("Look up a single book by id")
    fun book(id: String): BookType? = database.books[id]?.let(::BookType)

    @GraphQLDescription("All books in the catalogue")
    fun books(): List<BookType> = database.books.values.map(::BookType)
}
