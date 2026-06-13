# LIGHT & DARKNESS — DEVELOPMENT BLUEPRINT (DEV PLAN)

> **AI INSTRUCTION:** Execute the project lifecycle sequentially based on the technical architecture rules defined here. Do not build arbitrary modules or code blocks outside the active phase task context.

---

## TECHNICAL SYSTEM ARCHITECTURE RULES

### Rule 1: Structural String Decoupling (Zero Hardcoded Text)
* Under no circumstances should Data classes, Room database tables, or JSON blueprints carry direct user-facing narrative text lines (e.g., fields like `textEn`, `titleTr` are strictly forbidden).
* Data elements must exclusively hold alphanumeric localization identifier strings (e.g., `titleKey`, `labelKey`, `actionKey`).
* String resolution must flow through `LocalizationManager.getString(lang, key)` exclusively.

### Rule 2: Unified Financial & Resource Domain
Omit all references to deprecated dual-resource structures (`Gleam`, `Pyre`). The runtime engine must manage only four active parameters:
1. `hp`: Current health boundaries.
2. `gold`: Universal tradable economic currency.
3. `aether`: Mono-channel meta-energy driving specialized actions and mechanics.
4. `will`: Exploration currency consumed by player movement, node transitions, and scouting.

### Rule 3: Database Integrity Strategy
* Perform a **Destructive Database Migration** cycle to version 4.
* Completely strip narrative translation text blocks from the `journal_entries` schema.
* Enforce `fallbackToDestructiveMigration()` in the Room database configuration module to drop and cleanly rebuild local storage during the validation sequence.

---

## PHASE EXECUTION SEQUENCING

### PHASE 0 — DATA SCHEMA STRUCTURING (ACTIVE PHASE)

#### Task P0.1: Domain Model Refactoring (`data/model/FloorDomain.kt`)
Reconstruct the domain layer to reflect modular architecture mapping. Ensure the following data signatures are structured without code implementations:
* **Enums:** Create `NodePath` (LIGHT, DARK, SHARED), `ChoiceWeight` (TRIVIAL, MINOR, MODERATE, MAJOR, HEAVY), `EnemyForm` (NEUTRAL, LIGHT_FORM, DARK_FORM), and `FloorType` (NORMAL, SPECIAL, HUB).
* **Preconditions Block (`NodePrereq`):** Design optional check conditions containing null-safe validation metrics for pathing, level parameters, titles, item requirements, string flags, and the critical hidden threshold metric `minCorruption`.
* **Gated Choice Validation (`ChoicePrereq`):** Construct localized criteria mapping for player state requirements including `minHp`, momentum barriers, and hidden corruption minimum checks.
* **Impact Operations Matrix (`ChoiceEffects`):** Map out state modifications containing numerical values for momentum shifts, internal hidden corruption shifts (`corruptionShift`), item/title rewards, and delayed impact indexing (`consequenceRing`).
* **Core Blueprint Structure (`FloorBlueprint`):** Rebuild the overarching schema to map out separate, immutable `List<FloorNode>` pipelines for `pathLight`, `pathDark`, and `shared`, completely independent of manual text strings.

#### Task P0.2: Room Entity Cleanup (`data/model/GameEntities.kt`)
* Re-architect `PlayerProfile` to contain active variables for `gold`, `aether`, `momentum`, `corruption` (internal integer), and a centralized serialization string component `storyFlags` tracking active collected runes.
* Strip `actionTakenEs` and `actionTakenTr` from `JournalEntry`. Enforce a single semantic identifier parameter named `actionKey`.

---

### PHASE 1 — PARSER RECOGNITION ENGINE (NEXT PHASE)

#### Task P1.1: Strict Unified Parser Pipeline (`FloorBlueprintSystem.kt`)
* Purge the obsolete dual-parser fallback routine from the system layout.
* Deploy a strict single-pass JSON parser that handles recursive parsing of nodes, stock entries, and conditional logic.
* Implement structured error traps ensuring malformed parameters yield clear structural telemetry log events instead of throwing unhandled app failures.