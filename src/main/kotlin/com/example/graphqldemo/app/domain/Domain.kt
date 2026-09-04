package com.example.graphqldemo.app.domain

data class BookEntity(
    val bookId: String,
    val title: String,
    val authorId: String,
)

data class AuthorEntity(
    val authorId: String,
    val name: String,
    val email: String,
    val petId: String? = null,
    val booksIds: MutableList<String> = mutableListOf(),
)

data class PetEntity(
    val petId: String,
    val name: String,
    val type: PetEntityType,
)

enum class PetEntityType {
    Dog,
    Cat,
}

data class PublisherEntity(
    val publisherId: String,
    val name: String,
    val booksIds: MutableList<String> = mutableListOf(),
)

data class ReviewEntity(
    val reviewId: String,
    val booksId: String,
    val reviewer: String,
    val stars: Int,
    val text: String,
)
