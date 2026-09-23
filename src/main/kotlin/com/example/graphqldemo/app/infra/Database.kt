package com.example.graphqldemo.app.infra

import com.example.graphqldemo.app.domain.AuthorEntity
import com.example.graphqldemo.app.domain.BookEntity
import com.example.graphqldemo.app.domain.Email
import com.example.graphqldemo.app.domain.PetEntity
import com.example.graphqldemo.app.domain.PetEntityType
import com.example.graphqldemo.app.domain.PublisherEntity
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * Mockad databas. Läs/skriv sker samtidigt från flera requests under WebFlux,
 * därav ConcurrentHashMap.
 *
 * Alla find*-metoder är batchade och tar en lista nycklar. Det är den signatur
 * en DataLoader behöver, och den speglar hur ett riktigt datalager bör anropas.
 */
@Repository
class Database {
    val books: ConcurrentHashMap<String, BookEntity> =
        ConcurrentHashMap(
            mapOf(
                "book1" to BookEntity("book1", "imse vimse spindel", authorId = "author1", publisherId = "publisher1"),
                "book2" to BookEntity("book2", "bamses äventyr", authorId = "author1", publisherId = "publisher1"),
                "book3" to BookEntity("book3", "norgeskämmt vol 11", authorId = "author2", publisherId = "publisher1"),
            ),
        )

    val authors: ConcurrentHashMap<String, AuthorEntity> =
        ConcurrentHashMap(
            mapOf(
                "author1" to
                    AuthorEntity(
                        authorId = "author1",
                        name = "John Doe",
                        email = Email.trusted("john@author1.com"),
                        petId = "pet1",
                    ),
                "author2" to
                    AuthorEntity(
                        authorId = "author2",
                        name = "Kalle McTest",
                        email = Email.trusted("kalle@author2.com"),
                        petId = null,
                    ),
            ),
        )

    val pets: ConcurrentHashMap<String, PetEntity> =
        ConcurrentHashMap(mapOf("pet1" to PetEntity("pet1", "Spot", PetEntityType.Dog)))

    val publishers: ConcurrentHashMap<String, PublisherEntity> =
        ConcurrentHashMap(mapOf("publisher1" to PublisherEntity("publisher1", "Kalles Böcker AB")))

    fun findAuthors(authorIds: Set<String>): Map<String, AuthorEntity> = authorIds.mapNotNull { authors[it] }.associateBy { it.authorId }

    fun findPets(petIds: Set<String>): Map<String, PetEntity> = petIds.mapNotNull { pets[it] }.associateBy { it.petId }

    fun findPublishers(publisherIds: Set<String>): Map<String, PublisherEntity> =
        publisherIds.mapNotNull { publishers[it] }.associateBy { it.publisherId }

    fun findBooksByAuthor(authorIds: Set<String>): Map<String, List<BookEntity>> {
        val byAuthor = books.values.groupBy { it.authorId }
        return authorIds.associateWith { byAuthor[it].orEmpty() }
    }

    fun findBooksByPublisher(publisherIds: Set<String>): Map<String, List<BookEntity>> {
        val byPublisher = books.values.groupBy { it.publisherId }
        return publisherIds.associateWith { byPublisher[it].orEmpty() }
    }
}
