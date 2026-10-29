package com.otus.jscontact.validation

import com.otus.jscontact.Name
import com.otus.jscontact.NameComponent
import com.otus.jscontact.Nickname
import com.otus.jscontact.OrgUnit
import com.otus.jscontact.Organization
import com.otus.jscontact.Pronouns
import com.otus.jscontact.Relation
import com.otus.jscontact.SpeakToAs
import com.otus.jscontact.Title

/** Validates a Name: [Name.components] or [Name.full], the component rules, and sortAs (RFC 9553, section 2.2.1.1). */
internal fun ValidationScope.checkName(name: Name) {
    propertyNames(name.extra, Name.serializer())
    ensure(name.components != null || name.full != null, "A Name needs components or full ($NAME_SECTION)")
    components(name.componentRules())
    checkSortAs(name)
}

private fun Name.componentRules(): ComponentRules =
    ComponentRules(
        parts = components?.map { ComponentRules.Part(it.kind.value, it.phonetic, it.extra) },
        isOrdered = isOrdered,
        defaultSeparator = defaultSeparator,
        phoneticScript = phoneticScript,
        phoneticSystem = phoneticSystem,
        kinds = RegisteredValues.nameComponentKinds,
        partSerializer = NameComponent.serializer(),
        section = NAME_SECTION,
    )

// Each sortAs key is a component kind that at least one component has (RFC 9553, section 2.2.1.1).
private fun ValidationScope.checkSortAs(name: Name) {
    val keys = name.sortAs?.keys?.map { it.value }
    val kinds = name.components?.map { it.kind.value }?.toSet()
    enumKeys(SORT_AS, keys, RegisteredValues.nameComponentKinds)
    member(SORT_AS, keys) { values ->
        if (kinds == null) {
            report("sortAs needs components ($NAME_SECTION)")
        } else {
            values
                .filterNot { it in kinds }
                .forEach { kind -> report("No component has the kind \"$kind\" ($NAME_SECTION)", kind) }
        }
    }
}

/** Validates a Relation of `relatedTo`: a set of relation types (RFC 9553, section 2.1.8). */
internal fun ValidationScope.checkRelation(relation: Relation) {
    propertyNames(relation.extra, Relation.serializer())
    enumSet(
        "relation",
        relation.relation?.mapKeys { it.key.value },
        RegisteredValues.relationTypes,
        "RFC 9553, section 2.1.8",
    )
}

/** Validates a Nickname (RFC 9553, section 2.2.2). */
internal fun ValidationScope.checkNickname(nickname: Nickname) {
    propertyNames(nickname.extra, Nickname.serializer())
    contexts(nickname.contexts)
    pref(nickname.pref)
}

/** Validates an Organization: name or units, and units never empty (RFC 9553, section 2.2.3). */
internal fun ValidationScope.checkOrganization(organization: Organization) {
    propertyNames(organization.extra, Organization.serializer())
    ensure(
        organization.name != null || organization.units != null,
        "An Organization needs a name or units ($ORGANIZATION_SECTION)",
    )
    ensure(organization.units?.isEmpty() != true, "units must not be empty ($ORGANIZATION_SECTION)", "units")
    items("units", organization.units) { propertyNames(it.extra, OrgUnit.serializer()) }
    contexts(organization.contexts)
}

/** Validates a SpeakToAs, which needs a grammatical gender or pronouns (RFC 9553, section 2.2.4). */
internal fun ValidationScope.checkSpeakToAs(speakToAs: SpeakToAs) {
    propertyNames(speakToAs.extra, SpeakToAs.serializer())
    ensure(
        speakToAs.grammaticalGender != null || speakToAs.pronouns != null,
        "A SpeakToAs needs a grammaticalGender or pronouns (RFC 9553, section 2.2.4)",
    )
    enumValue(speakToAs.grammaticalGender?.value, RegisteredValues.grammaticalGenders, "grammaticalGender")
    idMap("pronouns", speakToAs.pronouns) { pronouns ->
        propertyNames(pronouns.extra, Pronouns.serializer())
        contexts(pronouns.contexts)
        pref(pronouns.pref)
    }
}

/** Validates a Title (RFC 9553, section 2.2.5). */
internal fun ValidationScope.checkTitle(title: Title) {
    propertyNames(title.extra, Title.serializer())
    enumValue(title.kind?.value, RegisteredValues.titleKinds, "kind")
}

private const val NAME_SECTION = "RFC 9553, section 2.2.1.1"

private const val ORGANIZATION_SECTION = "RFC 9553, section 2.2.3"

private const val SORT_AS = "sortAs"
