# Design system

The `designsystem` module (`com.otusfoss.core.designsystem`, artifact `com.otusfoss.core:designsystem`) holds the
Otus theme, elements, and components. It moved here from otus-mail in version 0.2.0. Elements and
components take values and callbacks; they own no ViewModels, navigation, networking, or storage.
`HtmlView` is the one exception: with `loadRemote`, its web view fetches the images, styles, and
fonts the document names. Without it, the platform also blocks every web request beneath the
document's Content Security Policy: `blockNetworkLoads` on Android, a content rule list on iOS. On
iOS each view keeps cookies and cache in memory only. Without `loadRemote`, and with
`onShowRemote`, each remote image shows as a dashed placeholder of its size, and a tap on one runs
`onShowRemote`. The placeholder link carries a key
made for each render, so a link in the message cannot fake it. A `HtmlQuote` folds under the body
in a `<details>` element, so it unfolds without scripts; the body is sealed first, so an element
it leaves open, such as `<style>` or `<a>`, cannot swallow or wrap the fold. `HtmlView` prepares
the page on a background thread, and shows nothing in its place until the first page is ready.
The preparation reads the sender's markup in one linear scan, as a browser tokenizes it, rather
than with regular expressions that could backtrack, so no message can stall or crash it. Only real
tags count: an `<img>` or a `color` attribute inside a comment, a style sheet, or an attribute
value is left alone.
The user picks one of four themes; the app can generate one from a hue. Changing any single color saves a custom
theme. The Otus theme with Otus blue is the palette recorded in [mail design foundations](suite/mail-design.md). See
[decision 0014](decisions/0014-user-themes.md).

## Packages

Elements and components live in `shared/<group>` when any app may use them and in
`mail/<feature>` when they draw something only Mail has. Each group has an `elements` layer, the
smallest pieces, and a `components` layer, which combines elements. `theme.OtusTheme` and the
generated `resources.Res` sit at the module root. The KDoc on each declaration is the inventory;
this page holds the rules. See [decision 0018](decisions/0018-ui-domain-packages.md) for the
dependency rules.

| Package | Holds |
| --- | --- |
| `shared/actionbar` | The action bar, its groups, menus, morphs, and notices |
| `shared/contacts` | Contact rows, fields, headers, and the letter index |
| `shared/controls` | Buttons, chips, pills, toggles, counts, press feedback, and `UiAction` |
| `shared/dates` | Calendars, wheel pickers, and date sheets |
| `shared/feedback` | Empty states, info strips and tips, pull to refresh, and toasts |
| `shared/filters` | The filter panel and its rows |
| `shared/folders` | Folder glyphs, paths, stacks, and color swatches |
| `shared/input` | Text fields and search fields |
| `shared/overlays` | Sheets (`SheetFrame`, `ListSheet`, `InputSheet`), dialogs, menus, and popup placement |
| `shared/pages` | Page scaffolds, slides, insets, and pinned chrome |
| `shared/panels` | The context panel, its search, stages, and the grown panel |
| `shared/people` | Avatars, person chips, pills, and rows |
| `shared/reorder` | Reorderable columns and trees |
| `shared/rows` | Row groups, cards, captions, details, folds, and list helpers such as drag to select |
| `shared/selection` | `PickRow`, `ChoiceSheet`, `OrderedChoiceSheet`, dropdowns, and wheels |
| `shared/settings` | Setting rows and sections, theme cards, and the change log |
| `shared/sidebar` | The sidebar, its header, entries, folders, and footer |
| `shared/swipe` | Swipe actions on rows and swipe to dismiss |
| `mail/attachments` | Attachment chips and strips |
| `mail/brand` | Mail's app icon |
| `mail/compose` | Compose fields, rich text, the formatting and image bars, and the From pill |
| `mail/folders` | Folder rows, rules, hints, destination pills, and folder symbols |
| `mail/inbox` | The compact conversation row |
| `mail/launch` | Mail's launch animation and its art |
| `mail/quickview` | The quick view's modules |
| `mail/read` | Read view strips, quotes, threads, and `HtmlView` |
| `mail/read/html` | Plain Kotlin: the HTML scanner, sanitizer, quote folds, and CSP pieces `HtmlView` uses |
| `mail/sidebar` | Mailbox tiles and the Pinned tile editor |
| `theme` | `OtusTheme`; `colors`, `motion`, `brand`, `shapes`, `typography`, and `tokens.<family>` |

