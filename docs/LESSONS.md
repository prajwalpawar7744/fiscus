# Lessons learned

Mistakes made while building Fiscus, and the rule each one produced. **Read this file, together with every other doc, before planning or implementing anything**, including scaffolding, placeholders and "small" changes. Newest at the bottom.

## 2026-11-15: Add/edit/delete transaction (M1)

1. **Building from one doc instead of all of them.**
   - What happened: the first version was built after reading only DESIGN_SYSTEM.md. It used valid M3 components but had no motion, no hero treatment and no "satisfying save", so it didn't look like M3 Expressive at all, even though this flow is hero moment 2 (ADR-010).
   - Rule: before planning, read **all** of CLAUDE.md and `docs/*.md` in full (not grep). Before building each screen, run the M3_EXPRESSIVE.md §9 checklist and write down how each item is met.

2. **Ignoring the documented structure.**
   - What happened: shared composables went into `ui/component/`, but ARCHITECTURE.md says `ui/components/`. Pushed screens used a plain `TopAppBar`, but the feature pattern says `LargeFlexibleTopAppBar`. The detail ViewModel collected into a `MutableStateFlow`, but stream-based state must use `stateIn(WhileSubscribed(5_000))`.
   - Rule: copy the documented pattern (package, file set, top bar, state holder) exactly. If a doc and the existing code disagree, ask.

3. **Writing our own motion.**
   - What happened: screen transitions were first hand-written on `motionScheme` springs, then nearly rewritten by copying material3's `internal` easing/duration tokens into an app `Motion.kt`. Both are our own specs, which the guidelines forbid. The M3 transition-patterns page says transitions use the legacy easing system, which the library doesn't expose.
   - Rule: never write design tokens or transition specs, and never copy library-internal values. Use the public API (`MaterialTheme.motionScheme`, component defaults) or the library's default behavior (Navigation 3's `NavDisplay` transitions). If neither fits, **ask the user before writing anything**.

4. **Scale in enter/exit.**
   - What happened: the Save button swapped its content with `scaleIn`/`scaleOut`.
   - Rule: M3 enter/exit patterns avoid scale (it implies an elevation change). Use expand/collapse along an axis or a fade.

5. **Unread sources treated as "close enough".**
   - What happened: a shared-axis transition was implemented while the M3 transitions page was still marked unread. Its content later contradicted the implementation.
   - Rule: if a source a decision depends on can't be read, say so and ask the user for it (they can paste it) **before** implementing. Don't fill the gap from memory.

6. **Deprecations hidden in code that was already there.**
   - What happened: `currentWindowAdaptiveInfo()` was deprecated, but it only surfaced during a full rebuild.
   - Rule: after any change, run a full `assembleDebug` and treat every `w:` line, including in untouched files, as a failure to fix (ADR-013).

7. **Driving the user's real phone with blind taps.**
   - What happened: scripted `adb input tap` calls on a personal device left the app and opened an unrelated image viewer.
   - Rule: on a personal device, only run instrumented tests (they run in their own activity) and read-only commands like screenshots. Never script taps there. Ask the user to try flows by hand, or use an emulator. If the device is locked, stop and ask.

8. **Test harness versus real behavior.**
   - What happened: with the soft keyboard open in a plain `ComponentActivity` test host, taps below the focused field didn't register. `performScrollTo` also doesn't scroll an outer `verticalScroll` when the target sits inside a nested `LazyRow`.
   - Rule: when a UI test fails, find out whether the cause is the harness or the app before changing either, and report which.

9. **A valid M3 component isn't automatically Expressive.**
   - What happened: account and category used `FilterChip`s. They're correct M3, but chips aren't in the Expressive component set, so the hero flow looked like plain M3.
   - Rule: in hero moments, choose from the Expressive set first (button groups, toggle buttons, FAB menu, loading indicator, split button, toolbars, flexible app bars). Use a non-Expressive component only when nothing in the set fits, and say why.

10. **Swapping a component for its motion without showing the user first.**
   - What happened: to add press motion, the connected type selector was swapped for a standard `ButtonGroup` (pressed item widens, neighbours squeeze). The user rejected the result ("thats shit, revert it").
   - Rule: a visible change of component or style in a hero flow is the user's call. Describe it (or show a preview or screenshot) and get a yes before building it. The connected selector stays.

11. **Reusable pieces written as private.**
   - What happened: the docked date picker, the toggle-button option group and the currency transformation were written as `private` functions inside the editor screen. Any other feature needing a date picker would have had to copy them.
   - Rule: when writing a composable, ask "could another screen use this?". If yes, put it in `ui/components/` immediately (generic: no feature types and no feature strings), and list it in DESIGN_SYSTEM.md "Shared components". Before building, check that list first.

12. **Acting on derived state that can lag input.**
   - What happened: after moving the editor to `stateIn`, Save read `uiState.value`, which hadn't yet re-emitted the last keystroke, so a fast "type then Save" would have saved the old amount. A test caught it.
   - Rule: actions read the source of truth (the form), not the derived UI state. Keep a test that types and saves with no idle time in between.

13. **A colour that passes in one scheme can fail in another.**
   - What happened: the default placeholder colour (`onSurfaceVariant`) looked like the fix for the hero placeholder, but measured 1.8–2.3:1 on the coloured hero cards at medium and high contrast.
   - Rule: whenever text sits on a container it isn't paired with, measure contrast in all six schemes before shipping, and report the numbers.

14. **Repeating constants instead of sharing them.**
   - What happened: every screen file declared its own `ContentPadding = 16.dp`.
   - Rule: layout spacing comes from `FiscusLayout`; anything used twice is shared.

## Process checklist (every task, including scaffolding)
1. Read CLAUDE.md and every file in `docs/`, including this one.
2. Plan: list the docs and ADRs that apply, the §9 checklist answers per screen, and any source still marked unread (ask for it).
3. Check every API's name, status, deprecation and built-in motion in the real library (sources jar or `javap`) and record it in REFERENCES.md and DESIGN_SYSTEM.md.
4. Build only from public M3 and Jetpack APIs. Anything else needs the user's approval and an ADR. Check DESIGN_SYSTEM.md "Shared components" first; put anything reusable in `ui/components/`.
5. Verify: a full build with zero warnings, unit tests, lint, and device tests when possible. Report honestly what wasn't verified.
6. Record what you learned: REFERENCES.md (facts), DECISIONS.md (choices), LESSONS.md (mistakes).
