# Action bar

Design spec for a bottom action bar shared by Mail and Calendar. Status: designed, not built.
Design canvas: https://claude.ai/artifact/H3pmhTDTnmTH6EFExRzG5Y (artboards `U2-Reversed-Token`
for the states and `U3-Read-View` for the bar in a screen). Treat the canvas as the visual
reference and this file as the source of truth for values and behavior.

## Summary

One bar at the bottom of a page. Up to three secondary icon actions sit in a group on the start
side. One primary action sits in a shell on the end side. When the screen has context for the
primary action (who you reply to, how many items you move, when an event is), the shell shows that
context on a dark area and the primary shrinks to an icon button at the shell's end. Without
context the primary fills the shell with its label. The primary's icon never moves between the
two states, and no button changes position while a screen is open.

## Where it lives

- `designsystem/.../components/ActionBar.kt`: the component (public).
- `designsystem/.../theme/ActionBarTokens.kt`: the values below (public object, like
  `PanelTokens`).
- Docs: add `ActionBar` to the components list in [designsystem.md](designsystem.md) and link
  this file.

Like every component here it takes display values and callbacks only. No ViewModels, navigation,
contact lookup or app models. Mail and Calendar map their own data into the values.

## Anatomy

```
 page inset 8                                                     page inset 8
 |                                                                          |
 ┌──────────────────────┐ 8 ┌─────────────────────────────────────┬────────┐
 │ [icon] [icon] [icon] │   │ (token)  Label                      │ [icon] │
 │                      │   │          Subtitle                   │        │
 └──────────────────────┘   └─────────────────────────────────────┴────────┘
  Secondary group             Primary shell: context area         action
```

1. **Secondary group**: 0–3 icon buttons.
2. **Primary shell**: dark container, fills the remaining width.
3. **Context area** (optional): token, label, subtitle.
4. **Action**: filled accent button with the primary's icon, flush with the shell's end edge
   and drawn over the shell's border.

No-context state: the action covers the whole shell (over the border on all four sides) and shows
`Label` at the start and the icon at the end.

## Tokens

Colors are `OtusColors`; existing tokens are named where they already exist. New tokens go in
`ActionBarTokens`.

| Token | Value | Notes |
| --- | --- | --- |
| `barInset` | 8dp | = `PageTokens.pageInset`, on all sides of the bar |
| `partGap` | 8dp | between group and shell |
| `radius` | 22dp | = `PanelTokens.panelRadius`; group, shell and action |
| `borderWidth` | 1dp | = `PanelTokens.borderWidth` |
| `height` | 62dp | outer height of group and shell, borders included |
| `groupPadding` | 6dp | inside the group border |
| `groupItemSize` | 48dp | = `PanelTokens.touchTarget`; secondary button box |
| `groupItemGap` | 2dp | |
| `groupItemRadius` | 16dp | press-feedback shape of a secondary button |
| `iconSize` | 20dp | = `PanelTokens.iconSize`; secondary icons, context Place icon |
| `primaryIconSize` | 22dp | the action's icon |
| `shellStartPadding` | 5dp | shell content inset from its start border; top, bottom, end are 0 |
| `contextStartPadding` | 2dp | extra inset before the token, so content starts 7dp in |
| `contextGap` | 10dp | token to text |
| `contextToActionGap` | 6dp | text area to action |
| `tokenSize` | 36dp | |
| `tokenOverlap` | 12dp | second avatar in a People token overlaps the first |
| `dateTokenRadius` | 10dp | Date token is a rounded square |
| `actionWidth` | 60dp | compact action, including its border |
| `actionOverlap` | 1dp | how far the action extends past the shell's border box |
| `actionEndPadding` | 19dp | action's end edge to its icon (icon center sits 29dp inside the shell's outer end edge) |
| `actionLabelStartPadding` | 20dp | label inset in the no-context state |

Colors:

| Part | Fill | Border | Content |
| --- | --- | --- | --- |
| Group | `bgDark` | `strokeBgDark` | icons `text1` |
| Shell | `bgDark` | `strokeBgDark` | label `text1`, subtitle `text2` |
| Action | `accentDark` | `accentRegular` | icon and label `text1` |
| Token (Person, Count, Date, Place) | `bgLight` | none | `text1` |
| People token, second circle | `bgLight` | 2dp ring `bgDark` (separates the overlap) | `text1` |

`text1` on `accentDark` passes contrast. Do not put `text3` or `accentLight` on `accentDark` or
`bgLight` (see the README contrast notes).

