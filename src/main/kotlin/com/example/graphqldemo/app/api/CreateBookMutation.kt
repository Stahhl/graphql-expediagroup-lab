package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.app.CreateBookUseCase
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import org.springframework.stereotype.Component

@Component
class CreateBookMutation(
    private val createBook: CreateBookUseCase,
) : Mutation {
    @GraphQLDescription("Add a book to the catalogue")
    fun createBook(input: CreateBookInput): BookMutationResult =
        createBook.execute(input.toUseCaseInput()).fold(
            ifLeft = { CreateBookError(it.message) },
            ifRight = { CreateBookOk(BookType(it)) },
        )

    // Input. Namnet slutar redan på "Input", så ramverket lägger inte till suffixet igen.
    data class CreateBookInput(
        val title: String,
        val authorId: String,
    )

    private fun CreateBookInput.toUseCaseInput(): CreateBookUseCase.Input = CreateBookUseCase.Input(title = title, authorId = authorId)

    // Output. Ett interface utan fält blir en union: CreateBookError | CreateBookOk
    interface BookMutationResult

    data class CreateBookOk(
        val book: BookType,
    ) : BookMutationResult

    data class CreateBookError(
        val message: String,
    ) : BookMutationResult
}
