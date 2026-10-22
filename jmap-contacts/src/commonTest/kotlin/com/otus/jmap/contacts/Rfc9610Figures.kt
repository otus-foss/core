package com.otus.jmap.contacts

// The four figures of RFC 9610, section 4, verbatim without the page margin.

/** Figure 1: fetching every address book and card. */
val FIGURE_1 =
    """
    [
      ["AddressBook/get", {
        "accountId": "a0x9"
      }, "0"],
      ["ContactCard/get", {
        "accountId": "a0x9"
      }, "1"]
    ]
    """.trimIndent()

/** Figure 2: the response to [FIGURE_1]. */
val FIGURE_2 =
    """
    [
      ["AddressBook/get", {
        "accountId": "a0x9",
        "list": [{
          "id": "062adcfa-105d-455c-bc60-6db68b69c3f3",
          "name": "Personal",
          "description": null,
          "sortOrder": 0,
          "isDefault": true,
          "isSubscribed": true,
          "shareWith": {
            "3f1502e0-63fe-4335-9ff3-e739c188f5dd": {
              "mayRead": true,
              "mayWrite": false,
              "mayShare": false,
              "mayDelete": false
            }
          },
          "myRights": {
            "mayRead": true,
            "mayWrite": true,
            "mayShare": true,
            "mayDelete": false
          }
        }, {
          "id": "cd40089d-35f9-4fd7-980b-ba3a9f1d74fe",
          "name": "Autosaved",
          "description": null,
          "sortOrder": 1,
          "isDefault": false,
          "isSubscribed": true,
          "shareWith": null,
          "myRights": {
            "mayRead": true,
            "mayWrite": true,
            "mayShare": true,
            "mayDelete": false
          }
        }],
        "notFound": [],
        "state": "~4144"
      }, "0"],
      ["ContactCard/get", {
        "accountId": "a0x9",
        "list": [{
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
        }],
        "notFound": [],
        "state": "ewarbckaqJ::112"
      }, "1"]
    ]
    """.trimIndent()

/** Figure 3: making "Autosaved" the default address book. */
val FIGURE_3 =
    """
    [
      ["AddressBook/set", {
        "accountId": "a0x9",
        "onSuccessSetIsDefault": "cd40089d-35f9-4fd7-980b-ba3a9f1d74fe"
      }, "0"]
    ]
    """.trimIndent()

/**
 * Figure 4: the response to [FIGURE_3]. The RFC nests `oldState` and `newState` inside `updated`,
 * where RFC 8620, section 5.3 puts them at the top level.
 */
val FIGURE_4 =
    """
    [
      ["AddressBook/set", {
        "accountId": "a0x9",
        "updated": {
          "cd40089d-35f9-4fd7-980b-ba3a9f1d74fe": {
            "isDefault": true
          },
          "062adcfa-105d-455c-bc60-6db68b69c3f3": {
            "isDefault": false
          },
          "oldState": "~4144",
          "newState": "~4148"
        }
      }, "0"]
    ]
    """.trimIndent()

/** [FIGURE_4] with its states moved to the top level, where RFC 8620, section 5.3 puts them. */
val FIGURE_4_CORRECTED =
    """
    [
      ["AddressBook/set", {
        "accountId": "a0x9",
        "oldState": "~4144",
        "newState": "~4148",
        "updated": {
          "cd40089d-35f9-4fd7-980b-ba3a9f1d74fe": {
            "isDefault": true
          },
          "062adcfa-105d-455c-bc60-6db68b69c3f3": {
            "isDefault": false
          }
        }
      }, "0"]
    ]
    """.trimIndent()

const val PERSONAL = "062adcfa-105d-455c-bc60-6db68b69c3f3"
const val AUTOSAVED = "cd40089d-35f9-4fd7-980b-ba3a9f1d74fe"