Type (Figtree via `panelTextStyle()`):

| Text | Size / line height | Weight | Tracking |
| --- | --- | --- | --- |
| Label (context area and full action) | 16 / 20sp | SemiBold 600 | 0 |
| Subtitle | 12 / 16sp | Regular 400 | 0.5sp |
| Token initials | 13sp | SemiBold | 0 |
| Count token | 16sp | SemiBold | 0 |
| Date token | month 8sp Regular uppercase, day 14sp SemiBold, line height 1 | | 0.5sp on month |

Label and subtitle are single line with an ellipsis. The label never truncates before the subtitle:
give the label its intrinsic width and let the subtitle take the rest.

## Layout

```
Row(Modifier.fillMaxWidth().padding(barInset), spacedBy(partGap), CenterVertically) {
    SecondaryGroup()                     // wraps its content
    PrimaryShell(Modifier.weight(1f))    // fills the rest
}
```

### Secondary group

- Row of `groupItemSize` boxes, `groupItemGap` apart, `groupPadding` inside a `radius` shape
  with border. Height comes out at 62dp.
- Each item is an icon-only button with a content description. Reuse `IconButton`'s press
  feedback (`controlPressFeedback`) clipped to `RoundedCornerShape(groupItemRadius)`; no
  per-item border or fill.
- 0 items: omit the group entirely and let the shell take the full width.

### Primary shell

- Fixed outer height 62dp (`heightIn(min = height)`; grows only for very large font scales),
  `radius` shape, `bgDark` fill, `strokeBgDark` border.
- With context: `[contextStartPadding][token][contextGap][text column, weight 1][contextToActionGap][action]`.
  With no token, the text column starts right after the start paddings.
- **Action overlap**: the action is 60 × 62dp but positioned so its top, end and bottom edges sit
  `actionOverlap` (1dp) outside the shell's border box, i.e. its border is drawn exactly over the
  shell's border. Compose has no negative padding, so either:
  - draw the shell's fill and border in `drawBehind` and place the action with a
    `Modifier.layout` that measures it 2dp taller and offsets it by -1dp on y and +1dp on x, or
  - build the shell as a `Box`: border layer underneath, content row on top with the action
    using `Modifier.offset` plus `requiredHeight`.

  The action is drawn after (above) the shell border. Do not clip the shell's content to its shape,
  or the overlap disappears.
- No context: the action spans the whole shell and overlaps the border by 1dp on all four sides
  (outer size = shell + 2dp each way). Content: label at `actionLabelStartPadding`, icon at the end
  with `actionEndPadding`, `SpaceBetween`.

### Fixed positions (required)

- The primary icon's center is 29dp from the shell's outer end edge in both states. Animate the
  action's width and keep its end edge anchored; never re-center the icon.
- Secondary slots fill from the start. A screen picks its secondary actions once; if one is not
  available right now, show it disabled instead of removing it, so the others don't shift.
- The same action keeps the same slot on every screen in both apps where it appears (for example,
  Delete is always in the same slot). Keep a slot table in the feature docs.
- RTL: mirror everything. The group sits at the end, the shell at the start, the action at the
  shell's start edge. The primary icon mirrors only if it is directional (reply, forward).

## Context tokens

Exactly one token per context, or none. The token repeats information that is also in the text,
so it is `contentDescription = null` / decorative.

| Kind | Look | Use |
| --- | --- | --- |
| `Person` | `Avatar` at 36dp: photo or initials, `colorIndex = 1` (`bgLight`) | reply, forward, a single attendee |
| `People` | first avatar plus a second circle with `+N`, overlapping by 12dp | reply all, invites |
| `Count` | circle with the number | bulk actions on a selection |
| `Date` | 36dp rounded square (10dp): month over day | event actions, snooze |
| `Place` | circle with a 20dp Symbol (folder, calendar) | move to a named folder |

Shapes are fixed by meaning: circles are people and counts, the rounded square is always time.
`People` shows at most one avatar plus the count circle in this bar (the panel's
`AvatarStackChip` can show more; this bar has less room).

## Proposed API

```kotlin
public data class BarAction(
    val label: String,                 // spoken label, also used for tooltips
    val icon: DrawableResource,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

public sealed interface ContextToken {
    public data class Person(val initial: String, val image: ImageBitmap? = null) : ContextToken
    public data class People(val first: Person, val more: Int) : ContextToken
    public data class Count(val count: Int) : ContextToken
    public data class Date(val month: String, val day: String) : ContextToken
    public data class Place(val icon: DrawableResource) : ContextToken
}

public data class BarContext(
    val subtitle: String,              // "to Maren Holt", "5 people", "Fri 16 Oct · 10:00"
    val token: ContextToken? = null,
)

@Composable
public fun ActionBar(
    primary: BarAction,
    modifier: Modifier = Modifier,
    secondary: List<BarAction> = emptyList(),   // 0..3; more is a programming error
    context: BarContext? = null,
)
```

