package com.example.graphqldemo.app.app

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.example.graphqldemo.app.Book
import com.example.graphqldemo.app.Repository
import com.example.graphqldemo.app.api.BookResponse
import org.springframework.stereotype.Service
import java.util.UUID

fun interface UseCase<Input, Error, Output> {
    fun execute(input: Input): Either<Error, Output>
}

fun <Input, Output> UseCase<Input, Error, Output>.execute(input: Input): Either<Error, Output> = execute(input)

interface CreateBook : UseCase<CreateBook.Input, CreateBook.Error, BookResponse> {
    data class Input(
        val title: String,
        val authorId: String,
    )

    data class Error(
        val message: String,
    )
}

@Service
class CreateBookImpl(
    private val repository: Repository,
) : CreateBook {
    override fun execute(input: CreateBook.Input): Either<CreateBook.Error, BookResponse> =
        either {
            ensureNotNull(repository.authors[input.authorId]) {
                CreateBook.Error("Author ${input.authorId} not found")
            }
            val book = Book(bookId = UUID.randomUUID().toString(), title = input.title, authorId = input.authorId)
            repository.books[book.bookId] = book
            BookResponse(book = book)
        }
}
