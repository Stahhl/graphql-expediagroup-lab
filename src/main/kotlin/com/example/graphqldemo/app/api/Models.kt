package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.*
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import org.springframework.beans.factory.annotation.Autowired

data class BookType(
    private val book: BookEntity,
) {
    val bookId: String get() = book.bookId
    val title: String get() = book.title

    fun author(
        @GraphQLIgnore @Autowired repo: Repository,
    ): AuthorType? {
        println("BookResponse.author called!")
        val author = repo.authors[book.authorId] ?: return null
        return AuthorType(author)
    }

    fun reviews(
        @GraphQLIgnore @Autowired service: Service,
    ): List<ReviewType> {
        println("BookResponse.reviews called!")
        val reviews = service.reviews.filter { it.value.booksId == book.bookId }.map { it.value }
        return reviews.map { ReviewType(it.reviewer, it.stars, it.text) }
    }

    fun publisher(
        @GraphQLIgnore @Autowired repo: Repository,
    ): PublisherType {
        println("BookResponse.publisher called!")
        val publisher =
            repo.publishers
                .filter { it.value.booksIds.contains(book.bookId) }
                .map { it.value }
                .single()
        return PublisherType(publisher)
    }
}

data class AuthorType(
    private val author: AuthorEntity,
) {
    val name: String get() = author.name

//    val email: String get() = author.email
    val email: EmailScalarType get() = EmailScalarType(author.email)

    fun pet(
        @GraphQLIgnore @Autowired repo: Repository,
    ): PetType? {
        println("AuthorResponse.pet called!")
        val pet = repo.pets[author.petId] ?: return null
        return when (pet.type) {
            PetEntityType.Dog -> DogType(pet.name)
            PetEntityType.Cat -> CatType(pet.name)
        }
    }

    fun books(
        @GraphQLIgnore @Autowired repo: Repository,
    ): List<BookType?> {
        println("AuthorResponse.books called!")
        val books = author.booksIds.map { repo.books[it] }
        return books.map { BookType(it!!) }
    }
}

data class PublisherType(
    private val publisher: PublisherEntity,
) {
    val name: String get() = publisher.name

    fun books(
        @GraphQLIgnore @Autowired repo: Repository,
    ): List<BookType?> {
        println("PublisherResponse.books called!")
        val books = publisher.booksIds.map { repo.books[it] }
        return books.map { BookType(it!!) }
    }
}

abstract class PetType {
    abstract val name: String
}

data class DogType(
    override val name: String,
) : PetType() {
    fun woof(): String = "woof"
}

data class CatType(
    override val name: String,
) : PetType() {
    fun meow(): String = "meow"
}

data class ReviewType(
    val reviewer: String,
    val stars: Int,
    val text: String,
)
