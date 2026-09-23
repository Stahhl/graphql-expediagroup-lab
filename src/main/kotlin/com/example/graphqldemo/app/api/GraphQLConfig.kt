package com.example.graphqldemo.app.api

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.expediagroup.graphql.generator.execution.KotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.server.spring.execution.SpringDataFetcher
import com.expediagroup.graphql.server.spring.execution.SpringKotlinDataFetcherFactoryProvider
import graphql.GraphqlErrorBuilder
import graphql.analysis.MaxQueryComplexityInstrumentation
import graphql.analysis.MaxQueryDepthInstrumentation
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import graphql.schema.DataFetcherFactory
import graphql.schema.DataFetchingEnvironment
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaType

/**
 * Låter en resolver-parameter injiceras med enbart @GraphQLIgnore.
 *
 * Ramverkets standard kräver två annoteringar: schemageneratorn tittar bara efter
 * @GraphQLIgnore (kFunctionExtensions.getValidArguments) och SpringDataFetcher bara
 * efter @Autowired. Ingen av dem löser meta-annoteringar, så en egen sammansatt
 * annotering fungerar inte — men data fetcher-fabriken är utbytbar.
 *
 * Notera priset: @GraphQLIgnore på en parameter betyder nu "det här är en bean".
 * @Autowired fungerar fortfarande via super, inklusive @Qualifier.
 *
 * Relationer bör ändå gå via DataLoader (se DataLoaders.kt). Bean-injektion är för
 * tjänster som inte är nyckelbaserade uppslag.
 */
class BeanInjectingDataFetcher(
    target: Any?,
    fn: KFunction<*>,
    private val applicationContext: ApplicationContext,
) : SpringDataFetcher(target, fn, applicationContext) {
    override fun mapParameterToValue(
        param: KParameter,
        environment: DataFetchingEnvironment,
    ): Pair<KParameter, Any?>? =
        if (param.hasAnnotation<GraphQLIgnore>()) {
            param to applicationContext.getBean(param.type.javaType as Class<*>)
        } else {
            super.mapParameterToValue(param, environment)
        }
}

@Configuration
class GraphQLConfig {
    /** Ersätter ramverkets standardbean (den är @ConditionalOnMissingBean). */
    @Bean
    fun dataFetcherFactoryProvider(applicationContext: ApplicationContext): KotlinDataFetcherFactoryProvider =
        object : SpringKotlinDataFetcherFactoryProvider(applicationContext) {
            override fun functionDataFetcherFactory(
                target: Any?,
                kClass: KClass<*>,
                kFunction: KFunction<*>,
            ): DataFetcherFactory<Any?> = DataFetcherFactory { BeanInjectingDataFetcher(target, kFunction, applicationContext) }
        }

    /**
     * Schemat är cykliskt (Book <-> Author <-> Book), så utan tak kan en liten query
     * expandera till godtyckligt mycket arbete. Instrumentation-beans plockas upp
     * automatiskt och kedjas av GraphQLSchemaConfiguration.
     */
    @Bean
    fun maxQueryDepth(): MaxQueryDepthInstrumentation = MaxQueryDepthInstrumentation(MAX_DEPTH)

    @Bean
    fun maxQueryComplexity(): MaxQueryComplexityInstrumentation = MaxQueryComplexityInstrumentation(MAX_COMPLEXITY)

    /** Standardhanteraren läcker interna exception-meddelanden till klienten. */
    @Bean
    fun dataFetcherExceptionHandler(): DataFetcherExceptionHandler = SafeDataFetcherExceptionHandler()

    companion object {
        const val MAX_DEPTH = 10
        const val MAX_COMPLEXITY = 200
    }
}

class SafeDataFetcherExceptionHandler : DataFetcherExceptionHandler {
    private val logger = LoggerFactory.getLogger(SafeDataFetcherExceptionHandler::class.java)

    override fun handleException(parameters: DataFetcherExceptionHandlerParameters): CompletableFuture<DataFetcherExceptionHandlerResult> {
        logger.error("Unhandled exception while fetching {}", parameters.path, parameters.exception)

        val error =
            GraphqlErrorBuilder
                .newError()
                .message("Internal server error")
                .path(parameters.path)
                .location(parameters.sourceLocation)
                .extensions(mapOf("code" to "INTERNAL_ERROR"))
                .build()

        return CompletableFuture.completedFuture(DataFetcherExceptionHandlerResult.newResult(error).build())
    }
}
