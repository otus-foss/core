# 0014: User themes and accent colors

## Decision

The user asked for a theme page in Appearance & behavior with four themes and a free accent
color that changes the whole app. This replaces the rule that dark mode is the only color mode
and that apps offer no theme switch. The supplied palette stays the default: the Otus theme with
Otus blue gives exactly the original values.

The themes are **Otus** (the original soft dark), **Light**, **Graphite** (a warm grey dark after
Notion), and **Black** (true black for OLED, after the X mobile app). The user picks one accent,
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
