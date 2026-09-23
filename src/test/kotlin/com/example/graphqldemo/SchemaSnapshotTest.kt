package com.example.graphqldemo

import com.expediagroup.graphql.generator.extensions.print
import graphql.schema.GraphQLSchema
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

/**
 * Code-first betyder att schemat är en bieffekt av Kotlin-koden. Den här snapshoten
 * gör oavsiktliga schemaändringar synliga i CI i stället för hos konsumenten.
 *
 * Är ändringen avsiktlig: kör testet, kopiera det faktiska schemat till
 * src/test/resources/schema.graphql och granska diffen som en API-ändring.
 */
@SpringBootTest
class SchemaSnapshotTest {
    @Autowired
    private lateinit var schema: GraphQLSchema

    @Test
    fun `schemat matchar incheckad snapshot`() {
        val expected = checkNotNull(javaClass.getResource("/schema.graphql")).readText().trim()

        assertEquals(expected, schema.print(includeDirectives = false).trim())
    }
}
