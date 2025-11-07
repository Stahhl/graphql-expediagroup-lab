package com.example.graphqldemo.app

data class Book(
    val bookId: String,
    val title: String,
    val authorId: String,
)

data class Author(
    val authorId: String,
    val name: String,
    val petId: String?,
    val booksIds: MutableList<String> = mutableListOf(),
)

data class Pet(
    val petId: String,
    val name: String,
    val type: PetType,
)

data class Publisher(
    val publisherId: String,
    val name: String,
    val booksIds: MutableList<String> = mutableListOf(),
)

data class Review(
    val reviewId: String,
    val booksId: String,
    val reviewer: String,
    val stars: Int,
    val text: String,
)

enum class PetType {
    Dog,
    Cat,
}
