package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.app.CreateAuthorUseCase
import com.example.graphqldemo.app.domain.Email
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import org.springframework.stereotype.Component

@Component
class CreateAuthorMutation(
    private val createAuthor: CreateAuthorUseCase,
) : Mutation {
    @GraphQLDescription("Register a new author")
    fun createAuthor(input: CreateAuthorInput): AuthorMutationResult =
        createAuthor.execute(input.toUseCaseInput()).fold(
            ifLeft = { CreateAuthorError(it.message) },
            ifRight = { CreateAuthorOk(AuthorType(it)) },
        )

    // Input. Email är redan validerad av scalaren när vi kommer hit.
    data class CreateAuthorInput(
        val name: String,
        val email: Email,
    )

    private fun CreateAuthorInput.toUseCaseInput(): CreateAuthorUseCase.Input = CreateAuthorUseCase.Input(name = name, email = email)

    // Output
    interface AuthorMutationResult

    data class CreateAuthorOk(
        val author: AuthorType,
    ) : AuthorMutationResult

    data class CreateAuthorError(
        val message: String,
    ) : AuthorMutationResult
}
