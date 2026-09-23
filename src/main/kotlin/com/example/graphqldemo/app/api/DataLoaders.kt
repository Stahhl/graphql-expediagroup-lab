package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.domain.AuthorEntity
import com.example.graphqldemo.app.domain.BookEntity
import com.example.graphqldemo.app.domain.PetEntity
import com.example.graphqldemo.app.domain.PublisherEntity
import com.example.graphqldemo.app.domain.ReviewEntity
import com.example.graphqldemo.app.infra.Database
import com.example.graphqldemo.app.infra.ReviewApi
import com.expediagroup.graphql.dataloader.KotlinDataLoader
import graphql.GraphQLContext
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.MappedBatchLoader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

/**
 * En DataLoader per relation — botemedlet mot N+1.
 *
 * Alla KotlinDataLoader-beans registreras automatiskt av GraphQLExecutionConfiguration,
 * och KotlinDataLoaderRegistryFactory bygger ett nytt register per request, så cachen
 * läcker aldrig mellan anrop.
 *
 * Sätt logging.level.com.example.graphqldemo.app.api=DEBUG för att se batcharna.
 */
private val logger = LoggerFactory.getLogger("com.example.graphqldemo.app.api.DataLoaders")

/**
 * Mappad batch: nycklar som saknas i resultatmappen fullbordas som null av DataLoader.
 * java-dataloader är JSpecify-annoterat och tillåter inte nullable V, därför casten —
 * den beskriver runtime-beteendet korrekt och gör att anropsplatsen får rätt typ.
 */
@Suppress("UNCHECKED_CAST")
private fun <K : Any, V : Any> optionalLoader(
    name: String,
    load: (Set<K>) -> Map<K, V>,
): DataLoader<K, V?> = totalLoader(name, load) as DataLoader<K, V?>

/** Batch där varje efterfrågad nyckel alltid får ett svar. */
private fun <K : Any, V : Any> totalLoader(
    name: String,
    load: (Set<K>) -> Map<K, V>,
): DataLoader<K, V> =
    DataLoaderFactory.newMappedDataLoader(
        MappedBatchLoader { keys: Set<K> ->
            CompletableFuture.supplyAsync {
                logger.debug("batch {} keys={}", name, keys)
                load(keys)
            }
        },
    )

@Component
class AuthorDataLoader(
    private val database: Database,
) : KotlinDataLoader<String, AuthorEntity?> {
    override val dataLoaderName = NAME

    override fun getDataLoader(graphQLContext: GraphQLContext): DataLoader<String, AuthorEntity?> =
        optionalLoader(NAME, database::findAuthors)

    companion object {
        const val NAME = "AuthorDataLoader"
    }
}

@Component
class PetDataLoader(
    private val database: Database,
) : KotlinDataLoader<String, PetEntity?> {
    override val dataLoaderName = NAME

    override fun getDataLoader(graphQLContext: GraphQLContext): DataLoader<String, PetEntity?> = optionalLoader(NAME, database::findPets)

    companion object {
        const val NAME = "PetDataLoader"
    }
}

@Component
class PublisherDataLoader(
    private val database: Database,
) : KotlinDataLoader<String, PublisherEntity?> {
    override val dataLoaderName = NAME

    override fun getDataLoader(graphQLContext: GraphQLContext): DataLoader<String, PublisherEntity?> =
        optionalLoader(NAME, database::findPublishers)

    companion object {
        const val NAME = "PublisherDataLoader"
    }
}

@Component
class BooksByAuthorDataLoader(
    private val database: Database,
) : KotlinDataLoader<String, List<BookEntity>> {
    override val dataLoaderName = NAME

    override fun getDataLoader(graphQLContext: GraphQLContext): DataLoader<String, List<BookEntity>> =
        totalLoader(NAME, database::findBooksByAuthor)

    companion object {
        const val NAME = "BooksByAuthorDataLoader"
    }
}

@Component
class BooksByPublisherDataLoader(
    private val database: Database,
) : KotlinDataLoader<String, List<BookEntity>> {
    override val dataLoaderName = NAME

    override fun getDataLoader(graphQLContext: GraphQLContext): DataLoader<String, List<BookEntity>> =
        totalLoader(NAME, database::findBooksByPublisher)

    companion object {
        const val NAME = "BooksByPublisherDataLoader"
    }
}

@Component
class ReviewsByBookDataLoader(
    private val reviewApi: ReviewApi,
) : KotlinDataLoader<String, List<ReviewEntity>> {
    override val dataLoaderName = NAME

    override fun getDataLoader(graphQLContext: GraphQLContext): DataLoader<String, List<ReviewEntity>> =
        totalLoader(NAME, reviewApi::findByBooks)

    companion object {
        const val NAME = "ReviewsByBookDataLoader"
    }
}
