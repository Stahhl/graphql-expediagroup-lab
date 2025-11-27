package com.example.graphqldemo.app.app

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.example.graphqldemo.app.BookEntity
import com.example.graphqldemo.app.Repository
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.collections.set

interface CreateBookUseCase : UseCase<CreateBookUseCase.Input, CreateBookUseCase.Error, BookEntity> {
    data class Input(
        val title: String,
        val authorId: String,
    )

    data class Error(
        val message: String,
    )
}

@Service
class CreateBookUseCaseImpl(
    private val repository: Repository,
) : CreateBookUseCase {
    override fun execute(input: CreateBookUseCase.Input): Either<CreateBookUseCase.Error, BookEntity> =
        either {
            ensureNotNull(repository.authors[input.authorId]) {
                CreateBookUseCase.Error("Author ${input.authorId} not found")
            }
            val book = BookEntity(bookId = UUID.randomUUID().toString(), title = input.title, authorId = input.authorId)
            repository.books[book.bookId] = book
            book
        }
}
