package com.example.graphqldemo.app.infra

import com.example.graphqldemo.app.domain.ReviewEntity
import org.springframework.stereotype.Service

/** Mockat externt api. */
@Service
class ReviewApi {
    private val reviews =
        listOf(
            ReviewEntity("review1", bookId = "book1", reviewer = "Kalle Anka", stars = 3, text = "Helt ok..."),
            ReviewEntity("review2", bookId = "book2", reviewer = "Arne Anka", stars = 5, text = "Mästerverk!"),
        )

    fun findByBooks(bookIds: Set<String>): Map<String, List<ReviewEntity>> {
        val byBook = reviews.groupBy { it.bookId }
        return bookIds.associateWith { byBook[it].orEmpty() }
    }
}
