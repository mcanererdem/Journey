# LIGHT & DARKNESS — GELİŞTİRME PLANI
> CLEAN_PLAN.md + STATUS_REPORT.md birleştirildi. Tek kaynak bu döküman.
> Game Bible (tasarım) → GAME_BIBLE.md | Floor Domain (veri modeli) → FLOOR_DOMAIN.md
> Son güncelleme: Phase 0 analizi sonrası

---

## GENEL PHASE YAPISI

```
Phase 0 — Temizlik & Mimari      ← ŞU AN BURDAYIZ
Phase 1 — Floor Sistemi          ← Sonraki
Phase 2 — İçerik (Floor 1-10)   ← Ondan sonra
Phase 3 — Yönetim & Politika    ← İleride
Phase 4 — Multiplayer & Sezon   ← Uzak vadeli
```

---

# PHASE 0 — TEMİZLİK & MİMARİ

## Mevcut Durum (Son Zip Analizi)

### ✅ Tamamlanan
| Ne | Durum |
|---|---|
| `GameViewModel.kt` 2123 → 512 satır | ✅ |
| `TowerScreen.kt` 2085 → 330 satır | ✅ |
| `CombatViewModel`, `FloorViewModel`, `ProfileViewModel` ayrıldı | ✅ |
| `CombatSection`, `NarrativeSection`, `FloorMapSection`, `NodeDetailSheet` ayrıldı | ✅ |
| `CombatDomain.kt` temiz (key-based `CombatLogEntry`, `SkillDef`) | ✅ |
| `FloorDomain.kt` oluşturuldu (temel `AdventureNode`, `NodeChoice`) | ✅ |
| `LocalizationManager.getFloorString()` eklendi | ✅ |
| `NarrativeEngine` tamamen key-based | ✅ |
| `LocalizationExtensions.kt` oluşturuldu | ✅ |

### ❌ Hâlâ Açık (Kritik)
| Sorun | Neden Kritik | Dosya |
|---|---|---|
| `floor_1/2/3.json` hâlâ `titleEn`/`textTr` formatında | İçerik yazımı bu olmadan doğru yapılamaz | `blueprints/` |
| `FloorDomain.kt` eksik modeller (`NodePath`, `NodeChain`, `MerchantRef` vb.) | Yeni JSON şeması parse edilemiyor | `FloorDomain.kt` |
| `JournalEntry` hâlâ `actionTakenEs`/`actionTakenTr` taşıyor | Room migration gerektirir | `GameEntities.kt` |
| `EnemyFaction` hâlâ string matching ile çalışıyor | İsim değişirse faction yanlış atanır | `GameEntities.kt` |
| `FloorBlueprintSystem` çift format parser | Eski+yeni format karışık, hack kod | `FloorBlueprintSystem.kt` |

### ⚠️ Düşük Öncelik
| Sorun | Dosya |
|---|---|
| `_firebaseSyncState` string yerine enum olmalı | `GameViewModel.kt` |
| Prosedürel node üretimi hâlâ `infested_rat` placeholder | `AdventureEngine.kt` |
| `LegacyUpgrades.kt` aktif game design ile uyumsuz | `LegacyUpgrades.kt` |

---

## Phase 0 Görevleri — Sıralı

### GÖREV P0-1: FloorDomain.kt Tamamla
**Öncelik:** KRİTİK | **Tahmini süre:** 1-2 saat

Mevcut `FloorDomain.kt`'ye eklenecekler:

