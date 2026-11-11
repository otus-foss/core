package com.otusfoss.core.designsystem.mail.read.components

import com.otusfoss.core.designsystem.mail.folders.components.FolderName
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_archive
import com.otusfoss.core.designsystem.shared.people.components.DemoPeople
import com.otusfoss.core.designsystem.theme.colors.FolderColor

/** Made-up messages and folders for the read view screenshots. */
internal object DemoRead {
    val thread =
        listOf(
            ThreadEntry("Grace Hopper", DemoPeople.grace, "12 Oct", "Let's lock it in on Thursday at 10."),
            ThreadEntry("You", DemoPeople.ada, "11 Oct", "Can we meet Thursday to settle it?"),
            ThreadEntry("Radia Perlman", DemoPeople.linus, "11 Oct", "Infra is fine with either plan."),
            ThreadEntry("Margaret Knuth", DemoPeople.margaret, "10 Oct", "I went through the plan with legal."),
            ThreadEntry("Ada Lovelace", DemoPeople.edsger, "9 Oct", "Kicking this off. Draft plan attached."),
        )

    val invoices = FolderName(listOf("Finance", "Invoices"), FolderColor.Yellow, system = false)
    val freelancing = FolderName(listOf("Freelancing"), FolderColor.Pink, system = false)
    val clients = FolderName(listOf("Clients"), FolderColor.Blue, system = false)
    val archive = FolderName(listOf("Archive"), color = null, system = true, icon = Res.drawable.symbol_archive)
    val taxes = FolderName(listOf("Taxes"), FolderColor.Green, system = false)
}
