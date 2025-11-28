package com.example.graphqldemo.app.api

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.GraphQLScalarType
import java.util.*

@JvmInline
value class EmailScalarType(
    override val value: String,
) : CustomScalarType<String> {
    override val companion: CustomScalarCompanion
        get() = Companion

    companion object : CustomScalarCompanion {
        override val graphqlType: GraphQLScalarType = graphqlEmailType
    }
}

interface CustomScalarType<T> {
    val value: T

    val companion: CustomScalarCompanion
}

interface CustomScalarCompanion {
    val graphqlType: GraphQLScalarType
}

val graphqlEmailType: GraphQLScalarType =
    GraphQLScalarType
        .newScalar()
        .name("Email")
        .description("A valid email address")
        .coercing(EmailCoercing)
        .build()

object EmailCoercing : Coercing<String, String> {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    override fun parseValue(
        input: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): String {
        val emailString = input.toString()

        if (!emailRegex.matches(emailString)) {
            throw CoercingParseValueException("Invalid email format: $emailString")
        }

        return emailString
    }

    override fun parseLiteral(
        input: Value<*>,
        variables: CoercedVariables,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): String {
        val emailString =
            (input as? StringValue)?.value
                ?: throw CoercingParseLiteralException("Expected a string literal for Email")
        if (!emailRegex.matches(emailString)) {
            throw CoercingParseLiteralException("Invalid email format: $emailString")
        }
        return emailString
    }

    override fun serialize(
        dataFetcherResult: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): String {
        val result = (dataFetcherResult as EmailScalarType).value
        println("serialize $result")
        return result
    }
}
