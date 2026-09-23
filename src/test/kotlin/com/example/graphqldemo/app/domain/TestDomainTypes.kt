package com.example.graphqldemo.app.domain

/**
 * Ligger medvetet i domänpaketet — skyddsnätet i Hooks avgränsar på just paketnamnet,
 * och det går inte att testa trovärdigt utanför det.
 *
 * Ingen av dem är @Component eller refererad från schemat, så de påverkar inte
 * SchemaSnapshotTest.
 */
@JvmInline
value class UnregisteredValue(
    val value: String,
)

@JvmInline
value class SecondUnregisteredValue(
    val value: String,
)

data class NotAValueClass(
    val value: String,
)
