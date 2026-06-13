# CLAUDE.md — LIGHT & DARKNESS (JOURNEY) RUNTIME EXECUTION LAWS

> **AI INSTRUCTION:** You must treat the rules, boundaries, and mathematical constraints defined below as absolute laws. Do not generate arbitrary boilerplate code or mix deprecated legacy mechanics during tasks.

---

## 1. RUNTIME CONFIGURATION & MATH BOUNDARIES

### 1.1 State Clamping & Hidden Logic
* **Momentum Engine boundaries:** Strictly locked within a safe scale of `0` to `100`. The structural neutral midpoint is exactly `50`.
* **The Clamping Law:** Every single transformation, arithmetic addition, or subtraction involving momentum MUST use `.coerceIn(0, 100)`. Symmetrical scales like `-100..100` are banned and constitute application-breaking bugs.
* **Hidden Alignment Engine:** Track player malicious or absolute evil choices using an internal variable named `corruption: Int` inside the profile layer. This operates under the hood to handle dynamic branching in text data arrays.

### 1.2 Resource Framework
* The engine must process precisely two economic/power resources: `gold: Int` for universal commerce and `aether: Int` for cosmic energy.
* **Deprecation Mandate:** Legacy parameters like `Gleam` and `Pyre` are fully deprecated. Permanently purge them from any data layer, viewmodel, or JSON asset file you handle.

### 1.3 Localisation & Structural i18n
* **Zero Hardcoded Strings Policy:** No Kotlin source file, Room entity table, or JSON floor blueprint may carry raw user-facing narrative lines.
* Data objects are strictly limited to tracking alphanumeric semantic localization pointer tags (e.g., `titleKey`, `descriptionKey`, `actionKey`).
* The UI presentation layers must resolve strings exclusively using the decoupled `LocalizationManager.getString(lang, key)` routing pipeline.

---

## 2. MODULAR ARCHITECTURE & UI RULES
* **File Sizing Cap:** No Kotlin code file may exceed a hard limit of `500` lines under any condition.
* **Token Sizing Policy:** Inline hex definitions like `Color(0xFF...)` and raw layout dimensions like `dp`/`sp` are forbidden. Force compilation through the design tokens located inside the package `ui/theme/`.
* **Layout Stability:** Avoid UI jumping when content changes. Use `weight(1f)` for flexible areas and pin narrative/actions to predictable positions (e.g., above bottom navbar).
* **Information Density:** Maintain high information density in game screens. Use tight spacing (`Dimens.SpacingXs`/`Dimens.SpacingS`) and avoid excessive empty areas.
* **Thematic Consistency:** Use `ColorStatGold` for Will/Essence. Use `ColorSanctumPrimary` for Sanctum (Light) and `ColorCovenantGlow` for Covenant (Dark) elements.

---

## 3. ACTIVE SPRINT TRACKING (WHERE WE ARE)
* **Phase Target:** Phase 0 — Data Architecture & Schema Alignment.
* **Current Status:** UI Refinement for Header and Tower screens completed.
* **Next Objective:** Execute Task P0.1 (Domain Model Refactoring) and Task P0.2 (Room Entity Cleanup) as defined in `Dev Plan.md`.
