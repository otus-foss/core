package com.otusfoss.core.jscontact

// Whole Cards from RFC 9553 and RFC 9610, verbatim. Fragments of a Card are in CardOfAllFigures.kt.

// RFC 9553, figure 6.
const val CARD_FIGURE_6 = """
{
  "@type": "Card",
  "version": "1.0",
  "uid": "22B2C7DF-9120-4969-8460-05956FE6B065",
  "kind": "individual",
  "name": {
    "components": [
      { "kind": "given", "value": "John" },
      { "kind": "surname", "value": "Doe" }
    ],
    "isOrdered": true
  }
}
"""

// RFC 9553, figure 20. It has no version and no uid.
const val CARD_FIGURE_20 = """
{
  "@type": "Card",
  "language": "zh-Hant",
  "name": {
    "components": [
      { "kind": "surname", "value": "孫" },
      { "kind": "given", "value": "中山" },
      { "kind": "given2", "value": "文" },
      { "kind": "given2", "value": "逸仙" }
    ]
  },
  "localizations": {
    "yue": {
      "name/phoneticSystem": "jyut",
      "name/phoneticScript": "Latn",
      "name/components/0/phonetic": "syun1",
      "name/components/1/phonetic": "zung1saan1",
      "name/components/2/phonetic": "man4",
      "name/components/3/phonetic": "jat6sin1"
    }
  }
}
"""

// RFC 9553, figure 39. It has no @type, no version, and no uid.
const val CARD_FIGURE_39 = """
{
  "name": {
    "components": [
      { "kind": "title", "value": "Mr." },
      { "kind": "given", "value": "Ivan" },
      { "kind": "given2", "value": "Petrovich" },
      { "kind": "surname", "value": "Vasiliev" }
    ]
  },
  "localizations": {
    "uk-Cyrl": {
      "name": {
        "components": [
          { "kind": "title", "value": "г-н" },
          { "kind": "given", "value": "Иван" },
          { "kind": "given2", "value": "Петрович" },
          { "kind": "surname", "value": "Васильев" }
        ]
      }
    }
  }
}
"""

// RFC 9553, figure 40, a fragment of Card members wrapped in braces.
const val CARD_FIGURE_40 = """
{
"name": {
  "full": "Gabriel García Márquez"
},
"titles": {
  "t1": {
    "kind": "title",
    "name": "novelist"
  }
},
"localizations": {
  "es": {
    "titles/t1/name": "escritor"
  }
}
}
"""

// RFC 9610, section 4.1, figure 2: the ContactCard in the ContactCard/get response.
const val CONTACT_CARD_EXAMPLE = """
{
  "id": "3",
  "addressBookIds": {
    "062adcfa-105d-455c-bc60-6db68b69c3f3": true
  },
  "name": {
    "components": [
      { "kind": "given", "value": "Joe" },
      { "kind": "surname", "value": "Bloggs" }
    ],
    "isOrdered": true
  },
  "emails": {
    "0": {
      "contexts": {
        "private": true
      },
      "address": "joe.bloggs@example.com"
    }
  }
}
"""
