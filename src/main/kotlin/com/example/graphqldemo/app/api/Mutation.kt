package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.app.CreateBook
import com.expediagroup.graphql.server.operations.Mutation
import org.springframework.stereotype.Component

@Component
class BookMutation(
    private val createBook: CreateBook,
) : Mutation {
    fun createBook(payload: BookPayload): BookMutationResult {
        println("createBook *******************************")
        val result = createBook.execute(payload.mapToInput())

        return result.fold(
            ifLeft = { error -> CreateBookError(error.message) },
            ifRight = { bookResponse -> CreateBookOk(bookResponse) },
        )
    }

    interface BookMutationResult

    class CreateBookOk(
        val bookresponse: BookResponse,
    ) : BookMutationResult

    class CreateBookError(
        val message: String,
    ) : BookMutationResult
}
