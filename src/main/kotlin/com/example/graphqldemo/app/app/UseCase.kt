package com.example.graphqldemo.app.app

import arrow.core.Either

fun interface UseCase<Input, Error, Output> {
    fun execute(input: Input): Either<Error, Output>
}
