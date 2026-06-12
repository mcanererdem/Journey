# PROJECT STATUS — Light & Darkness (Journey)
> Bu belge "şu an kodda gerçekte ne var" durumunu tutar.
> GAME_SOURCE.md, ROADMAP.md, GAME_DESIGN.md, Clean Plan.md ile çelişen
> noktalar burada işaretlenir ve karar verildikçe güncellenir.
> Amaç: Claude (veya başka bir geliştirici) hangi belgenin hangi konuda
> güncel olduğunu bilmeden kod yazmasın.

**Son güncelleme:** 2026-06-12
**Durum:** Belge birleştirme / netleştirme aşaması (Clean Plan AŞAMA 0 öncesi)

---

## 1. BELGE HİYERARŞİSİ (KESİNLEŞTİ)

| Belge | Rol | Güncelleme sırası |
|---|---|---|
| `Clean Plan.md` | **Mimari master** — dosya yapısı, model tasarımı, refactor sırası | İlk güncellenecek |
| `GAME_SOURCE.md` | **İçerik/lore master** — dünya kurgusu, ton, kat yazım kuralları | Clean Plan sonrası, mimariye göre revize |
| `ROADMAP.md` | Üst seviye vizyon/öncelik — Clean Plan ve GAME_SOURCE'a göre revize edilecek | Clean Plan + GAME_SOURCE sonrası |
| `GAME_DESIGN.md` | Ekran/akış rehberi — yukarıdakilere göre revize edilecek | Son |
| **(gelecekte eklenecek "gerçek" source-of-truth)** | Henüz yüklenmedi — geldiğinde bu hiyerarşinin neresine gireceği yeniden değerlendirilecek | TBD |

> Not: Aşağıdaki tüm "kesinleşti" işaretli kararlar geçicidir; gerçek
> source-of-truth belgesi yüklendiğinde çakışan noktalar yeniden gözden
> geçirilecektir.

---

## 2. STAT SİSTEMİ — KESİNLEŞEN KARARLAR

### 2.1 Momentum (Alignment'ın yeni adı)

**Karar:** Aralık **0–100**, nötr = **50**. Negatif aralık (-100..+100) KULLANILMAYACAK.

**Mevcut kod durumu:**
- `PlayerProfile.momentum: Int = 50` — yorum "0 to 100 indicator (0 void/monstrous, 100 light/saintly)" ✅ doğru
- `NarrativeEventProcessor.processNarrativeChoice`: `coerceIn(0, 100)` ✅ doğru
- `GameViewModel.handleRpgChoice`: `coerceIn(-100, 100)` ❌ **HATA — düzeltilecek**
- `GameViewModel.selectNodeChoice`: `coerceIn(-100, 100)` ❌ **HATA — düzeltilecek**

**Yapılacak düzeltme (Clean Plan AŞAMA 0 kapsamında, kod refactor'undan
ÖNCE bile yapılabilecek küçük bug-fix):**
```kotlin
// GameViewModel.kt — handleRpgChoice ve selectNodeChoice içinde:
val newMomentum = (profile.momentum + option.alignmentShift).coerceIn(-100, 100)
// →
val newMomentum = (profile.momentum + option.alignmentShift).coerceIn(0, 100)
```

**QuestTitleSystem title gereksinimleri:** Mekanik olarak 0-100 üzerinden
çalışıyor (örn. `it.momentum <= 15`, `it.momentum >= 85`), bu doğru. Sadece
**açıklama metinlerinde** eski -100/+100 dönemine ait ifadeler var (örn.
"-35 veya daha az Hizalanma", "En az 70 Momentum" gibi karışık terminoloji).
Bu metin düzeltmesi GAME_SOURCE.md revizyonu sırasında ele alınacak
(içerik işi, mimari değil).

### 2.2 Aether (Gleam + Pyre birleşimi)

**Karar:** Tek para birimi `aether: Int`. Gleam/Pyre kavramları YOK.

**Mevcut kod durumu:**
- `PlayerProfile.aether: Int = 0` ✅ zaten birleşik, doğru
- Hiçbir Kotlin dosyasında ayrı `gleam`/`pyre` alanı yok ✅

**Kalıntı (sadece JSON/string kaynaklarında):**
- `en.json`: `"label_gleam": "Aether"`, `"label_pyre": "Aether"` — iki
  farklı key, aynı değer. Kullanılmıyor gibi görünüyor ama temizlenmeli.
- `tr.json`: `"label_gleam": "Semavi Işıltı"`, `"label_pyre": "Kara Ateş"`
  — **gerçek tutarsızlık**: TR tarafı hâlâ eski iki-para-birimi isimlerini
  kullanıyor, EN tarafı zaten "Aether"e geçmiş.
- `values/strings.xml`, `values-en/strings.xml`: aynı `label_gleam`/
  `label_pyre` kalıntıları (Auth ekranı dışında kullanılmıyor olabilir,
  doğrulanacak).

**Yapılacak düzeltme:**
1. `en.json` ve `tr.json`'da `label_gleam` ve `label_pyre` key'lerini
   kaldır, tek bir `label_aether` key'i ekle (EN: "Aether", TR: "Aether"
   veya Türkçe karşılığı — GAME_SOURCE revizyonunda netleşecek).
