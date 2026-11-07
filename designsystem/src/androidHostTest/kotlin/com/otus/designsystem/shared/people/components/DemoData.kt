package com.otus.designsystem.shared.people.components

import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_drive_file_move
import com.otus.designsystem.resources.symbol_edit
import com.otus.designsystem.resources.symbol_flag
import com.otus.designsystem.resources.symbol_send
import com.otus.designsystem.shared.actionbar.components.BarAction
import com.otus.designsystem.shared.controls.elements.UiAction

/** Made-up people for the component screenshots, so no image shows real personal data. */
internal object DemoPeople {
    val ada = ChipAvatar("AL", colorIndex = 0)
    val grace = ChipAvatar("GH", colorIndex = 1)
    val linus = ChipAvatar("LT", colorIndex = 2)
    val margaret = ChipAvatar("MH", colorIndex = 0)
    val edsger = ChipAvatar("ED", colorIndex = 1)

    /** One more than an avatar stack shows. */
    val all = listOf(ada, grace, linus, margaret, edsger)

    val mira = NamedAvatar("Mira Chen", ChipAvatar("MC", colorIndex = 0))
    val tomas = NamedAvatar("Tomas Berg", ChipAvatar("TB", colorIndex = 1))
    val lena = NamedAvatar("Lena Park", ChipAvatar("LP", colorIndex = 2))
    val sam = NamedAvatar("Sam Ortiz", ChipAvatar("SO", colorIndex = 3))
    val noor = NamedAvatar("Noor Haddad", ChipAvatar("NH", colorIndex = 4))
}

/** Action bar actions shared by the bar screenshots. */
internal object DemoActions {
    val write = BarAction(UiAction("Compose", Res.drawable.symbol_edit) {})
    val send = BarAction(UiAction("Send", Res.drawable.symbol_send) {})
    val move = BarAction(UiAction("Move", Res.drawable.symbol_drive_file_move) {})
    val secondary =
        listOf(
            BarAction(UiAction("Archive", Res.drawable.symbol_archive) {}),
            move,
            BarAction(UiAction("Flag", Res.drawable.symbol_flag) {}),
        )
}
