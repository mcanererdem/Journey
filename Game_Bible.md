# LIGHT & DARKNESS — GAME BIBLE (KULE ANAYASASI)

> **AI INSTRUCTION:** This document is the ultimate Source of Truth (SoT) for game systems, philosophy, and alignment tracking. Adhere strictly to the definitions, math constraints, and mechanics defined below. Do not deviate.

---

## 1. CORE PHILOSOPHY & MATHEMATICAL CONSTRAINTS

### 1.1 The Spire's True Purpose (Will Harvesting)
* **The Spire is not a battleground for good versus evil.** It is an ancient techno-magical **Will Harvesting Device**.
* The Will harvested from climbers is utilized as fuel to maintain the seal trapping the ultimate external threat: **The Devourer**.
* Traveling, climbing, scouting, and narrative events consume **Will (⚡)**. Every unit of Will spent is absorbed by the Spire to prolong the world's survival.

### 1.2 Two-Tiered Alignment Engine
Climber decisions feed into two distinct structural parameters within `PlayerProfile`:

#### Parameter A: Momentum (`momentum: Int`) [Visible]
* **Range Constraints:** Strict range of `0` to `100`. **Neutral midpoint is exactly 50.**
* **Clamping Rule:** Any arithmetic change to momentum MUST be clamped immediately using `.coerceIn(0, 100)`. Negative numbers or values outside this bounds are prohibited.
* **Vectoring:** Shifting towards 100 represents **Lawful/Sanctum (Light)** felsefesi. Shifting towards 0 represents **Chaotic/Covenant (Dark)** felsefesi.

#### Parameter B: Corruption (`corruption: Int`) [Hidden/Internal]
* **Functionality:** Tracks absolute malicious, self-serving, or defiling choices (The Evil Path).
* **Visibility:** Completely hidden from the player UI. It operates underneath the active Momentum vector.
* **The Absolute Evil Path (Devourer's Pact):** When `corruption` crosses a predefined critical high threshold and specific preconditions (hidden story flags or items) are met, the engine unlocks hidden choices to serve **Mutlak Kötülük / Heavenly Demon**.

---

## 2. SPIRE GEOGRAPHY & LIFECYCLE (FLOOR 0 - 100)

### 2.1 Structural Topology
* **Linear Ascent:** Players climb upwards from Floor 0 towards Floor 100. Descent is locked during active progression loops.
* **Shared Convergence Hubs:** At Floors **0, 25, 50, 75, and 100**, the structural branching paths merge into unified narrative zones.
* **Shortcuts:** Special exploration nodes can trigger shortcuts that bypass multiple floors sequentially based on title or key validation.

### 2.2 Endgame Bifurcation (Post-Floor 100)
Upon completing Floor 100, the climber learns the absolute truth of the Spire and faces an irreversible meta-progression choice:
1. **The Outer Realm Campaign:** Exit the Spire to engage in active military campaigns, skirmishes, and exploration across the Grey Kingdoms and neutral zones.
2. **Spire Governance:** Remain within the Spire structure to permanently participate in the tactical asymmetric political system.

---

## 3. ASYMMETRIC GOVERNANCE SYSTEM
Strategic milestones (Every 10th, 25th, and 33rd floor) contain political management matrix slots:

### 3.1 Tier Classification & Rotation Matrix
* **Normal Floors (1-9, 11-19...):** Managed by standard candidates. Rotation occurs on a strict **7-day (Weekly)** timer.
* **Special Milestone Floors (10, 20, 30...):** Higher security tiers. Rotation occurs on a **14-day (Bi-weekly)** timer.
* **Greater Faction Milestones (25, 50, 75):** Frontline campaign orchestrators managing faction wars (Skirmishes/Wars). Selected monthly from qualified tier governors.
* **Ultra Apex Floors (0 and 100):** Controlled by a single sovereign (Lord/Baron) representing the absolute apex title holder. Selected seasonally on a **3-month (90 days)** lifecycle.

### 3.2 Governance Privileges & Betrayal Mechanics
* Active governors receive unique persistent/seasonal political Titles.
* Governors have the power to project floor-wide operational parameters (buffs/debuffs) and emit automated objective quotas (e.g., target tracking quests, speed-climbing milestones) to normal players on that floor.
* **Betrayal Protocol:** Governors can willingly execute a paradigm shift to the opposing faction, stripping old faction benefits but instantly generating the permanent un-deletable title: **"Betrayer"**.