```kotlin
// 1. NodePath — yol sistemi
enum class NodePath { LIGHT, DARK, SHARED }

// 2. ChoiceWeight — seçim ağırlığı
enum class ChoiceWeight { TRIVIAL, MINOR, MODERATE, MAJOR, HEAVY }

// 3. EnemyForm — boss için iki taraf formu
enum class EnemyForm { NEUTRAL, LIGHT_FORM, DARK_FORM }

// 4. NodePrereq — node görünürlük koşulu
data class NodePrereq(
    val requiredPath: NodePath? = null,
    val minMomentum: Int? = null,
    val maxMomentum: Int? = null,
    val minLevel: Int? = null,
    val requiredTitleId: String = "",
    val requiredItemId: String = "",
    val requiredFlag: String = "",
    val excludesFlag: String = ""
)

// 5. ChoicePrereq — seçim koşulu
data class ChoicePrereq(
    val minMomentum: Int? = null,
    val maxMomentum: Int? = null,
    val minLevel: Int? = null,
    val minHp: Int? = null,
    val requiredTitleId: String = "",
    val requiredItemId: String = "",
    val requiredFlag: String = "",
    val excludesFlag: String = ""
)

// 6. SecretConditionType + SecretCondition
enum class SecretConditionType {
    HAS_FLAG, HAS_TITLE, HAS_ITEM,
    MOMENTUM_RANGE, FLOOR_CLEARED,
    BOSS_DEFEATED, STAT_CHECK
}

data class SecretCondition(
    val type: SecretConditionType,
    val value: String = "",
    val minValue: Int = 0,
    val successNodeId: String = "",
    val failNodeId: String = ""
)

// 7. MerchantStockEntry
data class MerchantStockEntry(
    val itemId: String,
    val baseCost: Int,
    val currency: String = "GOLD",       // "GOLD" | "AETHER"
    val minMomentum: Int? = null,
    val maxMomentum: Int? = null,
    val requiredTitleId: String = "",
    val requiredItemId: String = "",
    val discountPercent: Int = 0,
    val premiumPercent: Int = 0
)

data class MerchantRef(
    val merchantId: String,
    val stock: List<MerchantStockEntry> = emptyList()
)

// 8. CampRef
data class CampRef(
    val campId: String,
    val freeHealAmount: Int = 20,
    val paidHealAmount: Int = 40,
    val paidHealCost: Int = 30,
    val willRestoreAmount: Int = 2,
    val hasMiniMerchant: Boolean = false,
    val miniMerchantId: String = ""
)

// 9. NodeChain
data class NodeChain(
    val chainId: String,
    val nodes: List<FloorNode>,
    val exitToPath: Boolean = true
)
```

Mevcut modellere eklenecek field'lar:

```kotlin
// AdventureNode'a ekle:
data class AdventureNode(
    ...
    val path: NodePath = NodePath.SHARED,         // YENİ
    val chainId: String? = null,                   // YENİ
    val chainNext: String? = null,                 // YENİ
    val chainExit: Boolean = false,                // YENİ
    val prereq: NodePrereq? = null,                // YENİ
    val merchantRef: MerchantRef? = null,          // YENİ
    val campRef: CampRef? = null,                  // YENİ
    val secretCondition: SecretCondition? = null   // YENİ
)

// NodeChoice'a ekle:
data class NodeChoice(
    ...
    val prereq: ChoicePrereq? = null,              // YENİ
    val isHidden: Boolean = false,                 // YENİ
    val isIrreversible: Boolean = false,           // YENİ
    val weight: ChoiceWeight = ChoiceWeight.MINOR, // YENİ
    val nextChainNodeId: String? = null            // YENİ
)

// ChoiceEffects'e ekle (alignmentShift → momentumShift):
data class ChoiceEffects(
    ...
    val momentumShift: Int = 0,      // alignmentShift'in yerini alır
    val removesFlag: String = "",    // YENİ
    val consequenceRing: Int = 0,   // YENİ (0=anında, 1-3=geciktirme)
    val consequenceKey: String = "", // YENİ
    val triggerChainId: String = ""  // YENİ
)

// EnemyRef'e ekle:
data class EnemyRef(
    val enemyId: String,
    val isBoss: Boolean = false,
    val form: EnemyForm = EnemyForm.NEUTRAL,  // YENİ
    val scaleFactor: Float = 1.0f             // YENİ
)

// FloorBlueprint tamamen yeniden yaz:
data class FloorBlueprint(
    val floor: Int,
    val region: String,                       // "verdant_depths" vb.
    val type: FloorType,                      // NORMAL | SPECIAL | HUB
    val titleKey: String,
    val descriptionKey: String,
    val minSecondsOnFloor: Int = 0,
    val intro: FloorNode,                     // introScenario yerine
    val pathLight: List<FloorNode> = emptyList(),
    val pathDark: List<FloorNode> = emptyList(),
    val shared: List<FloorNode> = emptyList(),
    val chains: List<NodeChain> = emptyList(),
    val boss: EnemyRef? = null
)

enum class FloorType { NORMAL, SPECIAL, HUB }

// FloorNode = AdventureNode'un yeni adı (veya typealias)
typealias FloorNode = AdventureNode
```

