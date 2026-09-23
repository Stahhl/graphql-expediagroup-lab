package com.example.graphqldemo.app.domain

data class BookEntity(
    val bookId: String,
    val title: String,
    val authorId: String,
    val publisherId: String? = null,
)

data class AuthorEntity(
    val authorId: String,
    val name: String,
    val email: Email,
    val petId: String? = null,
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
)

data class ReviewEntity(
    val reviewId: String,
    val bookId: String,
    val reviewer: String,
    val stars: Int,
    val text: String,
)
