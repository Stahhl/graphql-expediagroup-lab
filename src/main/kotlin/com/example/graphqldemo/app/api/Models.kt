package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.domain.AuthorEntity
import com.example.graphqldemo.app.domain.BookEntity
import com.example.graphqldemo.app.domain.Email
import com.example.graphqldemo.app.domain.PetEntity
import com.example.graphqldemo.app.domain.PetEntityType
import com.example.graphqldemo.app.domain.PublisherEntity
import com.example.graphqldemo.app.domain.ReviewEntity
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLName
import com.expediagroup.graphql.server.extensions.getValueFromDataLoader
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

/**
 * Api-typer wrappar en privat domäntyp och exponerar valda fält.
 *
 * Relationer laddas via DataLoader: ett anrop per nivå och unik nyckel i stället för
 * ett anrop per nod. Returtypen CompletableFuture packas upp av ramverket
 * (SchemaGeneratorHooks.willResolveMonad) och syns inte i schemat.
 */
@GraphQLName("Book")
@GraphQLDescription("A book in the catalogue")
class BookType(
    private val book: BookEntity,
) {
    val bookId: String get() = book.bookId

    val title: String get() = book.title

    @GraphQLDescription("The author who wrote this book")
    fun author(dfe: DataFetchingEnvironment): CompletableFuture<AuthorType?> =
        dfe
            .getValueFromDataLoader<String, AuthorEntity?>(AuthorDataLoader.NAME, book.authorId)
            .thenApply { it?.let(::AuthorType) }

    @GraphQLDescription("Reviews from the external review service")
    fun reviews(dfe: DataFetchingEnvironment): CompletableFuture<List<ReviewType>> =
        dfe
            .getValueFromDataLoader<String, List<ReviewEntity>>(ReviewsByBookDataLoader.NAME, book.bookId)
            .thenApply { reviews -> reviews.map { ReviewType(it.reviewer, it.stars, it.text) } }

    @GraphQLDescription("The publisher, or null if the book is unpublished")
    fun publisher(dfe: DataFetchingEnvironment): CompletableFuture<PublisherType?> {
        val publisherId = book.publisherId ?: return CompletableFuture.completedFuture(null)
        return dfe
            .getValueFromDataLoader<String, PublisherEntity?>(PublisherDataLoader.NAME, publisherId)
            .thenApply { it?.let(::PublisherType) }
    }
}

@GraphQLName("Author")
@GraphQLDescription("Someone who has written at least one book")
class AuthorType(
    private val author: AuthorEntity,
) {
    val name: String get() = author.name

    /** Domäntypen exponeras direkt — Hooks mappar den till scalar Email. */
    val email: Email get() = author.email

    fun pet(dfe: DataFetchingEnvironment): CompletableFuture<PetType?> {
        val petId = author.petId ?: return CompletableFuture.completedFuture(null)
        return dfe
            .getValueFromDataLoader<String, PetEntity?>(PetDataLoader.NAME, petId)
            .thenApply { pet -> pet?.let(::toPetType) }
    }

    fun books(dfe: DataFetchingEnvironment): CompletableFuture<List<BookType>> =
        dfe
            .getValueFromDataLoader<String, List<BookEntity>>(BooksByAuthorDataLoader.NAME, author.authorId)
            .thenApply { books -> books.map(::BookType) }
}

@GraphQLName("Publisher")
class PublisherType(
    private val publisher: PublisherEntity,
) {
    val name: String get() = publisher.name

    fun books(dfe: DataFetchingEnvironment): CompletableFuture<List<BookType>> =
        dfe
            .getValueFromDataLoader<String, List<BookEntity>>(BooksByPublisherDataLoader.NAME, publisher.publisherId)
            .thenApply { books -> books.map(::BookType) }
}

/**
 * Ett interface med minst ett fält blir en GraphQL interface. Vill man ha en union
 * ska typen sakna deklarerade fält och funktioner (se kClassExtensions.isDeclaredUnion).
 */
@GraphQLName("Pet")
interface PetType {
    val name: String
}

@GraphQLName("Dog")
class DogType(
    override val name: String,
) : PetType {
    fun woof(): String = "woof"
}

@GraphQLName("Cat")
class CatType(
    override val name: String,
) : PetType {
    fun meow(): String = "meow"
}

private fun toPetType(pet: PetEntity): PetType =
    when (pet.type) {
        PetEntityType.Dog -> DogType(pet.name)
        PetEntityType.Cat -> CatType(pet.name)
    }

@GraphQLName("Review")
class ReviewType(
    val reviewer: String,
    @GraphQLDescription("Rating from 1 to 5")
    val stars: Int,
    val text: String,
)
