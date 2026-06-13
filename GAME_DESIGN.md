# JOURNEY — GAME DESIGN & MECHANICS SCHEMATIC

> **AI INSTRUCTION:** This is the operational matrix for game loops. Maintain absolute compliance with the numerical weight categories, resource calculations, and visual token scales defined below.

---

## 1. COMPACT STATE DATA PROPERTIES

The runtime loop tracks precisely four primary numeric status metrics and two alignment indicators to calculate player state transitions:

### 1.1 Structural State Tracking
* `hp` -> Primary durability boundaries. Reaching a value of zero instantly initializes the Spirit Fracture resolution loop.
* `gold` -> Universal commerce token consumed at camp zones and merchant nodes for transactions.
* `aether` -> Pure cosmic structural energy acquired exclusively through narrative option completions.
* `will` -> Navigational capacity depleted sequentially by stepping floors or execution of scouting routines.

### 1.2 Multi-Tier Alignment Parameters
* `momentum` [Visible] -> Active alignment vector locked strictly between `0` and `100`. Midpoint neutrality is exactly `50`. Scale endpoint `100` indicates Lawful/Sanctum alignment, scale endpoint `0` indicates Chaotic/Covenant alignment.
* `corruption` [Hidden] -> Under-the-hood accumulator processing absolute malicious or defiling choices. High accumulation unlocks access to hidden Absolute Evil options.

---

## 2. CHOICE IMPACT MATRIX & SCALING WEIGHTS

Every node option array parsed from asset files must map directly to one of the following explicit behavioral weight categories:

* `TRIVIAL` (Value Weight: 1) -> Atmospheric conversational lines. Yields zero resource alterations or long-term impacts.
* `MINOR` (Value Weight: 3) -> Casual momentum adjustments on the active visible timeline.
* `MODERATE` (Value Weight: 7) -> Direct transaction triggers, currency shifts, or flag changes.
* `MAJOR` (Value Weight: 15) -> Structural story branching changes, conditional path locks, or unique title acquisitions.
* `HEAVY` (Value Weight: 35) -> Absolute irreversible commitments (e.g., accepting a high-corruption Devourer alliance covenant option).

---

## 3. DARK FANTASY DESIGN TOKEN STANDARDS

Enforce the following strict semantic color codes and type scales across all stateless interface components:

### 3.1 Color Token Semantics
* **Sanctum / Lawful Vectors:** Gold Accent -> Hex Token `#C8A94A`
* **Covenant / Chaotic Vectors:** Purple Accent -> Hex Token `#7B2FBE`
* **The Devourer / Evil Mechanics:** Deep Crimson -> Hex Token `#9C0000`
* **Base Layout Background:** Obsidian Black -> Hex Token `#0A0A0F`
* **Structural Borders:** Weathered Iron -> Hex Token `#2A2A3A`

### 3.2 Typography Standards
* **Epic Title Headers:** Cinzel Decorative font family (Classic gotik structure).
* **Lore Descriptions:** Crimson Pro font family (High legibility serif layout).
* **System UI Labels:** Rajdhani font family (Clean modern uppercase style).