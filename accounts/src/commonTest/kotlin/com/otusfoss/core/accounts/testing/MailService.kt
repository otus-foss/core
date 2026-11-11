package com.otusfoss.core.accounts.testing

import com.otusfoss.core.accounts.domain.AccountService
import com.otusfoss.core.jmap.JmapCapability

/** JMAP Mail (RFC 8621), checked with `Mailbox/get`, as Otus Mail asks for it. */
val MailService = AccountService("mail", JmapCapability.MAIL, "Mailbox")
