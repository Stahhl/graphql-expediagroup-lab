package com.example.graphqldemo

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
class GraphQLApiTest {
    @Autowired
    private lateinit var client: WebTestClient

    private fun query(document: String) =
        client
            .post()
            .uri("/graphql")
            .bodyValue(mapOf("query" to document))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()

    @Test
    fun `en bok utan forlag ger null i stallet for att spranga queryn`() {
        query(
            """
            mutation {
              createBook(input: { title: "Ny bok", authorId: "author1" }) {
                ... on CreateBookOk { book { title publisher { name } } }
                ... on CreateBookError { message }
              }
            }
            """.trimIndent(),
        ).jsonPath("$.errors")
            .doesNotExist()
            .jsonPath("$.data.createBook.book.publisher")
            .isEmpty()
    }

    @Test
    fun `en nyskapad bok dyker upp under forfattarens bocker`() {
        query("""mutation { createBook(input: { title: "Bok B", authorId: "author2" }) { __typename } }""")
            .jsonPath("$.data.createBook.__typename")
            .isEqualTo("CreateBookOk")

        query("""{ books { title author { books { title } } } }""")
            .jsonPath("$.data.books[?(@.title == 'Bok B')].author.books[?(@.title == 'Bok B')]")
            .exists()
    }

    @Test
    fun `okand forfattare ger ett domanfel i unionen i stallet for ett exception`() {
        query(
            """mutation { createBook(input: { title: "X", authorId: "nope" }) {
                 __typename ... on CreateBookError { message } } }""",
        ).jsonPath("$.errors")
            .doesNotExist()
            .jsonPath("$.data.createBook.__typename")
            .isEqualTo("CreateBookError")
            .jsonPath("$.data.createBook.message")
            .isEqualTo("Author nope not found")
    }

    @Test
    fun `ogiltig email avvisas av scalaren`() {
        query("""mutation { createAuthor(input: { name: "X", email: "inte-en-email" }) { __typename } }""")
            .jsonPath("$.errors[0].message")
            .value<String> { assert(it.contains("Invalid email format")) { it } }
    }

    @Test
    fun `for djup query avvisas`() {
        val deep = generateSequence("name") { "books { author { $it } }" }.take(12).last()

        query("{ books { author { $deep } } }")
            .jsonPath("$.errors[0].message")
            .value<String> { assert(it.contains("depth", ignoreCase = true)) { it } }
    }
}