**Instruction:** `alignmentShift` → `momentumShift` rename yapılırken
`GameViewModel`, `FloorViewModel`, `CombatViewModel` içindeki tüm
referanslar da güncellenmeli. Derleme hatası rehber olarak kullanılabilir.

---

### GÖREV P0-2: JournalEntry Temizle
**Öncelik:** KRİTİK | **Tahmini süre:** 45 dakika

```kotlin
// GameEntities.kt — ESKİ:
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val floor: Int,
    val actionTakenEs: String,   // ← KALDIRILACAK
    val actionTakenTr: String,   // ← KALDIRILACAK
    val sideAlignmentShift: String,
    val alignmentImpact: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val nodeIndex: Int = -1
)

// YENİ:
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val floor: Int,
    val actionKey: String,                           // ← YENİ
    val sideAlignmentShift: String,
    val alignmentImpact: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val nodeIndex: Int = -1
)
```

Room database migration gerekli:

```kotlin
// GameDatabase.kt içinde:
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE journal_entries ADD COLUMN actionKey TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE journal_entries DROP COLUMN actionTakenEs")
        // Not: SQLite DROP COLUMN Android API 35'te destekleniyor,
        // daha eskisi için tablo yeniden oluşturulmalı
    }
}
```

`LocalizationExtensions.kt` güncelleme:

```kotlin
// ESKİ:
fun JournalEntry.getActionTaken(lang: String): String =
    if (lang == "TR") actionTakenTr else actionTakenEs

// YENİ:
fun JournalEntry.getActionTaken(lang: String): String =
    LocalizationManager.getString(lang, actionKey)
```

---

### GÖREV P0-3: EnemyFaction Düzelt
**Öncelik:** ORTA | **Tahmini süre:** 30 dakika

`global_enemies.json`'a `faction` field ekle:

```json
{
  "infested_rat": {
    "id": "infested_rat",
    "faction": "BLIGHTED",
    ...
  }
}
```

`GameEntities.kt` içindeki `fromName()` → `fromId()`:

```kotlin
// ESKİ — string matching:
fun fromName(nameEn: String): EnemyFaction { ... }

// YENİ — JSON'dan direkt:
fun fromId(factionId: String): EnemyFaction = when (factionId.uppercase()) {
    "BLIGHTED"  -> BLIGHTED
    "LAWFUL"    -> LAWFUL
    "CHAOTIC"   -> CHAOTIC
    else        -> NEUTRAL
}
```

---

### GÖREV P0-4: floor_1.json Yeni Formata Geçir
**Öncelik:** KRİTİK | **Tahmini süre:** 2-3 saat

Bu görev iki parçalı:

**4a) floor_1.json yeniden yaz** (metin → key):

```json
{
  "floor": 1,
  "region": "verdant_depths",
  "type": "NORMAL",
  "titleKey": "floor.1.title",
  "descriptionKey": "floor.1.description",
  "minSecondsOnFloor": 60,

  "intro": {
    "id": "floor_1_intro",
    "type": "NARRATIVE",
    "path": "SHARED",
    "titleKey": "floor.1.intro.title",
    "descriptionKey": "floor.1.intro.description",
    "choices": [
      {
        "id": "choice_a",
        "labelKey": "floor.1.intro.choice_a",
        "journalKey": "floor.1.intro.choice_a.journal",
        "effects": { "momentumShift": 10, "aetherChange": 30 },
        "weight": "MINOR"
      },
      {
        "id": "choice_b",
        "labelKey": "floor.1.intro.choice_b",
        "journalKey": "floor.1.intro.choice_b.journal",
        "effects": { "momentumShift": -10, "aetherChange": 30 },
        "weight": "MINOR"
      },
      {
        "id": "choice_c",
        "labelKey": "floor.1.intro.choice_c",
        "journalKey": "floor.1.intro.choice_c.journal",
        "effects": { "goldChange": 60 },
        "weight": "TRIVIAL"
      }
    ]
  },

  "path_light": [ ... ],
  "path_dark":  [ ... ],
  "shared":     [ ... ],
  "chains":     [ ... ],

  "boss": {
    "enemyId": "rat_king",
    "isBoss": true,
    "form": "NEUTRAL"
  }
}
```

