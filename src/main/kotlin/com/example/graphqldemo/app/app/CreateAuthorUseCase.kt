package com.example.graphqldemo.app.app

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.example.graphqldemo.app.domain.AuthorEntity
import com.example.graphqldemo.app.domain.Email
import com.example.graphqldemo.app.infra.Database
import org.springframework.stereotype.Service
import java.util.UUID

interface CreateAuthorUseCase : UseCase<CreateAuthorUseCase.Input, CreateAuthorUseCase.Error, AuthorEntity> {
    data class Input(
        val name: String,
        val email: Email,
    )

    data class Error(
        val message: String,
    )
}

@Service
class CreateAuthorUseCaseImpl(
    private val database: Database,
) : CreateAuthorUseCase {
    override fun execute(input: CreateAuthorUseCase.Input): Either<CreateAuthorUseCase.Error, AuthorEntity> =
        either {
            ensure(input.name.isNotBlank()) { CreateAuthorUseCase.Error("Name must not be blank") }
            ensure(database.authors.values.none { it.email == input.email }) {
                CreateAuthorUseCase.Error("Author with email ${input.email} already exists")
            }
            val author =
                AuthorEntity(
                    authorId = UUID.randomUUID().toString(),
                    name = input.name,
                    email = input.email,
                )
            database.authors[author.authorId] = author
            author
        }
}
