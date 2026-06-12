# CLAUDE.md — Light & Darkness (Journey) Proje Talimatları

> Bu dosya, Claude Code (veya bu repoda çalışan herhangi bir Claude oturumu)
> tarafından her oturum başında okunur. Amaç: tekrar tekrar bağlam vermeden
> tutarlı kararlarla geliştirme yapmak.

---

## 0. PROJE ÖZETİ

**Journey / "Light & Darkness"** — Android (Jetpack Compose) tabanlı,
text-ağırlıklı, dark fantasy rogue-lite kule tırmanma RPG'si. Oyuncu 100
katlı bir kuleyi tırmanır, kararlarıyla **Momentum** (Sanctum ↔ Covenant
ekseni) değişir, turn-based savaşlar yapar. TR/EN dual-language.

- Dil: Kotlin, UI: Jetpack Compose + Material3
- DB: Room (`PlayerProfile`, `JournalEntry`)
- İçerik: JSON blueprint'ler (`assets/blueprints/floor_X.json`) +
  locale dosyaları (`assets/locales/en.json`, `tr.json`)
- Build: Gradle KTS, Firebase (Auth + Firestore) entegre

---

## 1. REFERANS BELGE HİYERARŞİSİ — ÖNEMLİ

Bu repoda birden fazla planlama belgesi var ve bazıları birbiriyle
ÇELİŞİYOR. Çakışma durumunda şu sıra geçerlidir:

1. **`PROJECT_STATUS.md`** — kodun GERÇEK şu anki durumu + kesinleşen
   kararlar. ÖNCE BURAYA BAK.
2. **`Clean Plan.md`** — mimari/refactor master plan (dosya yapısı, model
   tasarımı, aşama sırası: 0.1 → 0.8)
3. **`GAME_SOURCE.md`** — içerik/lore master (dünya kurgusu, ton, kat
   yazım kuralları)
4. **`ROADMAP.md`**, **`GAME_DESIGN.md`** — üst seviye vizyon, henüz
   Clean Plan/GAME_SOURCE ile tam senkron değil; bunlardaki bilgi
   PROJECT_STATUS.md veya Clean Plan ile çelişiyorsa İKİNCİSİ KAZANIR.

> Eğer bir görev sırasında bu belgeler arasında çözülmemiş bir çelişki
> fark edersen, kod yazmadan önce kullanıcıya sor — tahmin yürütme.

---

## 2. KESİNLEŞEN MİMARİ KURALLAR

### 2.1 Momentum
- Aralık: **0–100**, nötr = **50**. `-100..+100` ASLA kullanılmaz.
- Tüm `coerceIn(...)` çağrıları `coerceIn(0, 100)` olmalı.
- Bilinen düzeltilecek hata: `GameViewModel.handleRpgChoice` ve
  `selectNodeChoice` içinde `coerceIn(-100, 100)` var — bu bir bug,
  `coerceIn(0, 100)` olmalı. (Detay: PROJECT_STATUS.md §2.1)

### 2.2 Para birimleri
- Tek enerji/para: `aether: Int`. Gleam/Pyre kavramları YOK, hiçbir yeni
  kodda bu isimler kullanılmaz.
- `gold: Int` ayrı, evrensel para birimi olarak kalır.
- `label_gleam` / `label_pyre` locale key'leri kalıntıdır, temizlenecek
  (Detay: PROJECT_STATUS.md §2.2). Yeni kod bu key'leri referans etmesin.

### 2.3 i18n (Clean Plan AŞAMA 0.1-0.5 hedefi)
- **Hedef:** Data class'lar metin taşımaz, sadece `*Key` (locale key)
  taşır. `LocalizationManager.getString(lang, key)` ile çözülür.
- **Şu an:** Çoğu model (`AdventureNode`, `NodeChoice`, `GameOption`,
  `QuestDef`, `TitleDef`, `NarrativeBranchOption`, `EnemyStats`) hâlâ
  `textEn`/`textTr`/`journalEn`/`journalTr` gibi hardcoded alanlar taşıyor.
- **Şablon olarak kullan:** `floor_1.json`, `floor_2.json`, `floor_3.json`
  — bunlar zaten key-referanslı yapıyı kısmen kullanıyor.
