package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.*
import com.expediagroup.graphql.generator.extensions.plus
import com.expediagroup.graphql.server.spring.execution.DefaultSpringGraphQLContextFactory
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

data class BookPayload(
    val title: String,
    val authorId: String,
)

@Component
class MyGraphQLContextFactory(
    private val repository: Repository,
    private val service: Service,
) : DefaultSpringGraphQLContextFactory() {
    override suspend fun generateContext(request: ServerRequest): GraphQLContext =
        super.generateContext(request) +
            mapOf(
                Service::class.simpleName!! to service,
                Repository::class.simpleName!! to repository,
            )
}

inline fun <reified T> DataFetchingEnvironment.getService(): T {
    val key = T::class.simpleName ?: error("Anonymous class?")
    return graphQlContext.get<T>(key)
        ?: error("GraphQLContext: service '$key' is not registered")
}

data class BookResponse(
    private val book: Book,
) {
    val bookId: String get() = book.bookId
    val title: String get() = book.title

    fun author(dfe: DataFetchingEnvironment): AuthorResponse? {
        println("BookResponse.author called!")
        val repo = dfe.getService<Repository>()
        val author = repo.authors[book.authorId] ?: return null
        return AuthorResponse(author)
    }

    fun reviews(dfe: DataFetchingEnvironment): List<ReviewResponse> {
        println("BookResponse.reviews called!")
        val service = dfe.getService<Service>()
        val reviews = service.reviews.filter { it.value.booksId == book.bookId }.map { it.value }
        return reviews.map { ReviewResponse(it.reviewer, it.stars, it.text) }
    }

    fun publisher(dfe: DataFetchingEnvironment): PublisherResponse {
        println("BookResponse.publisher called!")
        val repo = dfe.getService<Repository>()
        val publisher =
            repo.publishers
                .filter { it.value.booksIds.contains(book.bookId) }
                .map { it.value }
                .single()
        return PublisherResponse(publisher)
    }
}

data class AuthorResponse(
    private val author: Author,
) {
    val name: String get() = author.name

    fun pet(dfe: DataFetchingEnvironment): PetResponse? {
        println("AuthorResponse.pet called!")
        val repo = dfe.getService<Repository>()
        val pet = repo.pets[author.petId] ?: return null
        return when (pet.type) {
            PetType.Dog -> Dog(pet.name)
            PetType.Cat -> Cat(pet.name)
        }
    }

    fun books(dfe: DataFetchingEnvironment): List<BookResponse?> {
        println("AuthorResponse.books called!")
        val repo = dfe.getService<Repository>()
        val books = author.booksIds.map { repo.books[it] }
        return books.map { BookResponse(it!!) }
    }
}

data class PublisherResponse(
    private val publisher: Publisher,
) {
    val name: String get() = publisher.name

    fun books(dfe: DataFetchingEnvironment): List<BookResponse?> {
        println("PublisherResponse.books called!")
        val repo = dfe.getService<Repository>()
        val books = publisher.booksIds.map { repo.books[it] }
        return books.map { BookResponse(it!!) }
    }
}

abstract class PetResponse {
    abstract val name: String
}

data class Dog(
    override val name: String,
) : PetResponse() {
    fun woof(): String = "woof"
}

data class Cat(
    override val name: String,
) : PetResponse() {
    fun meow(): String = "meow"
}

data class ReviewResponse(
    val reviewer: String,
    val stars: Int,
    val text: String,
)

// @Component
// class BookResolver(
//    private val repository: Repository,
//    private val service: Service,
// ) : GraphQLResolver<Book> {
//    fun author(book: Book): AuthorResponse? {
//        val author = repository.authors[book.authorId] ?: return null
//        return AuthorResponse(author, repository, service)
//    }
//
//    fun reviews(book: Book): List<ReviewResponse> {
//        val reviews = service.reviews.values.filter { it.booksId == book.bookId }
//        return reviews.map { ReviewResponse(it.reviewer, it.stars, it.text) }
//    }
//
//    fun publisher(book: Book): PublisherResponse {
//        val publisher =
//            repository.publishers.values.single { it.booksIds.contains(book.bookId) }
//        return PublisherResponse(publisher, repository, service)
//    }
// }
