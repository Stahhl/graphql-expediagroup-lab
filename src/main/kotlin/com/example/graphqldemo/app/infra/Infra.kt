package com.example.graphqldemo.app.infra

import com.example.graphqldemo.app.domain.AuthorEntity
import com.example.graphqldemo.app.domain.BookEntity
import com.example.graphqldemo.app.domain.PetEntity
import com.example.graphqldemo.app.domain.PetEntityType
import com.example.graphqldemo.app.domain.PublisherEntity
import com.example.graphqldemo.app.domain.ReviewEntity
import org.springframework.stereotype.Service

@Service
class Repository {
    val books =
        mutableMapOf(
            "book1" to BookEntity(bookId = "book1", title = "imse vimse spindel", authorId = "author1"),
            "book2" to BookEntity(bookId = "book2", title = "bamses äventyr", authorId = "author1"),
            "book3" to BookEntity(bookId = "book3", title = "norgeskämmt vol 11", authorId = "author2"),
        )

    val authors =
        mutableMapOf(
            "author1" to
                    AuthorEntity(
                        authorId = "author1",
                        name = "John Doe",
                        petId = "pet1",
                        email = "john@author1.com",
                        booksIds = mutableListOf("book1", "book2"),
                    ),
            "author2" to
                    AuthorEntity(
                        authorId = "author2",
                        name = "Kalle McTest",
                        petId = "",
                        email = "kalle@author2.com",
                        booksIds = mutableListOf("book3"),
                    ),
        )

    val pets = mutableMapOf("pet1" to PetEntity("pet1", "Spot", PetEntityType.Dog))

    val publishers =
        mutableMapOf(
            "publisher1" to
                    PublisherEntity(
                        "publisher1",
                        "Kalles Böcker AB",
                        booksIds = mutableListOf("book1", "book2", "book3"),
                    ),
        )
}

@Service
class Service {
    val reviews =
        mutableMapOf(
            "review1" to ReviewEntity("review1", "book1", "Kalle Anka", 3, "Helt ok..."),
            "review2" to ReviewEntity("review2", "book2", "Arne Anka", 5, "Mästerverk!"),
        )
}
