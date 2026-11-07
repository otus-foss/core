package com.otus.avatar.ui.presentation

/** An account whose [addresses] the user gives avatars, by its [id] and the [name] it shows. */
public data class OwnAccount(
    val id: String,
    val name: String,
    val addresses: List<String>,
)
