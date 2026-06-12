# PHASE 0 — CLEAN ARCHITECTURE PLAN
> Proje: Light & Darkness (Journey-main)
> Amaç: Uygulama temiz, güvenli ve genişletilebilir hale getirilecek.
> İçerik yazımı bu aşama bitmeden başlamayacak.

---

## MEVCUT SORUNLAR — TAM TESPİT

### S1 — Çift i18n sistemi (KRİTİK)
Uygulama iki farklı yöntemle metin üretiyor:

**Yol A (doğru):** `LocalizationManager.getString(lang, key)` → locale JSON'dan çeker
**Yol B (yanlış):** Kotlin kodunun içine gömülü `textEn`/`textTr` string çiftleri

Yanlış kullanım sayıları:
- `AdventureEngine.kt`: 88 adet hardcoded bilingual string (narrative node metinleri)
- `GameViewModel.kt`: 31 adet `if (lang == "TR")` dalı
- `TowerScreen.kt`: 49 adet `if (activeLang == "TR")` dalı
- `NarrativeEngine.kt`: tüm floor 4-100 senaryo metinleri hardcoded

**Sonuç:** Yeni dil eklenemez. Metin değişirse birden fazla yerde güncelleme gerekir.
Şu an TR/EN değiştirilse bazı metinler değişmiyor.

---

### S2 — Veri modelleri metin taşıyor (KRİTİK)
```kotlin
// Şu an böyle — YANLIŞ
data class AdventureNode(
    val title: String,         // hardcoded EN metin
    val titleTr: String,       // hardcoded TR metin
    val description: String,
    val descriptionTr: String,
    ...
)

data class NodeChoice(
    val textEn: String,
    val textTr: String,
    val journalEn: String,
    val journalTr: String,
    ...
)
```
Modeller metin taşımamalı, **i18n key** taşımalı.

---

### S3 — Dosya boyutları (KRİTİK)
| Dosya | Satır | Sorun |
|---|---|---|
| `GameViewModel.kt` | 2123 | Combat logic + UI logic + i18n + Firebase hepsi burada |
| `TowerScreen.kt` | 2085 | Harita + combat + narrative + merchant + savaş UI tek dosyada |
| `FloorBlueprintSystem.kt` | 714 | JSON parser + procedural generator + model karışık |

---

### S4 — EnemyFaction string matching (ORTA)
```kotlin
// Şu an böyle — YANLIŞ
fun fromName(nameEn: String): EnemyFaction {
    val nameLower = nameEn.lowercase()
    return when {
        nameLower.contains("void") || nameLower.contains("shadow") -> VOID_CORRUPTION
        ...
    }
}
```
İsim değişirse faction yanlış atanır. Enemy catalog JSON'a `faction` field eklenecek.

---

### S5 — Prosedürel içerik kalitesiz (ORTA)
Kat 4-100 arası 3 hardcoded narrative template'ten random seçiyor.
Tema yok, progression yok, bölge mantığı yok.

---

### S6 — Combat sistemi yarım (ORTA)
- "Skills" butonu "3 available" yazıyor ama hardcoded
- Gerçek skill sistemi yok
- `executeCombatTurn()` sadece LIGHT_STRIKE / HEAVY_BLOW / BARRIER biliyor
- Status effect sistemi var ama combat log string matching ile çalışıyor

---

### S7 — Kullanılmayan/uyumsuz state (DÜŞÜK)
- `_lastActionMessageEn` + `_lastActionMessageTr` → key tabanlı olmalı
- `_firebaseSyncState` string ("IDLE"/"SYNCING") → enum olmalı
- `activeThemeSide` combine flow gereksiz karmaşık
- `LegacyScreen`, `OuterWorldScreen` mevcut game flow ile uyumsuz

---

## TEMİZLİK PLANI — AŞAMA SIRASI

---

## AŞAMA 0 ÖNCESİ — KESİNLEŞEN STAT KARARLARI

> Detaylı gerekçe ve kod referansları: `PROJECT_STATUS.md` §2

### Momentum
- Aralık **0–100**, nötr = **50**. Negatif aralık kullanılmaz.
- `GameViewModel.kt` içindeki `handleRpgChoice` ve `selectNodeChoice`
  fonksiyonlarında `coerceIn(-100, 100)` → `coerceIn(0, 100)` olarak
  düzeltilecek. Bu düzeltme bağımsızdır, AŞAMA 0.1 başlamadan da
  uygulanabilir (öncelikli bug-fix).

### Aether (Gleam + Pyre)
- `PlayerProfile.aether: Int` zaten tek/birleşik para birimi — model
  tarafında EK İŞ YOK.
- Locale kalıntıları temizlenecek: `en.json` ve `tr.json`'daki
  `label_gleam` / `label_pyre` key'leri kaldırılıp tek `label_aether`
  key'i eklenecek. `values/strings.xml` ve `values-en/strings.xml`'deki
  aynı kalıntılar da (kullanım doğrulandıktan sonra) kaldırılacak.
- Bu temizlik **AŞAMA 0.2 (locale JSON yapısını genişletme)** ile birlikte
  yapılacak, ayrı aşama açılmayacak.

