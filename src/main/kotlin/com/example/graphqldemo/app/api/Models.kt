package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.*

data class BookPayload(
    val title: String,
    val authorId: String,
)

data class BookResponse(
    private val book: Book,
    private val repository: Repository,
    private val service: Service,
) {
    val bookId: String get() = book.bookId
    val title: String get() = book.title

    fun author(): AuthorResponse? {
        println("BookResponse.author called!")
        val author = repository.authors[book.authorId] ?: return null
        return AuthorResponse(author, repository, service)
    }

    fun reviews(): List<ReviewResponse> {
        println("BookResponse.reviews called!")
        val reviews = service.reviews.filter { it.value.booksId == book.bookId }.map { it.value }
        return reviews.map { ReviewResponse(it.reviewer, it.stars, it.text) }
    }

    fun publisher(): PublisherResponse {
        println("BookResponse.publisher called!")
        val publisher =
            repository.publishers
                .filter { it.value.booksIds.contains(book.bookId) }
                .map { it.value }
                .single()
        return PublisherResponse(publisher, repository, service)
    }
}

data class AuthorResponse(
    private val author: Author,
    private val repository: Repository,
    private val service: Service,
) {
    val name: String get() = author.name

    fun pet(): PetResponse? {
        val pet = repository.pets[author.petId] ?: return null
        return when (pet.type) {
            PetType.Dog -> Dog(pet.name)
            PetType.Cat -> Cat(pet.name)
        }
    }

    fun books(): List<BookResponse?> {
        println("AuthorResponse.books called!")
        val books = author.booksIds.map { repository.books[it] }
        return books.map { BookResponse(it!!, repository, service) }
    }
}

data class PublisherResponse(
    private val publisher: Publisher,
    private val repository: Repository,
    private val service: Service,
) {
    val name: String get() = publisher.name

    fun books(): List<BookResponse?> {
        println("PublisherResponse.books called!")
        val books = publisher.booksIds.map { repository.books[it] }
        return books.map { BookResponse(it!!, repository, service) }
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
