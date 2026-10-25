package com.otus.avatar

/** A contact source whose cards hold only photos, as [load] returns them. */
internal fun photoSource(load: suspend (AvatarRequest, Boolean) -> AvatarImage?): ContactAvatarSource =
    ContactAvatarSource { request, external -> load(request, external)?.let { ContactAvatar.Photo(it, "image/png") } }

/** The photo [this] finds for [request], or null when it finds none or only a style. */
internal suspend fun ContactAvatarSource.photoOf(
    request: AvatarRequest,
    external: Boolean,
): AvatarImage? = (avatar(request, external) as? ContactAvatar.Photo)?.image