- Yeni kod yazarken: mümkünse hardcoded TR/EN string ekleme, key-based
  yaklaşımı taklit et. Mevcut hardcoded alanları "olduğu gibi" bırakmak
  AŞAMA 0.1/0.5 tamamlanana kadar kabul edilebilir — ama YENİ hardcoded
  string EKLEMEKTEN kaçın.

### 2.4 Kod stili / sınırlar
- Inline `Color(0xFF...)` YASAK — `ui/theme/Color.kt`'deki semantic
  isimler kullanılır.
- Inline `dp`/`sp` değeri YASAK — `ui/theme/Dimens.kt` kullanılır.
- Hedef: hiçbir dosya 500 satırı geçmesin (mevcut büyük dosyalar
  `GameViewModel.kt`, `TowerScreen.kt`, `QuestsScreen.kt` — bunlar Clean
  Plan AŞAMA 0.3/0.4 kapsamında bölünecek, henüz bölünmedi).
- TR/EN: Her kullanıcıya görünen yeni metin için her iki dil de eklenir
  (eksik bırakılmaz).

---

## 3. ŞU AN NEREDEYİZ

**Faz:** Clean Plan AŞAMA 0 öncesi — belge netleştirme tamamlandı,
mimari refactor henüz BAŞLAMADI.

**Henüz yapılmadı:**
- [ ] Momentum coerce bug-fix (GameViewModel, 2 nokta) — küçük, bağımsız,
      AŞAMA 0'dan önce de yapılabilir
- [ ] label_gleam/label_pyre temizliği (en.json, tr.json,
      values/strings.xml, values-en/strings.xml)
- [ ] AŞAMA 0.1: FloorDomain.kt + key-based AdventureNode/NodeChoice
- [ ] AŞAMA 0.2: locale JSON yapısını genişletme (floor/enemy/combat/item
      bölümleri) + Outer World key uyumsuzluklarının düzeltilmesi
- [ ] AŞAMA 0.3-0.8: bkz. Clean Plan.md

**Bekleyen:** Kullanıcının yükleyeceği "gerçek source-of-truth" belgesi —
geldiğinde bu dosya ve PROJECT_STATUS.md güncellenecek.

---

## 4. ÇALIŞMA TARZI / SÜREÇ KURALLARI

- Her görev Clean Plan'daki aşamalardan birine karşılık gelmeli. Aşama
  dışı/büyük "hepsini birden refactor et" isteklerinden kaçın — küçük,
  test edilebilir adımlar tercih edilir.
- Bir aşama tamamlandığında bu dosyanın §3 "Şu An Neredeyiz" bölümü
  güncellenmeli (checkbox işaretlenir, sıradaki adım belirtilir).
- Mimari/model kararı gerektiren ama belgelerde netleşmemiş bir konu
  çıkarsa: PROJECT_STATUS.md §4 "Açık Sorular"a not düş, kullanıcıya sor,
  varsayım yapıp ilerleme.
- Test/derleme: `app/build.gradle.kts`'de Robolectric + Roborazzi kurulu;
  UI değişikliklerinde mevcut test/screenshot altyapısını bozmamaya dikkat
  et.

---

## 5. HIZLI REFERANS — ÖNEMLİ DOSYA KONUMLARI

| Konu | Dosya |
|---|---|
| Player state | `data/model/GameEntities.kt` (`PlayerProfile`, `JournalEntry`) |
| Ana ViewModel | `ui/viewmodel/GameViewModel.kt` (bölünecek) |
| Kule UI | `ui/screens/TowerScreen.kt` (bölünecek) |
| Node/Choice modelleri | `data/engine/AdventureEngine.kt` |
| Floor blueprint yükleme | `data/engine/FloorBlueprintSystem.kt` |
| Locale yönetimi | `data/engine/LocalizationManager.kt` |
| Locale içerikleri | `assets/locales/en.json`, `tr.json` |
| El yapımı kat verisi | `assets/blueprints/floor_1.json`, `floor_2.json`, `floor_3.json` |
| Tema/renk/boyut | `ui/theme/Color.kt`, `Dimens.kt`, `Type.kt`, `Theme.kt` |
| Quest/Title sistemi | `data/engine/QuestTitleSystem.kt` |
