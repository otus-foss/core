package com.otusfoss.core.jscontact

/**
 * One valid Card that holds every RFC 9553 Card property, built from the property figures of section 2:
 * figures 7, 8, 10 to 13 (figure 11 sets kind, name, uid, and members), 15, 21 to 34, 36 to 38, and 41 to
 * 44. Values are verbatim, with whitespace compacted. Figures 22 and 24 share the organizations map, and
 * figures 31 to 33 the addresses map. Figure 36 gets the closing brace it lacks. The other figures would
 * repeat a property, figure 35 splits a string across lines, and figure 40 patches a title this Card lacks.
 */
const val CARD_OF_ALL_FIGURES = """
{
  "@type": "Card",
  "version": "1.0",
  "created": "2022-09-30T14:35:10Z",
  "kind": "group",
  "language": "de-AT",
  "name": { "full": "The Doe family" },
  "uid": "urn:uuid:ab4310aa-fa43-11e9-8f0b-362b9e155667",
  "members": {
    "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af": true,
    "urn:uuid:b8767877-b4a1-4c70-9acc-505d3819e519": true
  },
  "prodId": "ACME Contacts App version 1.23.5",
  "relatedTo": {
    "urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6": { "relation": { "friend": true } },
    "8cacdfb7d1ffdb59@example.com": { "relation": {} }
  },
  "updated": "2021-10-31T22:27:10Z",
  "nicknames": { "k391": { "name": "Johnny" } },
  "organizations": {
    "o1": {
      "name": "ABC, Inc.",
      "units": [{ "name": "North American Division" }, { "name": "Marketing" }],
      "sortAs": "ABC"
    },
    "o2": { "name": "ABC, Inc." }
  },
  "speakToAs": {
    "grammaticalGender": "neuter",
    "pronouns": { "k19": { "pronouns": "they/them", "pref": 2 }, "k32": { "pronouns": "xe/xir", "pref": 1 } }
  },
  "titles": {
    "le9": { "kind": "title", "name": "Research Scientist" },
    "k2": { "kind": "role", "name": "Project Leader", "organizationId": "o2" }
  },
  "emails": {
    "e1": { "contexts": { "work": true }, "address": "jqpublic@xyz.example.com" },
    "e2": { "address": "jane_doe@example.com", "pref": 1 }
  },
  "onlineServices": {
    "x1": { "uri": "xmpp:alice@example.com" },
    "x2": { "service": "Mastodon", "user": "@alice@example.net", "uri": "https://example.net/@alice" }
  },
  "phones": {
    "tel0": {
      "contexts": { "private": true },
      "features": { "voice": true },
      "number": "tel:+1-555-555-5555;ext=5555",
      "pref": 1
    },
    "tel3": { "contexts": { "work": true }, "number": "tel:+1-201-555-0123" }
  },
  "preferredLanguages": {
    "l1": { "language": "en", "contexts": { "work": true }, "pref": 1 },
    "l2": { "language": "fr", "contexts": { "work": true }, "pref": 2 },
    "l3": { "language": "fr", "contexts": { "private": true } }
  },
  "calendars": {
    "calA": { "kind": "calendar", "uri": "webcal://calendar.example.com/calA.ics" },
    "project-a": { "kind": "freeBusy", "uri": "https://calendar.example.com/busy/project-a" }
  },
  "schedulingAddresses": { "sched1": { "uri": "mailto:janedoe@example.com" } },
  "addresses": {
    "k23": {
      "contexts": { "work": true },
      "components": [
        { "kind": "number", "value": "54321" },
        { "kind": "separator", "value": " " },
        { "kind": "name", "value": "Oak St" },
        { "kind": "locality", "value": "Reston" },
        { "kind": "region", "value": "VA" },
        { "kind": "separator", "value": " " },
        { "kind": "postcode", "value": "20190" },
        { "kind": "country", "value": "USA" }
      ],
      "countryCode": "US",
      "defaultSeparator": ", ",
      "isOrdered": true
    },
    "k25": {
      "components": [
        { "kind": "number", "value": "46" },
        { "kind": "name", "value": "1 Sukhumvit 51 Alley" },
        { "kind": "subdistrict", "value": "Khlong Tan Nuea" },
        { "kind": "district", "value": " Watthana" },
        { "kind": "locality", "value": "Bangkok" },
        { "kind": "country", "value": "Thailand" },
        { "kind": "postcode", "value": "10110" }
      ],
      "defaultSeparator": ", ",
      "isOrdered": true
    },
    "k26": {
      "components": [
        { "kind": "block", "value": "2-7" },
        { "kind": "separator", "value": "-" },
        { "kind": "number", "value": "2" },
        { "kind": "separator", "value": " " },
        { "kind": "district", "value": "Marunouchi" },
        { "kind": "locality", "value": "Chiyoda-ku" },
        { "kind": "region", "value": "Tokyo" },
        { "kind": "separator", "value": " " },
        { "kind": "postcode", "value": "100-8994" }
      ],
      "defaultSeparator": ", ",
      "full": "2-7-2 Marunouchi, Chiyoda-ku, Tokyo 100-8994",
      "isOrdered": true
    }
  },
  "localizations": {
    "jp": {
      "addresses/k26": {
        "components": [
          { "kind": "region", "value": "東京都" },
          { "kind": "locality", "value": "千代田区" },
          { "kind": "district", "value": "丸ノ内" },
          { "kind": "block", "value": "2-7" },
          { "kind": "separator", "value": "-" },
          { "kind": "number", "value": "2" },
          { "kind": "postcode", "value": "〒100-8994" }
        ],
        "defaultSeparator": "",
        "full": "〒100-8994東京都千代田区丸ノ内2-7-2",
        "isOrdered": true
      }
    }
  },
  "cryptoKeys": { "mykey1": { "uri": "https://www.example.com/keys/jdoe.cer" } },
  "directories": {
    "dir1": { "kind": "entry", "uri": "https://dir.example.com/addrbook/jdoe/Jean%20Dupont.vcf" },
    "dir2": { "kind": "directory", "uri": "ldap://ldap.example/o=Example%20Tech,ou=Engineering", "pref": 1 }
  },
  "links": { "link3": { "kind": "contact", "uri": "mailto:contact@example.com", "pref": 1 } },
  "media": {
    "res45": { "kind": "sound", "uri": "CID:JOHNQ.part8.19960229T080000.xyzMail@example.com" },
    "res47": { "kind": "logo", "uri": "https://www.example.com/pub/logos/abccorp.jpg" },
    "res1": { "kind": "photo", "uri": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAASABIAAD/4..." }
  },
  "anniversaries": {
    "k8": { "kind": "birth", "date": { "year": 1953, "month": 4, "day": 15 } },
    "k9": {
      "kind": "death",
      "date": { "@type": "Timestamp", "utc": "2019-10-15T23:10:00Z" },
      "place": { "full": "4445 Tree Street\nNew England, ND 58647\nUSA" }
    }
  },
  "keywords": { "internet": true, "IETF": true },
  "notes": {
    "n1": {
      "note": "Open office hours are 1600 to 1715 EST, Mon-Fri",
      "created": "2022-11-23T15:01:32Z",
      "author": { "name": "John" }
    }
  },
  "personalInfo": {
    "pi2": { "kind": "expertise", "value": "chemistry", "level": "high" },
    "pi1": { "kind": "hobby", "value": "reading", "level": "high" },
    "pi6": { "kind": "interest", "value": "r&b music", "level": "medium" }
  }
}
"""
