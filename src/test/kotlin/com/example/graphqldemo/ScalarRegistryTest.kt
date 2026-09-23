package com.example.graphqldemo

import com.example.graphqldemo.app.api.Hooks
import com.example.graphqldemo.app.api.scalars.EmailScalar
import com.example.graphqldemo.app.api.scalars.StringApiScalar
import com.example.graphqldemo.app.domain.Email
import com.example.graphqldemo.app.domain.NotAValueClass
import com.example.graphqldemo.app.domain.SecondUnregisteredValue
import com.example.graphqldemo.app.domain.UnregisteredValue
import com.expediagroup.graphql.generator.scalars.ID
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScalarRegistryTest {
    private val emailScalar = EmailScalar()
    private val hooks = Hooks(listOf(emailScalar))

    @Test
    fun `domantypen lases ur typparametern`() {
        assertEquals(Email::class, emailScalar.domainType)
    }

    @Test
    fun `registrerad domantyp mappas till sin scalar`() {
        assertEquals(emailScalar.graphqlType, hooks.willGenerateGraphQLType(typeOf<Email>()))
    }

    @Test
    fun `exponerad domain value class utan ApiScalar stoppar schemat`() {
        val error =
            assertFailsWith<IllegalStateException> {
                hooks.willGenerateGraphQLType(typeOf<UnregisteredValue>())
            }
        assertTrue("UnregisteredValue" in error.message.orEmpty(), error.message.orEmpty())
    }

    @Test
    fun `domanklass som inte ar value class passerar`() {
        assertNull(hooks.willGenerateGraphQLType(typeOf<NotAValueClass>()))
    }

    @Test
    fun `value classes utanfor domanpaketet passerar`() {
        // ID och UInt är också @JvmInline value class — de får inte träffas av skyddsnätet.
        assertNull(hooks.willGenerateGraphQLType(typeOf<ID>()))
        assertNull(hooks.willGenerateGraphQLType(typeOf<UInt>()))
        assertNull(hooks.willGenerateGraphQLType(typeOf<String>()))
    }

    @Test
    fun `tva ApiScalar for samma domantyp gar inte att registrera`() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                Hooks(listOf(EmailScalar(), LegacyEmailScalar()))
            }
        assertTrue("EmailScalar" in error.message.orEmpty(), error.message.orEmpty())
        assertTrue("LegacyEmailScalar" in error.message.orEmpty(), error.message.orEmpty())
    }

    @Test
    fun `tva ApiScalar med samma schemanamn gar inte att registrera`() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                Hooks(listOf(EmailScalar(), ImpostorEmailScalar()))
            }
        assertTrue("Email" in error.message.orEmpty(), error.message.orEmpty())
        assertTrue("ImpostorEmailScalar" in error.message.orEmpty(), error.message.orEmpty())
    }
}

/** Samma domäntyp som EmailScalar, annat namn. */
private class LegacyEmailScalar : StringApiScalar<Email>(name = "LegacyEmail") {
    override fun encode(value: Email): String = value.value

    override fun decode(raw: String): Email = Email.of(raw).getOrNull() ?: invalid("nope")
}

/** Samma schemanamn som EmailScalar, annan domäntyp. */
private class ImpostorEmailScalar : StringApiScalar<SecondUnregisteredValue>(name = "Email") {
    override fun encode(value: SecondUnregisteredValue): String = value.value

    override fun decode(raw: String): SecondUnregisteredValue = SecondUnregisteredValue(raw)
}
