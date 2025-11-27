package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.app.CreateAuthorUseCase
import com.expediagroup.graphql.server.operations.Mutation
import org.springframework.stereotype.Service

@Service
class CreateAuthorMutation(
    private val createAuthor: CreateAuthorUseCase,
) : Mutation {
    fun createAuthor(payload: CreateAuthorPayload): AuthorMutationResult {
        val result = createAuthor.execute(payload.mapToInput())

        return result.fold(
            ifLeft = { error -> CreateAuthorError(error.message) },
            ifRight = { authorEntity -> CreateAuthorOk(AuthorType(authorEntity)) },
        )
    }

    // Input
    data class CreateAuthorPayload(
        val name: String,
        val email: EmailScalarType,
    )

    fun CreateAuthorPayload.mapToInput(): CreateAuthorUseCase.Input = CreateAuthorUseCase.Input(name = this.name, email = this.email.value)

    // Output
    interface AuthorMutationResult

    data class CreateAuthorOk(
        val author: AuthorType,
    ) : AuthorMutationResult

    data class CreateAuthorError(
        val message: String,
    ) : AuthorMutationResult
}
