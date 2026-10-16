package com.otus.designsystem.components

import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_drive_file_move
import com.otus.designsystem.resources.symbol_edit
import com.otus.designsystem.resources.symbol_flag
import com.otus.designsystem.resources.symbol_send

/** Made-up people shared by the component screenshots. */
internal object DemoPeople {
    val ada = ChipAvatar("AL", colorIndex = 0)
    val grace = ChipAvatar("GH", colorIndex = 1)
    val linus = ChipAvatar("LT", colorIndex = 2)
    val margaret = ChipAvatar("MH", colorIndex = 0)
    val edsger = ChipAvatar("ED", colorIndex = 1)

    /** One more than an avatar stack shows. */
    val all = listOf(ada, grace, linus, margaret, edsger)
}

/** Action bar actions shared by the bar screenshots. */
internal object DemoActions {
    val write = BarAction("Compose", Res.drawable.symbol_edit) {}
    val send = BarAction("Send", Res.drawable.symbol_send) {}
    val move = BarAction("Move", Res.drawable.symbol_drive_file_move) {}
    val secondary =
        listOf(
            BarAction("Archive", Res.drawable.symbol_archive) {},
            move,
            BarAction("Flag", Res.drawable.symbol_flag) {},
        )
}
