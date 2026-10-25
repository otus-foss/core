package com.otus.jmap.sharing

const val SESSION_URL = "https://jmap.example.com/jmap/session"

/**
 * RFC 9670, Figure 1, with Jane's account verbatim. Its `...` placeholders become the principals
 * account `u33084183` that Figure 1 refers to, plus the session fields RFC 8620 requires.
 */
const val SHARING_SESSION = """
{"capabilities": {"urn:ietf:params:jmap:core": {}, "urn:ietf:params:jmap:principals": {},
   "urn:com.example:jmap:todo": {}},
 "accounts": {
    "u12345678": {
      "name": "jane.doe@example.com",
      "isPersonal": true,
      "isReadOnly": false,
      "accountCapabilities": {
        "urn:com.example:jmap:todo": {},
        "urn:ietf:params:jmap:principals:owner": {
          "accountIdForPrincipal": "u33084183",
          "principalId": "P105aga511jaa"
        }
      }
    },
    "u33084183": {
      "name": "Directory",
      "isPersonal": false,
      "isReadOnly": true,
      "accountCapabilities": {
        "urn:ietf:params:jmap:principals": {"currentUserPrincipalId": "P105aga511jaa"}
      }
    }
 },
 "primaryAccounts": {"urn:ietf:params:jmap:principals": "u33084183"},
 "username": "jane.doe@example.com", "apiUrl": "/jmap/", "state": "s1"}
"""

/** RFC 9670, Figure 3, with its `...` placeholders removed and the call id set to this client's `c0`. */
const val PRINCIPAL_GET_RESPONSE = """
{"methodResponses": [[ "Principal/get", {
  "accountId": "u33084183",
  "state": "7b8eff5zz",
  "list": [{
    "id": "P2342fnddd20",
    "type": "individual",
    "name": "Joe Bloggs",
    "description": null,
    "email": "joe.bloggs@example.com",
    "timeZone": "Australia/Melbourne",
    "capabilities": {
        "urn:com.example:jmap:todo": {
            "accountId": null,
            "mayShareWith": true
        }
    },
    "accounts": null
  }, {
    "id": "P674pp24095qo49pr",
    "name": "Board room",
    "type": "location"
  }],
  "notFound": []
}, "c0" ]], "sessionState": "s1"}
"""