`primary.label` is the label in both states ("Reply all" in the context area or on the full
action). Names follow the existing Kotlin style; adjust to match `ContextPanel` conventions if they
differ.

## States and motion

- **Press**: `controlPressFeedback` on every button (text1 at 10%, 100ms, standard curve), clipped to
  the visible shape. The whole shell is one button in the context state, so pressing anywhere on it
  highlights the shell and runs `primary.onClick`.
- **Disabled**: icons and labels turn `text3`; fills stay (system rule). A disabled primary uses
  `bgDark` fill like `ActionButton`.
- **Context appears / disappears**: 220ms (`PanelTokens.MODE_TRANSITION_MILLIS`), `MotionTokens.standard`.
  The action's width animates between full and 60dp with its end edge fixed; the context content
  fades in after the action has cleared its space (fade starts at ~40% of the width animation) and
  fades out first on the way back; the full-state label crossfades with it. Respect the system
  animation scale.
- Changing context content (subtitle text, token) without a state change: crossfade the text using
  `AnimatedText` timings (260ms in, 180ms out).
- No shadows or elevation, like everything else in the system.

## Accessibility

- Secondary buttons: `contentDescription = label`, `Role.Button`, 48dp targets.
- Context state: the shell is a single button with
  `contentDescription = "${primary.label}, ${context.subtitle}"` ("Reply all, 5 people").
  The action inside it is not a separate focus stop.
- No-context state: `contentDescription = primary.label`.
- Long-press on a secondary icon shows its label (platform tooltip) since the group has no text.
- Large text: the shell may grow taller than 62dp; the group stays 62dp and centers vertically.

## Placement in a screen

- Bottom of the page, outside the scrolling content, `barInset` from the screen edges, above the
  navigation bar inset (`WindowInsets.navigationBars`) and above the IME when it is open.
- Background behind the bar is the page (`bgRegular`); the bar does not get its own strip.
- Give the scrolling content bottom padding equal to the bar's height plus insets so the last item
  can scroll above it.
- It replaces the open message's `MessageActionRow` in otus-mail (the TextButton row in the
  current message dialog) when the read view becomes a page.

## Screen mapping (initial)

| Screen | Secondary (slot order) | Primary | Context |
| --- | --- | --- | --- |
| Mail · open message | Archive, Delete, Move | Reply / Reply all | Person or People, "to Maren Holt" / "5 people" |
| Mail · selection | Mark read, Flag, Delete | Move | Count, "3 messages" |
| Calendar · event | Delete, Duplicate, Share | Edit | Date, "10:00–11:30" |
| Calendar · day | Today, Search | New event | none |

Mail has no compose screen yet. Until it does, the open message's primary should be **Move**
(Count/Place context as fits) and Reply joins later; the slot table above already reserves it.

## Assets to add

These symbols are used by the mapping but are not in `composeResources/drawable` yet. Add them
from Material Symbols Rounded the same way as the existing 33 (see the asset provenance notes),
then use them through `Symbol`:

- `symbol_reply`, `symbol_reply_all`, `symbol_forward`
- `symbol_content_copy` (Duplicate), `symbol_share`
- `symbol_today` (Calendar day, go to today)

Already available: `archive`, `delete`, `drive_file_move`, `mark_email_read`, `mark_email_unread`,
`flag`, `edit`, `add`, `search`, `folder`, `bookmark`.

## Tests

Follow the `ContextPanel` test style (Robolectric screenshots under `build/reports/`):

- Snapshot each state: no context, Person, People, Count, Date, 0–3 secondary items, RTL,
  font scale 1.3 and 2.0.
- Assert the primary icon's bounds are identical in the context and no-context states, and during
  every frame of the transition.
- Assert the action's border bounds equal the shell's border bounds on the top, end and bottom
  edges (the overlap), and that secondary slot positions don't change when an action is disabled.
- Semantics: the shell exposes one button with the combined description.

## Out of scope

- A split chevron or menu on the primary (explored on the canvas as F5 and T2; not chosen).
- Swipe or drag gestures on the bar.
- Hiding the bar on scroll.
