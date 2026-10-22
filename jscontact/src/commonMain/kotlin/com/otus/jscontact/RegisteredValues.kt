package com.otus.jscontact

/**
 * The registered values of each enumerated property (RFC 9553, section 3.7), as strings. Validation
 * compares against them, because a value that differs from a registered one only in case makes the
 * object invalid (section 1.7.1).
 */
internal object RegisteredValues {
    /** Table 6 for Address, table 7 for every other type (RFC 9553, section 3.7.3). */
    val contexts: Set<String> =
        setOf(
            ContactContext.Private.value,
            ContactContext.Work.value,
            ContactContext.Billing.value,
            ContactContext.Delivery.value,
        )

    /** Table 13, Card `kind`. */
    val cardKinds: Set<String> =
        setOf(
            CardKind.Individual.value,
            CardKind.Group.value,
            CardKind.Org.value,
            CardKind.Location.value,
            CardKind.Device.value,
            CardKind.Application.value,
        )

    /** Table 22, Relation `relation`. */
    val relationTypes: Set<String> =
        setOf(
            RelationType.Acquaintance.value,
            RelationType.Agent.value,
            RelationType.Child.value,
            RelationType.CoResident.value,
            RelationType.CoWorker.value,
            RelationType.Colleague.value,
            RelationType.Contact.value,
            RelationType.Crush.value,
            RelationType.Date.value,
            RelationType.Emergency.value,
            RelationType.Friend.value,
            RelationType.Kin.value,
            RelationType.Me.value,
            RelationType.Met.value,
            RelationType.Muse.value,
            RelationType.Neighbor.value,
            RelationType.Parent.value,
            RelationType.Sibling.value,
            RelationType.Spouse.value,
            RelationType.Sweetheart.value,
        )

    /** Table 17, NameComponent `kind`, which is also the key set of Name `sortAs`. */
    val nameComponentKinds: Set<String> =
        setOf(
            NameComponentKind.Title.value,
            NameComponentKind.Given.value,
            NameComponentKind.Given2.value,
            NameComponentKind.Surname.value,
            NameComponentKind.Surname2.value,
            NameComponentKind.Credential.value,
            NameComponentKind.Generation.value,
            NameComponentKind.Separator.value,
        )

    /** Table 21, Name and Address `phoneticSystem`. */
    val phoneticSystems: Set<String> =
        setOf(PhoneticSystem.Ipa.value, PhoneticSystem.Jyut.value, PhoneticSystem.Piny.value)

    /** Table 9, SpeakToAs `grammaticalGender`. */
    val grammaticalGenders: Set<String> =
        setOf(
            GrammaticalGender.Animate.value,
            GrammaticalGender.Common.value,
            GrammaticalGender.Feminine.value,
            GrammaticalGender.Inanimate.value,
            GrammaticalGender.Masculine.value,
            GrammaticalGender.Neuter.value,
        )

    /** Table 19, Title `kind`. */
    val titleKinds: Set<String> = setOf(TitleKind.Title.value, TitleKind.Role.value)

    /** Table 8, Phone `features`. */
    val phoneFeatures: Set<String> =
        setOf(
            PhoneFeature.Mobile.value,
            PhoneFeature.Voice.value,
            PhoneFeature.Text.value,
            PhoneFeature.Video.value,
            PhoneFeature.MainNumber.value,
            PhoneFeature.Textphone.value,
            PhoneFeature.Fax.value,
            PhoneFeature.Pager.value,
        )

    /** Table 12, Calendar `kind`. */
    val calendarKinds: Set<String> = setOf(CalendarKind.Calendar.value, CalendarKind.FreeBusy.value)

    /** Table 10, AddressComponent `kind`. */
    val addressComponentKinds: Set<String> =
        setOf(
            AddressComponentKind.Room.value,
            AddressComponentKind.Apartment.value,
            AddressComponentKind.Floor.value,
            AddressComponentKind.Building.value,
            AddressComponentKind.Number.value,
            AddressComponentKind.Name.value,
            AddressComponentKind.Block.value,
            AddressComponentKind.Subdistrict.value,
            AddressComponentKind.District.value,
            AddressComponentKind.Locality.value,
            AddressComponentKind.Region.value,
            AddressComponentKind.Postcode.value,
            AddressComponentKind.Country.value,
            AddressComponentKind.Direction.value,
            AddressComponentKind.Landmark.value,
            AddressComponentKind.PostOfficeBox.value,
            AddressComponentKind.Separator.value,
        )

    /** Table 14, Directory `kind`. */
    val directoryKinds: Set<String> = setOf(DirectoryKind.Directory.value, DirectoryKind.Entry.value)

    /** Table 15, Link `kind`. */
    val linkKinds: Set<String> = setOf(LinkKind.Contact.value)

    /** Table 16, Media `kind`. */
    val mediaKinds: Set<String> = setOf(MediaKind.Photo.value, MediaKind.Sound.value, MediaKind.Logo.value)

    /** Table 11, Anniversary `kind`. */
    val anniversaryKinds: Set<String> =
        setOf(AnniversaryKind.Birth.value, AnniversaryKind.Death.value, AnniversaryKind.Wedding.value)

    /** Table 18, PersonalInfo `kind`. */
    val personalInfoKinds: Set<String> =
        setOf(PersonalInfoKind.Expertise.value, PersonalInfoKind.Hobby.value, PersonalInfoKind.Interest.value)

    /** Table 20, PersonalInfo `level`. */
    val personalInfoLevels: Set<String> =
        setOf(PersonalInfoLevel.High.value, PersonalInfoLevel.Medium.value, PersonalInfoLevel.Low.value)
}
