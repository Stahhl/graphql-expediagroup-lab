package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.GraphQlService
import com.expediagroup.graphql.generator.extensions.plus
import com.expediagroup.graphql.server.spring.execution.DefaultSpringGraphQLContextFactory
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class MyGraphQLContextFactory(
    private val graphQLServices: List<GraphQlService>,
) : DefaultSpringGraphQLContextFactory() {
    override suspend fun generateContext(request: ServerRequest): GraphQLContext =
        super.generateContext(request) +
            graphQLServices.associateBy { service -> service::class.simpleName!! }
}

inline fun <reified T : GraphQlService> DataFetchingEnvironment.getService(): T {
    val key = T::class.simpleName ?: error("Anonymous class?")
    return graphQlContext.get<T>(key)
        ?: error("GraphQLContext: service '$key' is not registered")
}
