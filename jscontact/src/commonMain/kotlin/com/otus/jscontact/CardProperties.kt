package com.otus.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.elementNames

/** The JSON name of every property a [Card] knows, for a request that asks for properties by name. */
@OptIn(ExperimentalSerializationApi::class)
public val cardPropertyNames: List<String> =
    Card
        .generatedSerializer()
        .descriptor.elementNames
        .toList()
