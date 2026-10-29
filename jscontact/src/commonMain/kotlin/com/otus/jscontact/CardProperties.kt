package com.otus.jscontact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.elementNames

@OptIn(ExperimentalSerializationApi::class)
private val cardDescriptor = Card.generatedSerializer().descriptor

/** The JSON name of every property a [Card] knows, for a request that asks for properties by name. */
@OptIn(ExperimentalSerializationApi::class)
public val cardPropertyNames: List<String> = cardDescriptor.elementNames.toList()
