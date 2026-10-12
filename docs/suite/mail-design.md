# Mail design foundations

## Design source

[Figma: meta — supplied variables view](https://www.figma.com/design/7HWGPYYexnCqvkEYdEKIgq/meta?node-id=0-1&view=variables&p=f&t=Wf1hH9mBsgCc919a-0)

- File key: `7HWGPYYexnCqvkEYdEKIgq`
- Linked node: `0:1`
- Inspection status: Figma MCP access verified. Inbox (`19:386`) and search (`19:322`) panels inspected through metadata, design context, and screenshots. File-wide variables and other frames are not fully audited.

The palette below was supplied directly by the user. Preserve these values until a later explicit design decision changes them. Do not infer additional Figma modes, components, typography, or spacing from this list. The user has now requested this palette for both apps in dark mode only.

## Mail palette

All values are opaque sRGB hex colors.

| Token | Hex |
| --- | --- |
| `accent-light` | `#618BB6` |
| `accent-regular` | `#4D6F92` |
| `accent-dark` | `#39526B` |
| `bg-light` | `#3F4041` |
| `bg-regular` | `#202021` |
| `bg-dark` | `#151515` |
| `text-1` | `#EBECED` |
| `text-2` | `#C2C2C2` |
| `text-3` | `#8A8A8A` |
| `stroke-bg-dark` | `#27292C` |

## Implementation guidance

Define the palette centrally as named Compose theme tokens. Use semantic component roles when mapping these values into UI. The light/regular/dark suffixes describe palette variants; they do not establish separate light and dark application themes.

Exact component assignments, interaction states, and foreground/background pairings still need design verification. In particular, do not assume every text/accent pairing has sufficient contrast merely because both colors are supplied.

Both apps now use this palette in a dark-only Compose theme. Default text is text-1 (#EBECED) at full opacity; background and default surfaces use bg-regular (#202021). Secondary text colors are opt-in. System appearance does not select a different theme.

## Context panel decisions

The Mail start page is now the empty Inbox, with a search configuration. The earlier Inbox/Search/Metadata
playground remains a component test fixture. Small controls are design-system elements; the panel is a reusable
composition with values/callbacks and content slots. It has detached and overlapping chip layouts.

Material Symbols Rounded is the chosen icon family, explicitly replacing the Figma icons.
Figtree is bundled for panel text. Compact pills retain their visual size inside minimum 48 dp
interactive targets, which adds vertical space relative to the supplied design.

The inbox fade in Figma contains `#353637`, outside the supplied palette. Implementation uses a
transparent alpha fade instead. The palette above remains unchanged. See
[Mail context-panel documentation](https://github.com/reazndev/otus-mail/blob/main/docs/context-panel.md) for API and validation details.

## Inbox page

Figma nodes `23:488` (front page) and `10:58` (separate message row) were inspected for the inbox
implementation. The empty state is new UI; no populated inbox was present in that front-page frame.
The message-row avatar's black fill and label border `#2A5E96` are outside the approved palette.
`LabelPill` draws that label with palette tokens instead: an `accent-regular` border and
`accent-light` text when highlighted, otherwise a `bg-light` border and `text-2` text.
Generated avatars now choose `accentDark`, `bgLight`, or `bgDark` by email address, with `text1`
initials. Contact photos fill the same circle. These avatar variants are app decisions, not newly
verified Figma values. Metadata continues to use named text tokens. No palette values change.
See [Mail inbox documentation](https://github.com/reazndev/otus-mail/blob/main/docs/inbox.md) for implemented behavior and deferred work.

## Reusable pill sizing

Use these agreed defaults for compact filter and action pills throughout the app:

| Property | Default |
| --- | --- |
| Visual height | 36 dp |
| Leading icon size | 20 dp |
| Horizontal padding | 10 dp |
| Text size | 16 sp |
| Minimum touch target | 48 dp |

These defaults are implemented in Mail. Preserve font scaling and allow content to grow when
needed for accessibility. Use Material Symbols Rounded for leading icons; Inbox uses unread-mail
and attachment symbols.

Token ownership decision: pill-specific dimensions should belong to `ChipTokens`, so reusable
pills can use them independently of the context panel. Panel spacing and overlap settings belong
to `PanelTokens`. The sizing currently lives in `PanelTokens`; extracting `ChipTokens` is planned,
not yet implemented.

The detached panel adds no extra gap above the pill row. At normal text scale, the 36 dp pill
centered in its 48 dp touch target leaves a 6 dp visual gap below the header. This spacing remains
a panel layout decision rather than a pill token.

## Selection sheets

Inbox filters use the same bottom sheet as settings choices: it slides up from the bottom
edge with 22 dp top corners, a bg-dark surface, a grab handle, Figtree text, and the shared
clipped press highlight. Picker search reuses the SearchField element but keeps its query
independent of mail search. Clear edits only the sheet draft; Done commits, while the scrim,
Back, or a downward swipe discard changes.

Figma search frame `23:563` was inspected with design context and screenshot. Its blank popup
surface `24:655` informs the styling; it does not provide complete picker layouts. The sheet
content and date calendar originated in the app and were subsequently recreated as editable
Figma components (review `39:2`). The approved joined month navigation (`40:14`) is now
implemented in Mail: one outlined capsule, a center month/year pill, and separate arrow targets. See
[Mail selection sheets](https://github.com/reazndev/otus-mail/blob/main/docs/selection-sheets.md) for behavior and validation.

## Action bar

The spec times the primary's collapse into a context at 220 ms. On the device this felt slow, so
at the user's request it runs at `MotionSpeed.Fast`, 160 ms. The context still fades in after 40%
of the width animation.

The supplied action bar spec arrived without its first section, which held the dimensions and
API. Only two sizes come from its text: the 62 dp bar height and the 60 dp collapsed primary
action. The other sizes in `ActionBarTokens` were measured from the spec's U2 image. They are app
decisions, not verified Figma values, and no Figma frame for the bar has been inspected.

| Property | Value |
| --- | --- |
| Gap between group and shell | 8 dp |
| Corner radius | 22 dp, the panel radius |
| Group horizontal padding | 7 dp |
| Gap between secondary slots | 2 dp |
| Symbol size | 24 dp |
| Label inset, without context | 20 dp |
| Context inset | 12 dp |
| Token size | 36 dp |
| Gap after the token | 10 dp |
| Avatar and count overlap | 12 dp |
| Ring around the count | 2 dp |
| Date token radius | 8 dp |
| Title text | 18 sp |
| Subtitle text | 14 sp |

The group and shell use `bg-dark` with a `stroke-bg-dark` border. The primary action is
`accent-dark` with an `accent-regular` border, and a disabled one is `bg-dark` with a `stroke-bg-dark` border. Selected list rows
use an `accent-dark` tint. The palette is unchanged.

## Motion

Every animation uses Meta's `Motion` API: three cubic Bézier curves in `MotionCurve`, four
speeds in `MotionSpeed`, and a global speed multiplier in `LocalMotionScale`. The curves are
standard (0.4, 0, 0.2, 1), enter (0.2, 0, 0, 1), and exit (0.4, 0, 1, 1). The curves and speeds
are app motion decisions, not verified Figma motion values. The
[design system guide](../designsystem.md#motion) lists them and shows how to use them.

Rules for both apps:

- Animate with `Motion.tween(speed, curve)` or `motionTween(speed, curve)`, or use
  `Motion.millis(speed)` for a spec that is not a tween. Every animation respects
  `LocalMotionScale`.
- Never add a raw duration constant, a `tween` with a literal duration, a new
  `CubicBezierEasing`, or an animation that falls back to a Compose default spec.
- Pick the speed by what moves. `Quick` is feedback on touch, `Fast` is a small change in place,
  `Normal` is panels, sheets, and text, and `Relaxed` is a large surface such as the sidebar or a
  settling row.
- Pick the curve by direction. `Standard` is the default, `Enter` is for things that arrive, and
  `Exit` is for things that leave.
- To make everything faster or slower, change `LocalMotionScale`. Do not edit the speeds.

Mail follows these rules. Calendar still has its own motion code and adopts `Motion` when it
moves to Meta's design system.

The reusable `AnimatedText` element slides and fades between labels. Mail uses it for the
date-picker month/year capsule: later months move up, earlier months move down, and year text
changes only across years. Incoming text arrives at `Normal` speed with the enter curve, 240 ms;
outgoing text leaves at `Fast` speed with the exit curve, 160 ms.
See [Mail motion](https://github.com/reazndev/otus-mail/blob/main/docs/motion.md) and [Calendar motion](https://github.com/reazndev/otus-calendar/blob/main/docs/motion.md).