---


### AŞAMA 0.1 — Veri Modellerini Temizle
**Etkilenen dosyalar:** `AdventureEngine.kt`, yeni `FloorDomain.kt`

```kotlin
// HEDEF: böyle olacak
data class AdventureNode(
    val id: String,              // "floor_1_node_0"
    val type: NodeType,
    val titleKey: String,        // "floor.1.node.0.title"
    val descriptionKey: String,  // "floor.1.node.0.description"
    val depth: Int = 0,
    val column: Int = 0,
    val enemy: EnemyRef? = null, // sadece referans, metin yok
    val choices: List<NodeChoice> = emptyList(),
    val willCost: Int = 0
)

data class NodeChoice(
    val id: String,
    val labelKey: String,        // "floor.1.node.0.choice_a"
    val journalKey: String,      // "floor.1.node.0.choice_a.journal"
    val effects: ChoiceEffects
)

data class ChoiceEffects(
    val hpChange: Int = 0,
    val goldChange: Int = 0,
    val aetherChange: Int = 0,
    val expChange: Int = 0,
    val alignmentShift: Int = 0,
    val willChange: Int = 0,
    val rewardItemId: String = "",
    val rewardTitleId: String = "",
    val requiredFlag: String = "",
    val setsFlag: String = "",
    val skipToBoss: Boolean = false,
    val skipToNextFloor: Boolean = false
)

data class EnemyRef(
    val enemyId: String,     // global_enemies.json'dan lookup
    val isBoss: Boolean = false
)
```

---

### AŞAMA 0.2 — Locale JSON Yapısını Genişlet
**Etkilenen dosyalar:** `en.json`, `tr.json`

Mevcut düz `ui.*` yapısını hiyerarşik hale getir:

```json
{
  "ui": { ... },              // mevcut UI metinleri — korunur
  "floor": {                  // YENİ: floor içerikleri
    "1": {
      "title": "...",
      "description": "...",
      "intro": {
        "title": "...",
        "description": "...",
        "choice_a": { "text": "...", "journal": "..." },
        "choice_b": { "text": "...", "journal": "..." },
        "choice_c": { "text": "...", "journal": "..." }
      },
      "nodes": {
        "0": {
          "title": "...",
          "description": "...",
          "choice_a": { "text": "...", "journal": "..." },
          "choice_b": { "text": "...", "journal": "..." }
        }
      }
    }
  },
  "enemy": {                  // YENİ: düşman isimleri
    "infested_rat": { "name": "..." },
    "rat_king": { "name": "..." }
  },
  "item": {                   // YENİ: item isimleri
    "health_potion": { "name": "...", "description": "..." }
  },
  "combat": {                 // YENİ: combat log metinleri
    "log": {
      "player_strike": "...",
      "enemy_attacks": "...",
      "player_defeated": "..."
    },
    "status": {
      "poisoned": "...",
      "stunned": "...",
      "blessed": "...",
      "shielded": "..."
    }
  }
}
```

**`LocalizationManager`'a eklenecek metot:**
```kotlin
fun getFloorString(lang: String, floor: Int, path: String): String
// Örnek: getFloorString("EN", 1, "nodes.0.title") → "Entrance Bastion"
```

---

### AŞAMA 0.3 — GameViewModel Böl
**Etkilenen dosyalar:** `GameViewModel.kt` → 4 dosyaya bölünür

```
GameViewModel.kt         → sadece state koordinasyonu (~300 satır)
CombatViewModel.kt       → combat logic (executeCombatTurn, statuses, intent)
FloorViewModel.kt        → floor navigation (ascend, node select, scouting)  
ProfileViewModel.kt      → player profile, titles, quests, journal
```

Her ViewModel kendi state'ini tutar, `GameViewModel` bunları koordine eder.

---

### AŞAMA 0.4 — TowerScreen.kt Böl
**Etkilenen dosyalar:** `TowerScreen.kt` → 5 dosyaya bölünür

```
TowerScreen.kt           → sadece ana layout, composable'ları çağırır
CombatSection.kt         → savaş UI (enemy bar, action butonları, combat log)
NarrativeSection.kt      → hikaye node'ları (seçimler, metin gösterimi)
FloorMapSection.kt       → kat haritası, node navigasyonu
NodeDetailSheet.kt       → node detay modal/bottom sheet
```

**TowerScreen içinde kalmaya devam edecek:** hiçbir hardcoded metin, hiçbir `if (lang == "TR")` çağrısı.

---

### AŞAMA 0.5 — AdventureEngine Temizle
**Etkilenen dosyalar:** `AdventureEngine.kt`, `FloorBlueprintSystem.kt`

- Tüm hardcoded bilingual string çiftleri kaldırılır
- Node'lar artık `titleKey`/`descriptionKey` taşır
- Prosedürel generator sadece yapı üretir (key'ler pattern'den türetilir: `floor.{floor}.node.{index}.title`)
- `EnemyStats` → `EnemyRef` olur, metin taşımaz

---

### AŞAMA 0.6 — Enemy Catalog Güçlendir
**Etkilenen dosyalar:** `global_enemies.json`