**4b) en.json ve tr.json'a taşı:**

```json
// en.json içine ekle:
"floor": {
  "1": {
    "title": "The Verdant Depths",
    "description": "...",
    "intro": {
      "title": "Gateway to the Spire",
      "description": "You stand before the massive iron seal...",
      "choice_a": {
        "text": "Consecrate your blade (+Aether, +Light)",
        "journal": "You consecrated your blade at the entrance..."
      },
      "choice_b": {
        "text": "Absorb the blight (+Aether, +Dark)",
        "journal": "You absorbed the surrounding decay..."
      },
      "choice_c": {
        "text": "Scavenge the fallen (+Gold)",
        "journal": "You scavenged the remains..."
      }
    },
    "nodes": { ... },
    "chains": { ... }
  }
}
```

Aynısı `tr.json` için Türkçe olarak.

---

### GÖREV P0-5: FloorBlueprintSystem Yeniden Yaz
**Öncelik:** KRİTİK | **Tahmini süre:** 2 saat

Eski `loadBlueprintFromJson()` kaldırılır. Yeni JSON formatını
parse eden temiz bir parser yazılır:

```kotlin
object FloorBlueprintSystem {

    fun getBlueprintForFloor(floor: Int, player: PlayerProfile? = null): FloorBlueprint {
        return loadBlueprintFromJson(floor)
            ?: generateProceduralBlueprint(floor)
    }

    private fun loadBlueprintFromJson(floor: Int): FloorBlueprint? {
        val json = LocalizationManager.loadFloorBlueprint(floor) ?: return null
        return try {
            parseFloorBlueprint(json, floor)
        } catch (e: Exception) {
            android.util.Log.e("FloorBlueprintSystem", "Parse error floor $floor", e)
            null
        }
    }

    private fun parseFloorBlueprint(json: JSONObject, floor: Int): FloorBlueprint {
        val intro = parseNode(json.getJSONObject("intro"), floor)
        val pathLight = parseNodeList(json.optJSONArray("path_light"), floor)
        val pathDark  = parseNodeList(json.optJSONArray("path_dark"), floor)
        val shared    = parseNodeList(json.optJSONArray("shared"), floor)
        val chains    = parseChainList(json.optJSONArray("chains"), floor)
        val boss      = json.optJSONObject("boss")?.let { parseEnemyRef(it) }

        return FloorBlueprint(
            floor = floor,
            region = json.optString("region", "unknown"),
            type = FloorType.valueOf(json.optString("type", "NORMAL")),
            titleKey = json.optString("titleKey", "floor.$floor.title"),
            descriptionKey = json.optString("descriptionKey", "floor.$floor.description"),
            minSecondsOnFloor = json.optInt("minSecondsOnFloor", 0),
            intro = intro,
            pathLight = pathLight,
            pathDark = pathDark,
            shared = shared,
            chains = chains,
            boss = boss
        )
    }

    private fun parseNode(obj: JSONObject, floor: Int): FloorNode { ... }
    private fun parseNodeList(arr: JSONArray?, floor: Int): List<FloorNode> { ... }
    private fun parseChoiceList(arr: JSONArray?): List<NodeChoice> { ... }
    private fun parseChainList(arr: JSONArray?, floor: Int): List<NodeChain> { ... }
    private fun parseEnemyRef(obj: JSONObject): EnemyRef { ... }
    private fun parseMerchantRef(obj: JSONObject): MerchantRef { ... }
    private fun parseCampRef(obj: JSONObject): CampRef { ... }
    private fun parseEffects(obj: JSONObject): ChoiceEffects { ... }
}
```

---

### GÖREV P0-6: _firebaseSyncState Enum'a Çevir
**Öncelik:** DÜŞÜK | **Tahmini süre:** 15 dakika

```kotlin
// YENİ — GameEntities.kt veya ayrı dosya:
enum class SyncState { IDLE, SYNCING, SUCCESS, FAILURE }

// GameViewModel.kt:
private val _firebaseSyncState = MutableStateFlow(SyncState.IDLE)
```

---

## Phase 0 Tamamlanma Kriterleri

