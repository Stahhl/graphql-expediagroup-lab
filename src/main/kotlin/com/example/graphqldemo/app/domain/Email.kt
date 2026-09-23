package com.example.graphqldemo.app.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right

/**
 * E-postadress som domäntyp. Konstruktorn är privat — enda vägen in är [of],
 * så en Email-instans är per definition validerad.
 */
@JvmInline
value class Email private constructor(
    val value: String,
) {
    data class Invalid(
        val raw: String,
    )

    override fun toString(): String = value

    companion object {
        private val FORMAT = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

        fun of(raw: String): Either<Invalid, Email> = if (FORMAT.matches(raw)) Email(raw).right() else Invalid(raw).left()

        /**
         * Endast för redan validerade källor (seed-data, migrerad databasdata).
         * Kastar hellre direkt än att låta ogiltig data spridas vidare.
         */
        fun trusted(raw: String): Email = of(raw).getOrNull() ?: error("Not a valid email: $raw")
    }
}
