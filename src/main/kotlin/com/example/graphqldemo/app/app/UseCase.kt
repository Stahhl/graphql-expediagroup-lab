package com.example.graphqldemo.app.app

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.example.graphqldemo.app.BookEntity
import com.example.graphqldemo.app.Repository
import com.example.graphqldemo.app.api.BookType
import org.springframework.stereotype.Service
import java.util.UUID

fun interface UseCase<Input, Error, Output> {
    fun execute(input: Input): Either<Error, Output>
}

fun <Input, Output> UseCase<Input, Error, Output>.execute(input: Input): Either<Error, Output> = execute(input)
