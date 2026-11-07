package com.otus.accounts.testing

import com.otus.accounts.domain.AccountService
import com.otus.jmap.JmapCapability

/** JMAP Mail (RFC 8621), checked with `Mailbox/get`, as Otus Mail asks for it. */
val MailService = AccountService("mail", JmapCapability.MAIL, "Mailbox")
