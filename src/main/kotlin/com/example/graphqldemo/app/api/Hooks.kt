package com.example.graphqldemo.app.api

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.schema.GraphQLType
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf

@Component
class Hooks : SchemaGeneratorHooks {
    override fun willGenerateGraphQLType(type: KType): GraphQLType? {
        val kClass = type.classifier as? KClass<*> ?: return null

        if (kClass.isSubclassOf(CustomScalarType::class)) {
            val graphqlType = CustomScalarType.getGraphQlType(kClass as KClass<out CustomScalarType<*>>)
            println(graphqlType)
            return graphqlType
        }

        return null
    }

//    override fun willResolveMonad(type: KType): KType {
//        val kClass = type.classifier as? KClass<*> ?: return type
//
//        if (kClass.isSubclassOf(CustomScalarType::class)) {
//            val customScalarInterface =
//                kClass.supertypes.find {
//                    it.classifier == CustomScalarType::class
//                }
//            val unwrappedType = customScalarInterface?.arguments?.firstOrNull()?.type
//            println("unwrap $type to $unwrappedType")
//
//            return unwrappedType ?: type
//        }
//
//        return type
//    }
}
