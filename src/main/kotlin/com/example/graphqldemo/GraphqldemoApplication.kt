package com.example.graphqldemo

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.util.*

@SpringBootApplication
class GraphqldemoApplication

fun main(args: Array<String>) {
    runApplication<GraphqldemoApplication>(*args)
}

data class Book(

    val isbn: String,

    val title: String,

    @GraphQLIgnore
    val authorId: String

) {
//    fun author(@GraphQLIgnore authorRepository: AuthorRepository): Author? =
//        authorRepository.authors[authorId]
}

data class Author(val id: String, val name: String)

data class BookPayload(val title: String, val authorId: String)


@Component
class BookQuery(val bookRepository: BookRepository) : Query {
    fun getBook(id: String): Book? = bookRepository.books[id]

    fun getBooks(): List<Book> = bookRepository.books.values.toList()
}

@Component
class BookMutation(val bookRepository: BookRepository) : Mutation {
    fun createBook(payload: BookPayload): Book {
        val id = UUID.randomUUID().toString()
        val book = Book(id, payload.title, payload.authorId)
        bookRepository.books[id] = book
        return book
    }
}

@Component
class BookRepository {
    val books = mutableMapOf<String, Book>("1" to Book(isbn = "1", title = "foo", authorId = "author1"))
}

@Component
class AuthorRepository {
    val authors = mutableMapOf<String, Author>("author1" to Author(id = "author1", name = "John Doe"))
}
//
@Component("AuthorDataFetcher")
class AuthorDataFetcher(val authorRepository: AuthorRepository) : DataFetcher<Author> {
    override fun get(environment: DataFetchingEnvironment): Author {
        println("AuthorDataFetcher")
        val authorId = environment.getSource<Book>()?.authorId
        println(authorId)
        return authorRepository.authors[authorId]!!
    }
}

//@Component
//class BookResolver(private val authorRepository: AuthorRepository) {
//    init { println("BookResolver bean created") }
//    fun Book.author(): Author? = authorRepository.authors[this.authorId]
//}