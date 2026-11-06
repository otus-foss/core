# 0014: User themes and accent colors

## Decision

The user asked for a theme page in Appearance & behavior with four themes and a free accent
color that changes the whole app. This replaces the rule that dark mode is the only color mode
and that apps offer no theme switch. The supplied palette stays the default: the Otus theme with
Otus blue gives exactly the original values.

The themes are **Otus** (the original soft dark), **Light**, **Graphite** (a warm grey dark after
Notion), and **Black** (true black for OLED, after the X mobile app). The update below adds Ink
and removes Graphite. The user picks one accent,
from seven presets or by hue, intensity, or hex code. Otus derives the accent's fill
(`accentDark`), outline (`accentRegular`), and text (`accentLight`) shades for the chosen theme
and moves the fill and text shades until text is readable at 4.5:1 or more.

The device's light or dark setting still does not change the theme, and there are no dynamic
device colors.

## Implementation

`ThemeBase` holds each theme's surfaces and text colors, `AccentPreset` the presets, and
`ThemeChoice` a theme and accent whose `palette` is an `OtusPalette`. `accentShades` derives the
three accent shades. `OtusColors` keeps its named tokens; each one now reads the current palette
from snapshot state, which `OtusTheme(palette)` sets, so reading a token in composition or
drawing follows a theme change. Tokens must stay getters: a value copied from `OtusColors` into
a stored property keeps the old theme.

The `settings` module adds `ThemeSettingsState` with an app-supplied `ThemePreferenceStore`, the
`ThemeSettingsScreen` page, and a Look section with a Theme row on `AppearanceSettingsScreen`.
Common tests cover the shade rules and the saved choice.

## Consequences

Components must take colors from `OtusColors` at the time they draw and never cache them outside
composition. Colors that sit on accents or images, such as a swatch's check, are theme tokens of
their own. Scrims use `OtusColors.scrim`, which is dark in every theme.

## Update: Ink and custom colors

The user asked for a fifth theme and for every color to be editable, not only the accent.

The user asked to remove **Graphite**. A saved choice of Graphite now opens as Otus.

**Ink** sits between Otus and Light. It is darker than Otus, and its page, raised surfaces,
controls, and borders sit closer together, so screens show less contrast between surfaces. Its
muted text still reads at 4.5:1 on the page.

Each theme's ten colors (`ThemeColor`: page, raised surfaces, controls, borders, three text colors,
and the three accent shades) can be changed one by one under Colors on the theme page. The
built-in themes never change. The first change on a built-in theme creates a custom theme
(`CustomTheme`) made from it and selects it; it shows as "Custom", "Custom 2", and so on, after the
built-in cards. Later changes edit the selected custom theme. A custom theme stays, even with
every color put back, until the user deletes it.

A long press on a theme card, or the more button on the selected card, opens the theme's actions
in a small menu at the press point that leaves the page undimmed.
Every theme can be duplicated into a new custom theme, such as "Ink copy". A custom theme can also
be renamed or deleted; deleting the theme in use switches to the theme it was made from. Names are
unique, ignoring case, and at most 30 characters. "Reset to Otus" selects Otus and Otus blue and keeps custom themes.

The derived accent shades follow a changed page, raised surface, or text color. Picking a new
accent drops the accent shades set by hand in the selected custom theme. The saved preference
adds the selected custom theme, each custom theme and its source theme, its name with spaces
escaped, and each changed color after the theme and accent, such as
`light #2383E2 custom=1 1:ink 1:name=Night%20ink 1:page=#101011`. Older saved
values still read.

## Update: System theme

The user asked for Material You on Android, so the app can match the system. This replaces the
rule above that Otus has no dynamic device colors and ignores the device's light or dark setting,
for this one theme only.

A **System** card follows the built-in themes on Android 12 and newer. It takes every surface and
text color and the accent from the device's Material You colors, and follows the device's light or
dark mode. Android recreates the activity when the wallpaper colors or the mode change, and the app
reads them again. While System is selected, the theme page hides the accent swatches and Colors,
since the device sets them; System has no Duplicate action. iOS and older Android show no System
card, and a saved System choice falls back to the stored built-in theme there.

`SystemTheme` holds the device's colors, which `ThemeChoice.system` carries but never stores; the
saved preference adds a `system` part while it is selected. In Android source, `Context.systemTheme()`
maps the Material You tonal palettes onto the theme colors, with the dark raised surface below the
page and the light one above it, as in the built-in themes.
