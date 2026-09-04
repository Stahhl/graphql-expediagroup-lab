package com.example.graphqldemo.app.app

import arrow.core.Either

fun interface UseCase<Input, Error, Output> {
    fun execute(input: Input): Either<Error, Output>
}

fun <Input, Output> UseCase<Input, Error, Output>.execute(input: Input): Either<Error, Output> = execute(input)
