package com.example.graphqldemo.app.api.scalars

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import org.springframework.core.ResolvableType
import java.util.Locale
import kotlin.reflect.KClass

/**
 * Kastas av [StringApiScalar.decode] när indata inte kan bli en giltig domäntyp.
 * Basen översätter den till rätt Coercing-exception beroende på om värdet kom som
 * variabel eller som literal i queryn.
 */
class ApiScalarException(
    message: String,
) : RuntimeException(message)

/**
 * Kontraktet för en egen scalar. Implementationer registreras automatiskt genom att
 * vara Spring-beans — [com.example.graphqldemo.app.api.Hooks] injicerar alla och bygger
 * registret. Inget att komma ihåg utöver @Component.
 *
 * Domäntypen läses ur typparametern, samma mekanism Spring använder för Converter<S, T>.
 */
abstract class ApiScalar<Domain : Any, Serialized : Any>(
    val name: String,
    val description: String? = null,
) : Coercing<Domain, Serialized> {
    val domainType: KClass<*> by lazy {
        ResolvableType
            .forClass(ApiScalar::class.java, javaClass)
            .getGeneric(0)
            .resolve()
            ?.kotlin
            ?: error(
                "Could not resolve the domain type of ${javaClass.name}. " +
                    "An ApiScalar must declare a concrete domain type, e.g. StringApiScalar<Email>.",
            )
    }

    val graphqlType: GraphQLScalarType by lazy {
        GraphQLScalarType
            .newScalar()
            .name(name)
            .description(description)
            .coercing(this)
            .build()
    }
}

/**
 * Bas för scalars som serialiseras som sträng — de allra flesta.
 *
 * Du skriver [encode] och [decode]; basen sköter alla tre Coercing-metoderna,
 * uppackningen av StringValue ur AST:n och rätt exception-typ i varje position.
 *
 * Behöver du en annan serialiserad representation (Int, Long ...) ärver du
 * [ApiScalar] direkt och implementerar Coercing själv.
 *
 * Metoderna heter encode/decode och inte serialize/parse eftersom Coercing redan
 * deklarerar `serialize(Object)` — en `serialize(Domain)` skulle krocka på
 * JVM-signatur efter erasure.
 */
abstract class StringApiScalar<Domain : Any>(
    name: String,
    description: String? = null,
) : ApiScalar<Domain, String>(name, description) {
    protected abstract fun encode(value: Domain): String

    protected abstract fun decode(raw: String): Domain

    /** Bekvämlighet för [decode]: `?: invalid("...")`. */
    protected fun invalid(message: String): Nothing = throw ApiScalarException(message)

    @Suppress("UNCHECKED_CAST")
    final override fun serialize(
        dataFetcherResult: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): String {
        if (!domainType.isInstance(dataFetcherResult)) {
            throw CoercingSerializeException(
                "Scalar $name expected ${domainType.simpleName} but got ${dataFetcherResult::class.simpleName}",
            )
        }
        return encode(dataFetcherResult as Domain)
    }

    @Suppress("UNCHECKED_CAST")
    final override fun parseValue(
        input: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): Domain {
        if (domainType.isInstance(input)) return input as Domain
        return try {
            decode(input.toString())
        } catch (e: ApiScalarException) {
            throw CoercingParseValueException(e.message, e)
        }
    }

    final override fun parseLiteral(
        input: Value<*>,
        variables: CoercedVariables,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): Domain {
        val raw =
            (input as? StringValue)?.value
                ?: throw CoercingParseLiteralException(
                    "Scalar $name expected a string literal but got ${input::class.simpleName}",
                )
        return try {
            decode(raw)
        } catch (e: ApiScalarException) {
            throw CoercingParseLiteralException(e.message, e)
        }
    }
}