2. Bu key'i referans alan tüm UI kodu (`LocalizationManager.getString(...,
   "label_gleam")` / `"label_pyre"` çağrıları varsa) `"label_aether"`a
   güncellenecek.
3. `values/strings.xml` ve `values-en/strings.xml`'deki kalıntılar
   kaldırılacak (kullanım doğrulandıktan sonra).

> Not: Bu, AŞAMA 0.2 (locale JSON yapısını genişletme) ile birlikte
> yapılabilir — ayrı bir adım açmaya gerek yok.

---

## 3. CLEAN PLAN İLE EŞLEŞEN/EŞLEŞMEYEN DİĞER GÖZLEMLER

Bu bölüm Clean Plan.md'nin S1-S8 tespitlerini doğrulayan veya genişleten
notlar içerir. Clean Plan'daki aşama sırası DEĞİŞMEDİ; burada sadece ek
bağlam var.

### 3.1 i18n çifte sistem (Clean Plan S1) — DOĞRULANDI
- `AdventureEngine.kt`, `FloorBlueprintSystem.kt` (procedural kısım) ve
  `NarrativeEngine.kt` içinde hâlâ hardcoded `textEn`/`textTr` çiftleri var.
- `floor_1.json`, `floor_2.json`, `floor_3.json` zaten "doğru" yöntemi
  (locale key referansı + `LocalizationManager`) kullanıyor — bu üç dosya
  AŞAMA 0.2/0.5 için ŞABLON olarak kullanılabilir.

### 3.2 Model temizliği (Clean Plan S2) — DOĞRULANDI, kapsam notu
- `AdventureNode`, `NodeChoice`, `GameOption`, `NarrativeBranchOption`,
  `EnemyStats` hepsi metin taşıyor (textEn/textTr/journalEn/journalTr vb.)
- `QuestDef`, `TitleDef` de aynı şekilde (titleEn/titleTr/descEn/descTr) —
  Clean Plan'da açıkça listelenmemiş ama aynı problem kategorisine giriyor.
  AŞAMA 0.1 kapsamına bunlar da dahil edilmeli mi, yoksa ayrı bir
  "0.1b — Quest/Title modellerini key-based yap" adımı mı açılmalı,
  AŞAMA 0.1 detaylandırılırken kararlaştırılacak.

### 3.3 Dosya boyutları (Clean Plan S3) — DOĞRULANDI + ek dosya
- `GameViewModel.kt` (~2100+ satır), `TowerScreen.kt` (~2000+ satır):
  Clean Plan'daki tespitle uyumlu.
- `QuestsScreen.kt` da büyük (~1000+ satır) ve karışık sorumluluklara
  sahip (titles UI + floor objectives UI + narrative events UI + secret
  boss UI). Clean Plan'da bölünecek dosyalar listesine eklenmesi
  düşünülebilir.

### 3.4 Kullanılmayan/erişilemeyen UI elemanları
- `RpgGameScreen.kt` → `OuterWorldTab` çağrısında `LocalizationManager`
  key'leri (`action_rest_title`, `action_rest_desc`, `action_scout_title`,
  `action_scout_desc`, `market_header`) `en.json`/`tr.json`'da YOK
  (en.json'da `rest_title`, `rest_desc`, `scout_title`, `scout_desc` var
  ama `action_` ön ekiyle değil). Bu muhtemelen `LocalizationManager`'ın
  fallback'i (key'in kendisini döndürme) ile sessizce "kırık" görünüyor.
  AŞAMA 0.2 sırasında bu key uyumsuzlukları da düzeltilmeli.

### 3.5 Navigation enum vs gerçek tab kullanımı
- `NavigationTab` enum'unda `LEGACY` ve `SETTINGS` var ve
  `CustomBottomNavigationBar`'da kullanılıyor, ancak `GAME_DESIGN.md`'deki
  ekran akışında bu ikisi yer almıyor (Lobby/FloorPreview gibi planlanan
  ekranlar da kodda yok). GAME_DESIGN.md revizyonu sırasında ya bu ekranlar
  plana eklenecek ya da kod plana göre sadeleştirilecek — şimdilik
  DOKUNULMUYOR.

---

## 4. AÇIK SORULAR (gerçek source-of-truth geldiğinde tekrar değerlendirilecek)

- [ ] `tr.json`'daki `label_pyre: "Kara Ateş"` çevirisi tamamen silinsin mi,
      yoksa "Aether"in TR karşılığı olarak yeni bir terim mi belirlenecek?
- [ ] Quest/Title modellerindeki textEn/textTr alanları AŞAMA 0.1'e dahil
      mi, ayrı adım mı?
- [ ] `LEGACY` ve `SETTINGS` tab'ları GAME_DESIGN akışına nasıl entegre
      edilecek?
- [ ] Outer World locale key uyumsuzlukları (action_rest_title vb.)

---

## 5. SONRAKİ ADIM

Bu belge onaylandıktan sonra:
1. `CLAUDE.md` oluşturulacak (proje kuralları + "şu anda neredeyiz" özeti
   bu belgeye referans verecek).
2. Clean Plan AŞAMA 0.1 detaylandırılacak (kapsam: sadece AdventureNode/
   NodeChoice mi, Quest/Title de dahil mi — bkz. madde 3.2).
3. Momentum coerce bug-fix'i (madde 2.1) ayrı, küçük bir "quick fix" olarak
   AŞAMA 0'dan ÖNCE bile uygulanabilir — bekleyen kullanıcılar için davranış
   hatasını giderir, mimariyi etkilemez.
