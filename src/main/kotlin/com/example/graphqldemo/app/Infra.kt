package com.example.graphqldemo.app

import org.springframework.stereotype.Component

interface NamedService {
    fun getName(): String
}

@Component
class Repository {
    val books =
        mutableMapOf(
            "book1" to Book(bookId = "book1", title = "imse vimse spindel", authorId = "author1"),
            "book2" to Book(bookId = "book2", title = "bamses äventyr", authorId = "author1"),
            "book3" to Book(bookId = "book3", title = "norgeskämmt vol 11", authorId = "author2"),
        )

    val authors =
        mutableMapOf(
            "author1" to
                Author(
                    authorId = "author1",
                    name = "John Doe",
                    petId = "pet1",
                    booksIds = mutableListOf("book1", "book2"),
                ),
            "author2" to
                Author(
                    authorId = "author2",
                    name = "Kalle McTest",
                    petId = "",
                    booksIds = mutableListOf("book3"),
                ),
        )

    val pets = mutableMapOf("pet1" to Pet("pet1", "Spot", PetType.Dog))

    val publishers =
        mutableMapOf(
            "publisher1" to
                Publisher(
                    "publisher1",
                    "Kalles Böcker AB",
                    booksIds = mutableListOf("book1", "book2", "book3"),
                ),
        )
}

@Component
class Service {
    val reviews =
        mutableMapOf(
            "review1" to Review("review1", "book1", "Kalle Anka", 3, "Helt ok..."),
            "review2" to Review("review2", "book2", "Arne Anka", 5, "Mästerverk!"),
        )
}