Aşağıdaki tüm maddeler ✅ olmadan Phase 1'e geçilmez:

```
[ ] FloorDomain.kt → tüm modeller eksiksiz, derleniyor
[ ] FloorBlueprint → pathLight/pathDark/shared/chains alanları var
[ ] floor_1.json  → yeni format, sıfır hardcoded metin
[ ] floor_2.json  → yeni format
[ ] floor_3.json  → yeni format
[ ] en.json       → floor.1/2/3 bölümleri eksiksiz
[ ] tr.json       → floor.1/2/3 bölümleri eksiksiz
[ ] JournalEntry  → actionKey taşıyor, metin yok
[ ] EnemyFaction  → fromId() ile çalışıyor, string matching yok
[ ] FloorBlueprintSystem → tek format, hack yok
[ ] Uygulama derleniyor
[ ] Floor 1 oynanabilir (path seçimi, node'lar, boss)
```

---

# PHASE 1 — FLOOR SİSTEMİ GÜÇLENDİRME

*Phase 0 tamamlanınca başlar.*

## P1 Hedefleri

### P1-1: Yol Sistemi (Path) Aktif Et
- Oyuncu kule girişinde taraf seçer (LIGHT / DARK)
- `FloorViewModel` aktif path'i takip eder
- `NarrativeSection` path'e göre doğru node'ları gösterir
- Shared node'lar her iki path'de görünür

### P1-2: Chain Node Sistemi
- Chain başlatan seçim yapılınca `triggerChainId` işlenir
- `FloorViewModel.enterChain()` fonksiyonu
- Zincir biterken `exitToPath: true` ile ana yola dönüş
- Chain içinde dal değişimi (`nextChainNodeId`)

### P1-3: Merchant Sistemi
- `MerchantRef` render edilir
- Momentum filtresi çalışır (minMomentum / maxMomentum)
- Title/item koşulu çalışır
- Fiyat modifikasyonu (discount / premium)

### P1-4: Camp Sistemi
- CAMP node render edilir
- Ücretsiz iyileşme
- Ücretli iyileşme (gold)
- Mini merchant seçeneği

### P1-5: Secret Node Sistemi
- `SecretCondition` kontrol edilir
- Koşul sağlanmadan node görünmez
- `STAT_CHECK` → success/fail branching

### P1-6: Boss Formu
- Boss node'a girildiğinde oyuncunun tarafına göre
  `lightForm` veya `darkForm` yüklenir
- Farklı HP / ATK / loot
- Farklı isim (nameKey)

### P1-7: QuestTitleSystem Genişletme
- Floor bazlı quest tanımları
- Chain tamamlama → quest ilerlemesi
- Secret boss yenme → hidden title
- Momentum koşullu title'lar

---

# PHASE 2 — İÇERİK (FLOOR 1-10)

*Phase 1 tamamlanınca başlar.*

## Bölge: The Verdant Depths (Kat 1-9)

Ton: Öğrenme + merak. Tehlike var ama keşfedilebilir.
Referans: Solo Leveling E-rank dungeon, Made in Abyss 1. tabaka.

### Bölge Karakteristikleri
- Bozulmuş orman / bataklık tabanı
- Kule'nin dibinde yoğun sporlar ve çürüme
- Eski bir uygarlığın kalıntıları ağaçlar arasında gizli
- Blight henüz yönetilebilir seviyede
- Düşmanlar: enfekte hayvanlar, blight yaratıkları, kaybolmuş izci kalıntıları

### Kat Planı (1-9)

| Kat | İsim | Boss | İmza Mekanik |
|---|---|---|---|
| 1 | Giriş Harabesi | Rat King | Temel seçim + yol sistemi öğretimi |
| 2 | Terk Edilmiş Garnizon | Corpse Hound | Chain node öğretimi |
| 3 | Bataklık Geçidi | Mire Witch | Momentum-gated seçimler |
| 4 | Sporlu Orman | Spore Titan | Secret node öğretimi |
| 5 | Eski Kilise Harabesi | Hollow Cleric | Title ödülü ve quest |
| 6 | Yeraltı Tünelleri | Cave Crawler King | Merchant ve camp sistemi |
| 7 | İlk Blight Noktası | Blight Spawn (mini) | Blight mekaniği tanıtımı |
| 8 | Taş Köprü | Bridge Warden | Irreversible seçim |
| 9 | Kapı Önü | Gate Guardian | Kat 10'a hazırlık, tüm mekaniği kullanma |
| 10 | Kalıntı Şehri | Arbiter (Özel) | İlk özel kat, haftalık yönetici |

