# 0008: Central UI ownership

## Decision

The user requested Meta as the central home for all visual elements. All branded elements,
reusable components, visual tokens, and symbol resources now belong in `designsystem` in Meta,
even when only Mail currently uses them. Mail consumes those APIs through its pinned libraries.
Shared feature screens remain in their feature modules. Mail keeps its feature screens and
adapters, which choose text, transform domain data, and connect callbacks to app behavior.
Calendar adoption remains deferred.

## Implementation

Version 0.3.0 adds `ContentRow`, `DetailRow`, and `EmptyState`, moves
`InboxTokens` into Meta, and exports one public design-system `Res` bundle. Message rows,
folder rows, rule rows, and inbox empty states in Mail now map feature data into these shared
renderers. All Mail symbols and duplicate settings/account symbols use the central bundle.
Protocol behavior, credentials, persistence, and navigation stay in their existing layers.

Mail's architecture checker rejects production code in its local `designsystem` namespace and
local drawable resources. Regression tests cover rejection and allowed feature adapters.
Meta's checker also rejects drawable bundles outside design-system modules and still prevents
app dependencies and services in visual components. Regression tests cover central resources.
This strengthens ownership without changing the elements → components → screens hierarchy.
The library extraction is included in version 0.3.0.

## Consequences

A shared visual change is made in Meta and reaches apps through a library release. Feature
compositions can differ through values, slots, and callbacks without duplicating drawing code.
App-specific workflows remain with their feature. New reusable drawing code goes into Meta;
whole feature screens do not become design-system components.
