package com.example.graphqldemo.app.app

import arrow.core.Either
import com.example.graphqldemo.app.domain.AuthorEntity
import com.example.graphqldemo.app.infra.Repository
import org.springframework.stereotype.Service
import java.util.*

interface CreateAuthorUseCase : UseCase<CreateAuthorUseCase.Input, CreateAuthorUseCase.Error, AuthorEntity> {
    data class Input(
        val name: String,
        val email: String,
    )

    data class Error(
        val message: String,
    )
}

@Service
class CreateAuthorUseCaseImpl(
    private val repository: Repository,
) : CreateAuthorUseCase {
    override fun execute(input: CreateAuthorUseCase.Input): Either<CreateAuthorUseCase.Error, AuthorEntity> {
        val author =
            AuthorEntity(
                authorId = UUID.randomUUID().toString(),
                name = input.name,
                email = input.email,
            )
        repository.authors[author.authorId] = author
        return Either.Right(author)
    }
}