### İçerik Yazım Standardı (Her Kat)

```
Her kat için hazırlanacaklar:
[ ] floor_X.json  (yeni format, eksiksiz)
[ ] en.json → floor.X bölümü
[ ] tr.json → floor.X bölümü
[ ] global_enemies.json → yeni düşmanlar
[ ] global_items.json   → yeni itemlar
[ ] QuestTitleSystem    → kat quest'leri ve title'ları
```

---

# PHASE 3 — YÖNETİM & POLİTİKA

*Phase 2 sonrası. Multiplayer altyapısı gerektirir.*

- Floor Governor sistemi (yönetici seçimi)
- Guild / Band yapısı
- Skirmish combat format
- Pact Trial sezon sonu event
- Leaderboard sistemi

---

# PHASE 4 — MULTİPLAYER & SEZON

*Uzak vadeli. Ayrı tasarım dökümanı gerektirir.*

- Gerçek zamanlı multiplayer altyapısı
- Cross-shard yönetici sistemi
- Total War (50-200 oyuncu)
- Kingdom-building (post-launch)

---

# GENEL INSTRUCTION'LAR

## Kod Yazarken

1. **Metin asla Kotlin içinde olmaz.**
   Tüm gösterilen metinler `LocalizationManager.getString(lang, key)` ile çekilir.
   `if (lang == "TR")` pattern'i yasaktır.

2. **Data class'lar asla `xxxEn`/`xxxTr` field taşımaz.**
   Her zaman `xxxKey: String` taşır.

3. **JSON'lar asla metin içermez.**
   `titleEn`, `textTr`, `journalEn` gibi field'lar blueprint JSON'da olamaz.
   Metin sadece `locales/en.json` ve `locales/tr.json` içinde olur.

4. **Enemy/item tanımları global'de olur.**
   `global_enemies.json` ve `global_items.json` tek kaynak.
   Floor JSON'lar sadece ID referansı taşır.

5. **EnemyFaction JSON'dan gelir.**
   `fromName()` string matching yasaktır.
   `fromId()` ile enum parse edilir.

6. **Yeni bir dil eklemek sadece yeni bir JSON dosyası oluşturmak demektir.**
   Bu kural ihlal edilirse mimari bozulmuş sayılır.

## İçerik Yazarken

1. **Her seçim `weight` taşır.**
   `TRIVIAL` → küçük atmosferik | `HEAVY` → genellikle irreversible.

2. **Her seçimin `journalKey`'i vardır.**
   Journal boş bırakılamaz. Oyuncu ne yaptığını sonra okuyabilmelidir.

3. **Chain node'lar ana yolu bloklamaz.**
   `exitToPath: true` ile her chain ana akışa geri döner.

4. **Boss her katta 1 tane, iki form.**
   `lightForm` ve `darkForm` farklı stat ve loot taşır.

5. **Consequence ring kullanılır.**
   Seçimlerin bazı sonuçları anında değil, 1-3 kat sonra gelir.
   `consequenceRing: 1` → 1-3 kat içinde tetiklenir.

## Dosya Organizasyonu

```
Değiştirme (hiç dokunma):
  GameDatabase.kt, GameDao.kt, GameRepository.kt
  FirebaseManager.kt
  PlayerProfile (Room entity alanları — migration gerektirir)

Dikkatli değiştir (test gerekli):
  GameViewModel.kt, FloorViewModel.kt, CombatViewModel.kt
  FloorBlueprintSystem.kt, QuestTitleSystem.kt

Özgürce değiştir:
  FloorDomain.kt, CombatDomain.kt
  floor_X.json, en.json, tr.json
  global_enemies.json, global_items.json
  Tüm UI screen dosyaları
```

---

*Phase 0 görevleri tamamlandıkça bu döküman güncellenir.*
*Yeni kararlar önce GAME_BIBLE.md'ye, sonra buraya yansır.*
