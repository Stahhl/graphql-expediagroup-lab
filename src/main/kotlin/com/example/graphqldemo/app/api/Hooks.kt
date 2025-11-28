package com.example.graphqldemo.app.api

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.schema.GraphQLType
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf

@Component
class Hooks : SchemaGeneratorHooks {
    override fun willGenerateGraphQLType(type: KType): GraphQLType? {
        val kClass = type.classifier as? KClass<*> ?: return null

        if (kClass.isSubclassOf(CustomScalarType::class)) {
            // Retrieve companion object instance via Kotlin reflection
            val companion =
                kClass.companionObjectInstance
                    ?: error("E1")

            // Must implement CustomScalarCompanion
            return (companion as? CustomScalarCompanion)?.graphqlType ?: error("E2")
        }

        return null
    }
}
