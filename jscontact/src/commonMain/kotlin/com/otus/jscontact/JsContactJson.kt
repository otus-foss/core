package com.otus.jscontact

import kotlinx.serialization.json.Json

/**
 * The JSON configuration for JSContact data (RFC 9553, section 1.3). Every JSContact class keeps
 * unknown properties in its own `extra` map (section 1.7.4), so [Json.ignoreUnknownKeys] never
 * drops data here. Null and default-valued optional properties are left out of the output.
 */
public val JsContactJson: Json =
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = false
    }