A part moves from `mail/` to `shared/` when a second app needs it, not before. Shared code never
names its Mail callers, not even in KDoc, so a move does not leave stale prose behind.

## Tokens

Every size, gap, radius, and text size comes from `theme`. Components read scales and their own
token objects, never a token object named after another screen.

- **Spacing.** `theme/tokens/core/Spacing.kt` has the steps `xs` (4 dp), `s` (6 dp), `l` (12 dp), and `xl` (16 dp), the ones a token uses, and the
  roles `gutter` (16 dp, the inset from a screen or card edge), `indent` (16 dp, one tree level),
  `iconGap` (4 dp), and `inlineGap` (6 dp, between chips or an avatar and a name). A feature
  token that means one of these roles is defined as it, such as `SheetTokens.inset = Spacing.gutter`.
- **Sizes.** `theme/tokens/core/Sizes.kt` has the touch target (48 dp), icon (20 dp), border
  (1 dp), chip (36 dp), compact row (36 dp), and edge fade (24 dp).
- **Type.** `theme/typography/TypeScale.kt` has five roles: `title` (20 sp), `body` (16 sp, the
  size of `panelTextStyle`), `label` (14 sp), `detail` (12 sp), and `badge` (11 sp). A secondary
  line under a title is `detail` on every surface.
- **Feature tokens.** Each shared group, or a component with values of its own, has one token
  object, such as `ControlTokens`, `StripTokens`, `PickTokens`, or `FolderTokens`. Mail screens
  have theirs (`InboxTokens`, `ReadTokens`, `ComposeTokens`); shared components do not read them.
  Token KDoc says what a value is for, not which screens use it.

## Corners

Every radius comes from one scale in `theme/tokens/layout/CornerTokens.kt`. Feature tokens such as
`SheetTokens.radius` name a step, so a change to the scale reaches every part that uses it. A part
inside another takes the outer radius minus its inset: a menu row is
`MenuTokens.radius - MenuTokens.inset`. Pills, avatars, and circles round to half their height and
take no step. No corner rounds past half its own height. `RoundedCornerShape` scales its corners
down for that, and code that draws its own rounded rectangles caps the radius itself. Corners use
`SmoothCornerShape`, with `CornerTokens.SMOOTHING` (0.6, as on iOS).

| Step | Radius | For |
| --- | --- | --- |
| `tiny` | 4 dp | The ends of a date range |
| `small` | 8 dp | Compact chips and sketch bars |
| `chip` | 10 dp | Chips, value buttons, and tokens |
| `button` | 15 dp | Search fields and cards inside cards |
| `panel` | 16 dp | The control panel and the panels that grow out of it |
| `row` | 18 dp | Rows and cards inside pages, menus, and sheets |
| `large` | 20 dp | Nudge cards |
| `surface` | 22 dp | Menus, dropdowns, and the top corners of sheets and dialogs |

## One component per job

When two components do the same job, keep one and give it the parameter the other needed. Add a
parameter only when a caller needs it.

- **Sheet or popup.** Tasks and choices open a sheet. A popup is only for a warning or an
  irreversible confirmation (`ConfirmDialog`, `Confirmation`).
- **Sheets.** `SheetFrame` is the one sheet chrome: a title with Done, or with `closeButton` a ✕
  and the confirm button at the bottom. `ListSheet` adds a search slot and Clear for sheets that
  list things, whose list may end with a `SheetFootnote`. `ChoiceSheet` picks one option, at once or on Done with `saveOnDone`.
