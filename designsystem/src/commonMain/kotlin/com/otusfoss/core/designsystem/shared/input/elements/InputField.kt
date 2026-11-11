package com.otusfoss.core.designsystem.shared.input.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/** Labelled single-line form input, with its [note] below; a note without text shows only its mark. */
@Composable
public fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    note: InputNote? = null,
    kind: InputKind = InputKind.Text,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.iconGap)) {
        FieldLabel(label)
        InputBox(value, onValueChange, label, InputLook(placeholder, kind, valid = note?.valid))
        // A note may carry only its mark, such as a server that answered.
        if (note != null && note.text.isNotEmpty()) {
            Text(
                note.text,
                Modifier
                    .padding(horizontal = SheetTokens.gap)
                    // A note that follows a check, such as a server's, is read out when it changes.
                    .semantics { if (note.valid != null) liveRegion = LiveRegionMode.Polite },
                style = panelTextStyle(),
                fontSize = SheetTokens.detailSize,
                color = OtusColors.text2,
            )
        }
    }
}
