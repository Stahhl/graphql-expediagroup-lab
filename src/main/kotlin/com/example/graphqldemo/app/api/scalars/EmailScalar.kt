package com.example.graphqldemo.app.api.scalars

import com.example.graphqldemo.app.domain.Email
import org.springframework.stereotype.Component

/**
 * Hela definitionen av scalar Email. @Component räcker — ingen registrering någon
 * annanstans.
 *
 * All validering ligger i [Email]; den här klassen känner bara till felmeddelandet.
 * [decode] returnerar domäntypen, vilket gör att graphql-kotlin
 * (convertArgumentValue.mapToInlineValueClass) ser ett färdigt värde och aldrig anropar
 * Emails privata konstruktor reflektivt — valideringen går alltså inte att kringgå.
 */
@Component
class EmailScalar : StringApiScalar<Email>(
    name = "Email",
    description = "A valid email address, e.g. \"jane@example.com\"",
) {
    override fun encode(value: Email): String = value.value

    override fun decode(raw: String): Email = Email.of(raw).getOrNull() ?: invalid("Invalid email format: $raw")
}
