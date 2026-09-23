package com.example.graphqldemo.app.app

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.example.graphqldemo.app.domain.BookEntity
import com.example.graphqldemo.app.infra.Database
import org.springframework.stereotype.Service
import java.util.UUID

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
    private val database: Database,
) : CreateBookUseCase {
    override fun execute(input: CreateBookUseCase.Input): Either<CreateBookUseCase.Error, BookEntity> =
        either {
            ensure(input.title.isNotBlank()) { CreateBookUseCase.Error("Title must not be blank") }
            ensureNotNull(database.authors[input.authorId]) {
                CreateBookUseCase.Error("Author ${input.authorId} not found")
            }
            val book =
                BookEntity(
                    bookId = UUID.randomUUID().toString(),
                    title = input.title,
                    authorId = input.authorId,
                )
            database.books[book.bookId] = book
            book
        }
}
