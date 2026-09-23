package com.example.graphqldemo.app.api

import com.example.graphqldemo.app.api.scalars.ApiScalar
import com.example.graphqldemo.app.domain.BookEntity
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Kopplar domäntyper till egna scalars.
 *
 * Ramverket tillåter bara en SchemaGeneratorHooks-bean, så aggregeringen måste ske här.
 * Men registret byggs från alla [ApiScalar]-beans — den här filen behöver inte röras när
 * en scalar läggs till.
 */
@Component
class Hooks(
    scalars: List<ApiScalar<*, *>> = emptyList(),
) : SchemaGeneratorHooks {
    private val byDomainType: Map<KClass<*>, GraphQLScalarType> = index(scalars)

    override fun willGenerateGraphQLType(type: KType): GraphQLType? {
        val kClass = type.classifier as? KClass<*> ?: return null
        byDomainType[kClass]?.let { return it }
        requireNotUnregisteredDomainScalar(kClass)
        return null
    }

    /**
     * Den här hooken anropas bara för typer som faktiskt är på väg in i schemat, så en
     * domäntyp som aldrig exponeras kräver ingenting i api-lagret.
     *
     * Exponeras den däremot utan ApiScalar skulle ramverket tyst generera ett objekt
     * (`type Email { value: String! }`) — generateGraphQLType faller vidare till
     * objectFromReflection när hooken returnerar null. Vi stoppar det vid uppstart i stället.
     */
    private fun requireNotUnregisteredDomainScalar(kClass: KClass<*>) {
        if (!kClass.isValue) return
        if (kClass.java.packageName != DOMAIN_PACKAGE) return
        if (kClass in ALLOW_OBJECT_TYPE) return

        error(
            "${kClass.qualifiedName} is exposed in the GraphQL schema but has no ApiScalar. " +
                "Without one it silently becomes an object type. Add an ApiScalar in app/api/scalars, " +
                "or list the class in Hooks.ALLOW_OBJECT_TYPE if the object type is intended.",
        )
    }

    companion object {
        /**
         * Härlett ur en domänklass i stället för en strängliteral, så att en paketflytt
         * inte tyst slår av kontrollen ovan.
         */
        private val DOMAIN_PACKAGE: String = BookEntity::class.java.packageName

        /** Domän-value classes som medvetet ska bli objekttyper i schemat. */
        private val ALLOW_OBJECT_TYPE: Set<KClass<*>> = emptySet()

        /**
         * Avgränsningen till domänpaketet är nödvändig: ramverkets egen
         * com.expediagroup.graphql.generator.scalars.ID och Kotlins UInt är också
         * @JvmInline value class och skulle annars träffas av kontrollen.
         */
        private fun index(scalars: List<ApiScalar<*, *>>): Map<KClass<*>, GraphQLScalarType> {
            // graphql-java upptäcker visserligen två scalars med samma namn när schemat
            // byggs, men meddelandet lyder "redefined the type 'Email' from being a
            // 'GraphQLScalarType' to a 'GraphQLScalarType'" och nämner inga Kotlin-klasser.
            scalars.groupBy { it.name }.forEach { (name, claimants) ->
                require(claimants.size == 1) {
                    "Duplicate scalar name \"$name\" declared by " +
                        claimants.joinToString(" and ") { it.javaClass.simpleName }
                }
            }

            // Det här fallet upptäcks inte av någon annan: utan kontrollen vinner den sista
            // tyst och schemat får fel namn på typen.
            scalars.groupBy { it.domainType }.forEach { (domainType, claimants) ->
                require(claimants.size == 1) {
                    "Duplicate ApiScalar for domain type ${domainType.qualifiedName}: " +
                        claimants.joinToString(" and ") { it.javaClass.simpleName }
                }
            }

            return scalars.associate { it.domainType to it.graphqlType }
        }
    }
}