```json
{
  "infested_rat": {
    "id": "infested_rat",
    "nameKey": "enemy.infested_rat.name",
    "faction": "BLIGHTED",
    "tier": 1,
    "hp": 42,
    "atk": 4,
    "skills": [],
    "floorRange": [1, 3]
  }
}
```

`EnemyFaction.fromName()` string matching → `faction` field'dan okuma.

---

### AŞAMA 0.7 — Combat Sistemi Tamamla
Mevcut yarım combat sistemi tamamlanır:

```kotlin
// Gerçek skill sistemi
data class SkillDef(
    val id: String,
    val nameKey: String,
    val descriptionKey: String,
    val cost: Int,         // aether cost
    val damage: Int,
    val effect: StatusEffect? = null
)

// Combat log artık string değil key taşır
data class CombatLogEntry(
    val key: String,       // "combat.log.player_strike"
    val args: Map<String, Any>  // {"damage": 15, "enemy": "enemy.rat_king.name"}
)
```

---

### AŞAMA 0.8 — Kullanılmayan Kod Temizle
- `LegacyScreen.kt` → mevcut game design ile uyumsuz, kaldırılır
- `OuterWorldScreen.kt` → 100. kat sonrası için saklanır, navigation'dan çıkarılır
- `SecretBossEncounter` → şimdilik kaldırılır, yeniden tasarlanacak
- `watchRewardedAd()`, `purchaseProduct()` → şimdilik stub kalır
- `_lastActionMessageEn` + `_lastActionMessageTr` → tek `_lastActionMessageKey` olur

---

## DOSYA DURUMU TABLOSU (Sonuç Hedefi)

### Silinecek / Boşaltılacak
| Dosya | Aksiyon |
|---|---|
| `AdventureEngine.kt` - hardcoded narrative node'ları | Temizlenir |
| `NarrativeEngine.kt` - hardcoded floor senaryo metinleri | Temizlenir |
| `LegacyScreen.kt` | Kaldırılır |
| `SecretBossCombatView` (TowerScreen içindeki) | Kaldırılır |

### Bölünecek
| Mevcut | Yeni |
|---|---|
| `GameViewModel.kt` (2123 satır) | GameViewModel + CombatViewModel + FloorViewModel + ProfileViewModel |
| `TowerScreen.kt` (2085 satır) | TowerScreen + CombatSection + NarrativeSection + FloorMapSection + NodeDetailSheet |
| `FloorBlueprintSystem.kt` | FloorBlueprintSystem + FloorDomain (data classes) |

### Değiştirilecek
| Dosya | Değişim |
|---|---|
| `AdventureNode` | titleKey/descriptionKey taşır, metin taşımaz |
| `NodeChoice` | labelKey/journalKey taşır |
| `EnemyStats` | `EnemyRef` olur |
| `global_enemies.json` | faction field eklenir |
| `en.json` / `tr.json` | floor/enemy/combat/item bölümleri eklenir |
| `LocalizationManager` | getFloorString() eklenir |

### Korunacak (değişmeden)
| Dosya | Neden |
|---|---|
| `GameDatabase.kt` | Room DB yapısı temiz |
| `GameDao.kt` | Temiz |
| `GameRepository.kt` | Temiz |
| `PlayerProfile` (Room entity) | Korunur |
| `JournalEntry` (Room entity) | Korunur |
| `QuestTitleSystem.kt` | Genişletilecek ama yapı iyi |
| `FirebaseManager.kt` | Dokunulmaz |

---

## UYGULAMA SIRASI (Öneri)

```
Hafta 1:
  0.1 → FloorDomain.kt yaz (yeni data class'lar)
  0.2 → en.json / tr.json yapısını genişlet
  0.3 → LocalizationManager.getFloorString() ekle
  0.5 → AdventureEngine temizle (key-based node üretimi)
  0.6 → global_enemies.json güçlendir

Hafta 2:
  0.4 → TowerScreen.kt böl
  0.3 → GameViewModel böl
  0.7 → Combat sistemi tamamla
  0.8 → Kullanılmayan kod temizle

Hafta 3:
  → Floor 1-3 JSON + locale güncelle (yeni yapıya göre)
  → Test + stabilize
  → İçerik yazımına geç (Aşama 1)
```

---

## BAŞARI KRİTERLERİ

Aşama 0 tamamlandığında:
- [ ] Hiçbir Kotlin dosyasında `if (lang == "TR")` yok
- [ ] Hiçbir data class'ta `xxxEn`/`xxxTr` field yok
- [ ] Hiçbir AdventureNode içinde hardcoded metin string yok
- [ ] Yeni bir dil eklemek sadece yeni bir JSON dosyası oluşturmak demek
- [ ] TowerScreen.kt < 300 satır
- [ ] GameViewModel.kt < 400 satır
- [ ] Combat log key-based, metin değil
- [ ] EnemyFaction JSON'dan geliyor
- [ ] Uygulama hala çalışıyor (test geçiyor)

---

*Plan onaylandıktan sonra 0.1'den başlıyoruz.*