- **Rows that pick.** `PickRow` with a `PickMark` (radio, checkbox, check, check circle, or card) is
  the one choice row; `PersonPickRow` is its form for a person, with the address at the end.
- **Actions.** `UiAction` (label, symbol, enabled, onClick) is the one action value. Surfaces wrap
  it with their own extras: `BarAction` adds a menu, `SwipeAction` whether the row leaves, and
  `OptionsItem.Action` whether the entry opens a page.
- **Popups.** Menus, dropdowns, tooltips, and the quick action bar place themselves through
  `PopupPlacement.kt`, which keeps them inside the window.
- **Dismissing.** Sheets, panel sheets, and toasts close by one `DismissRule`: past a distance or
  faster than a fling.
- **Chips.** `Chip` is the one selectable chip. Its `ChipStyle` sets how it toggles (`ChipRole`)
  and its fill (`ChipLook.Raised` sits on a `bgDark` surface); a `ChipLabel` can carry a prefix and
  a detail after a divider line.

## Motion

Every animation in the design system and in Mail takes its duration and curve from `Motion`, in
`theme/motion/Motion.kt`. The rules for picking a speed and a curve are in
[mail design foundations](suite/mail-design.md#motion).

`MotionCurve` holds five cubic Bézier curves:

| Curve | Bézier | Use |
| --- | --- | --- |
| `Standard` | (0.4, 0, 0.2, 1) | The default, for anything that changes in place |
| `Reverse` | (0.8, 0, 0.6, 1) | `Standard` played backward, for undoing a change so it retraces its steps |
| `Enter` | (0.2, 0, 0, 1) | Decelerates, for sheets, the sidebar, and text that arrive |
| `Exit` | (0.4, 0, 1, 1) | Accelerates, for the same things as they leave |
| `Rise` | (0.2, 0.8, 0.25, 1) | Jumps off at once and settles slowly, for the launch animation only |

`MotionSpeed` divides the 240 ms base duration (`Motion.BASE_MILLIS`) by its factor:

| Speed | Factor | Duration | Use |
| --- | --- | --- | --- |
| `Quick` | 2 | 120 ms | Press feedback, the refresh indicator |
| `Fast` | 1.5 | 160 ms | Toggle switch, reorder shift, action bar collapse, outgoing text |
| `Normal` | 1 | 240 ms | Panel modes, sheets, pages sliding, row selection, swipe flash, incoming text |
| `Relaxed` | 0.8 | 300 ms | Sidebar opening, a swiped row settling |

A duration is `240 ms / (factor × scale)`, rounded to the nearest millisecond. The scale comes
from `LocalMotionScale`, which defaults to 1. Providing 2 runs every animation twice as fast, and
0.5 runs it at half speed. Android's animator duration scale, set in the developer options or by
the system's remove animations setting, applies on top of the result, because Compose reads it
for every animation.

Apps provide the user's `AnimationSpeed` as the scale. The Animations row on the shared
appearance page sets it through `AnimationSettingsState`: Slow is 0.75, Regular 1, Fast 1.5, and
Instant is infinite, so every duration rounds to 0 ms.

`SlidingPages` and `SlidingOverlay` slide whole pages, but chrome that both pages draw stays
still. When the page that leaves and the page that arrives both have a `ContextPanel`, or both
have a bar in an `ActionBarHost`, that chrome keeps its place while the rest of each page slides
under it. The arriving page's chrome draws on top, and the leaving page's copy fades out under it.
Chrome that only one of the pages has slides with its page.

A composable reads the scale for you:

```kotlin
val thumb by animateDpAsState(target, motionTween(MotionSpeed.Fast))
```

Transition lambdas, such as `AnimatedContent`'s `transitionSpec`, are not composable. Read the
scale outside the lambda and pass it in:

```kotlin
val motionScale = LocalMotionScale.current
AnimatedContent(
    state,
    transitionSpec = { fadeIn(Motion.tween(scale = motionScale)) togetherWith fadeOut(Motion.tween(scale = motionScale)) },
) { ... }
```

State holders that animate outside composition, such as `SidebarState`, the sheet position, and
the swipe settle, receive the scale from the composable that creates or drives them, and call
`Motion.tween(speed, curve, scale)` or `Motion.millis(speed, scale)`. The sidebar, for example,
shortens its tween to the distance left after a partial swipe:

```kotlin
val millis = Motion.millis(MotionSpeed.Relaxed, motionScale)
val spec = TweenSpec<Float>((millis * remaining).roundToInt(), easing = MotionCurve.Enter.easing)
```

## Resources

The module bundles the static Figtree Regular, SemiBold, Bold, and Black files from the upstream
Figtree project with their SIL Open Font License. `panelTextStyle()` registers all four, so each
weight draws from its own file. Bold and Black cover Android's bold text setting, which adds 300 to
every requested weight: Normal and SemiBold become Bold and Black, and unread text stays heavier. A single variable file cannot serve both
weights on Android: Compose caches asset fonts by path, so every weight would draw from whichever
variation loaded first. The variable file it replaces defaulted to Light 300. It also bundles all Material Symbols
used by Mail, accounts, and settings with their Apache 2.0 license. The generated public `Res`
class lives in `com.otusfoss.core.designsystem.resources`. Apps select icons from this bundle and pass
them to `Symbol` as `DrawableResource` values.

## Brand

`designsystem/brand/` keeps the source SVGs: the Otus logo, the owl on its own, the Mail app icon,
and Mail's launch animation in a play-once and a looping version. `tools/brand_drawables.py`
converts them into drawables. Change the SVG, or an `AccentPreset` color, and run it again.

- `brand_otus_logo` and `brand_owl` are Compose drawables of the logo and the owl. Nothing uses
  them yet. `symbol_owl` is a one-color owl for symbol slots, such as the App icon setting.
- The launch animation's layers each hold one color, so `MailLaunch` tints them from the theme.
  They share a 128 unit canvas so they line up when stacked.
- `otus_mail_icon_<name>` are Mail's launcher icons in `androidMain/res`, which merge into the
  app: one per `AccentPreset`, such as `otus_mail_icon_otus_blue`, and one per other
  `AppIconStyle`, such as `monochrome` or `rainbow`. Each is adaptive from Android 8 on, with the
  owl and envelope scaled into the 66 dp safe circle, and the full square icon before that. A
  launcher icon cannot follow the theme, so all but Chrome sit on `otus_brand_background`,
  the Otus theme's `bgDark`. A host test checks every preset and style has one.
- The pride icons paint the owl and envelope in a flag's stripes. `otus_mail_icon_chrome` is the
  owl as polished metal on a white tile, with its foreground as WebP at each density, because a
  vector cannot shade metal. `tools/brand_drawables.py` does not make these five; they are
  committed as they are.
- `brand_mail_icon_<name>` are Compose copies of the launcher icons' foregrounds. `MailAppIcon`
  draws one in a rounded square, as the App icon setting's grid shows them.

`AppIconStyle` picks the icon. `Accent` colors the owl `accentRegular`, and the pride icons keep it
for the launch animation. `Monochrome` and `Chrome` use `text1`. `AccentPreset.closest` picks the
icon for a custom accent.

`MailLaunch` plays the launch animation in the current theme: the owl and envelope in the style's
color, and the background and dark details in `bgDark`. It starts with the play-once version.
While the app reports `loading`, it continues with the loop, starting from the loop's settled
pose, which matches where the intro ends. As soon as loading has finished, even mid-intro, or on a
tap, the mark shrinks to `BrandTokens.LAUNCH_EXIT_SCALE` and the whole screen fades out at
`Relaxed` speed on the `Exit` curve. `MailLaunchTimeline` holds both runs' keyframes as fractions
of `Motion.LAUNCH_MILLIS` (3 s) and `Motion.LAUNCH_LOOP_MILLIS` (4.2 s), and `KeyframeTrack` eases
between them like a CSS keyframes rule. The tilt and the blink use `Standard` where the SVGs use
ease-in-out and linear timing. `LocalMotionScale` scales all of it.

## Behavior guides

These guides still live in Mail, which was the first app to use the components:
[context panel](https://github.com/otus-foss/otus-mail/blob/main/docs/context-panel.md),
[selection sheets](https://github.com/otus-foss/otus-mail/blob/main/docs/selection-sheets.md),
[sidebar](https://github.com/otus-foss/otus-mail/blob/main/docs/sidebar.md),
[motion](https://github.com/otus-foss/otus-mail/blob/main/docs/motion.md), and
[color theme](https://github.com/otus-foss/otus-mail/blob/main/docs/design.md).

## Date chooser

Version 0.4.0's `SpecificDatePicker` and `MonthYearPicker` components share one bottom sheet with
centered, snapping wheels, fading adjacent options, a circular close control, and a full-width
Go button. They use the supplied travel-date screenshot for layout and the existing Otus palette.
Both take an initial `YearMonth`, an optional initial day, an inclusive `allowedDates` range, and
dismiss/confirm callbacks. Future dates are allowed when the caller's range includes them.
Mail's calendar supplies January 1970 through today.

`SpecificDatePicker` always needs a day. It starts with Pick, disables Go until the user chooses
a day, and confirms with a `LocalDate`. An explicit initial day is also accepted and clamped to
the bounds. `MonthYearPicker` makes the day optional. It starts with Any and confirms with the
chosen `YearMonth` and a `LocalDate?` that is null when Any is selected. Changing month or year
preserves an unselected day. Selected days clamp to the valid month and both date bounds.
Closing, outside-tapping, or Back discards chooser edits.

Mail's calendar uses `MonthYearPicker`. Confirmation without a day navigates to the month and
preserves its current range; an explicit day retains start/end selection behavior.
The continuous calendar layout and gestures remain unchanged.

## Tests

Common tests cover the calendar week model, the decoded-avatar cache, motion, reorder, drag to
select, the HTML scanner, theme choices, and sheet animation. Android host tests cover image
decoding, the panels, pickers, press feedback, and the screenshots below. Mail's tests no longer
test design-system parts; a test that uses no Mail code belongs here.

### Screenshot tests

Each element and component has Roborazzi screenshot tests in `src/androidHostTest`. A test
renders the composable with Robolectric's native graphics at 360 × 640 dp and xhdpi, and
compares the image with a PNG in `src/androidHostTest/screenshots/`. Any changed pixel fails the
test, and Roborazzi writes the comparison image to `build/outputs/roborazzi/`.

Write a test with the `screenshot` helper in `screenshot/Screenshot.kt`:

```kotlin
@Test
fun selected() = compose.screenshot("chip_selected") { Chip("Work", {}, selected = true) }
```

The helper draws the content on `bgDark` in `OtusTheme`; pass a palette to draw it in another
theme. Use fixed dates and demo data, so an image changes only when the drawing does.

After an intended visual change, record the new images, look at each changed PNG, and commit
them with the change:

```sh
./gradlew :designsystem:testAndroidHostTest -Proborazzi.record
```

## Central UI ownership

Meta owns every branded element, reusable component, visual token, and symbol resource. These
APIs take values and callbacks; Mail domain models and workflow decisions stay in Mail adapters.
Import symbols through the public `com.otusfoss.core.designsystem.resources.Res` bundle. Mail, settings,
and accounts use the same symbol files. See [decision 0008](decisions/0008-central-ui-ownership.md).
