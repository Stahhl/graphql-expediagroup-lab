package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.app.CreateBookUseCase
import com.expediagroup.graphql.server.operations.Mutation
import org.springframework.stereotype.Component

@Component
class CreateBookMutation(
    private val createBook: CreateBookUseCase,
) : Mutation {
    fun createBook(payload: BookPayload): BookMutationResult {
        println("createBook *******************************")
        val result = createBook.execute(payload.mapToInput())

        return result.fold(
            ifLeft = { error -> CreateBookError(error.message) },
            ifRight = { bookEntity -> CreateBookOk(BookType(bookEntity)) },
        )
    }

    // Input
    data class BookPayload(
        val title: String,
        val authorId: String,
    )

    fun BookPayload.mapToInput(): CreateBookUseCase.Input = CreateBookUseCase.Input(title, authorId)

    // Output
    interface BookMutationResult

    data class CreateBookOk(
        val bookResponse: BookType,
    ) : BookMutationResult

    data class CreateBookError(
        val message: String,
    ) : BookMutationResult
}
