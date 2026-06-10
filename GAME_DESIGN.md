# Journey — Oyuncu Akışı & Feature Rehberi (v2)

> **Son Güncelleme:** Haziran 2026  
> Bu döküman, oyuncunun oyuna ilk girişinden itibaren yaşayacağı tüm deneyimi, ekran geçişlerini, sistemleri ve feature'ları tanımlar.  
> Tüm geliştirme kararları bu belge üzerinden şekillendirilir.

---

## 📝 Tasarımcı Notları Nasıl Eklenir?

Belgeye kendi notlarını eklemek için dosyanın **en altındaki "BÖLÜM 6 — Tasarımcı Notları"** bölümüne yaz.  
Format:
```
### [TARİH] Konu Başlığı
- Not satırın
- İkinci satır
```
Ben her geliştirme döngüsünde bu notları okuyup belgeye ve koda yansıtacağım.  
Ayrıca belgenin herhangi bir yerine `<!-- TODO: fikir veya soru -->` şeklinde de yorum bırakabilirsin.

---

## BÖLÜM 1 — Oyuncu Yolculuğu (Player Journey)

```
[İlk Açılış]
      │
      ▼
[Splash / Loading]
      │
      ▼
[Giriş / Kayıt]  ←── Firebase Auth (Google veya Email/Password)
      │
      ├─► Yeni oyuncu → [Karakter Oluşturma] → [Lobby]
      └─► Mevcut hesap → [Lobby]
                │
                ▼
         [ANA LOBBY]
                │
                ├─► [🏰 KULE'YE GİR]
                │         │
                │         ▼
                │   [Kat Giriş Önizlemesi]  ←── Kat adı, tehdit, hazırlık
                │         │
                │         ▼
                │   [KAT TIRMANIŞ]
                │         │
                │         ├─► [Narratif Node]  → Seçim yap → Sonuç
                │         ├─► [Savaş Node]     → Savaş → Zafer/Mağlubiyet
                │         ├─► [Sandık Node]    → Seçim yap → Ödül
                │         ├─► [Shrine Node]    → Seçim yap → Etki
                │         ├─► [Tüccar Node]    → Alışveriş yap
                │         └─► [KAT içi CAMP]  → Dinlen, Will doldur
                │               │
                │               ▼
                │         [Kat Sonu BOSS]
                │               │
                │       ┌───────┴──────────────┐
                │       ▼                      ▼
                │   [Zafer → Ödül]      [Spirit Fracture]
                │       │                      │
                │       ▼                      ▼
                │   [Sonraki Kat]      [Ölüm Özeti + Checkpoint]
                │                      (kalıcı kazanımlar korunur)
                │
                ├─► [👤 KARAKTER]
                │         ├─► Kimlik kartı, unvan değiştir
                │         ├─► Koleksiyon, Relic'ler
                │         └─► Legacy Unlocks (meta-progression)
                │
                ├─► [🎯 GÖREVLER]
                │         ├─► Günlük görevler
                │         ├─► Hikaye / Chain görevler
                │         ├─► Gizli görevler
                │         └─► Macera görevleri (tırmanıştan tetiklenir)
                │
                ├─► [📖 KARAR GÜNLÜĞÜ]
                │
                └─► [🌍 DIŞ DÜNYA]  ←── KİLİTLİ (100. kat sonrası açılır)
                          │
                          ├─► Yeni harita: farklı bölgeler
                          ├─► Özel maceralar & olaylar
                          └─► Endgame içerik
```

---

## BÖLÜM 2 — Ekranlar ve İşlevleri

### 2.0 Giriş / Kayıt Ekranı
**Dosya:** `AuthScreen.kt`  
**Amaç:** Oyuncu hesabını oluşturur veya giriş yapar. Profil bulutta saklanır.

**Auth Stratejisi:**
- **Firebase Auth** kullanılır (Firebase plugin zaten projede mevcut)
- İki giriş yöntemi:
  1. **Google ile Giriş** — Tek tıkla, kullanıcı dostu
  2. **Email + Password** — Alternatif, kayıt + giriş formu
- **Misafir Modu (Guest)** — Firebase Anonymous Auth; oyuncu sonradan hesaba bağlayabilir

**Akış:**
```
İlk açılış
  → Hesap var mı? (Firebase token kontrol)
      → Evet: Direkt Lobby
      → Hayır: Auth ekranı göster
          → Google ile Giriş: tek buton
          → Email/Pass: Form → doğrulama → Lobby
          → Misafir: Anonymous auth → Lobby
              (Banner: "Hesap oluştur — ilerlemen kaybolmasın!")
```

**Tasarım:**
- Tam ekran koyu arkaplan, kule silueti
- Ortada "Journey" logosu (gothic font)
- Alt kısımda giriş seçenekleri (Google butonu öne çıkar)
- Küçük "Misafir olarak devam et" link-text

**Teknik:**
- Firebase Auth UID, Room DB'deki PlayerProfile ile eşleştirilir
- Firestore: Profil verisi UID altında saklanır (Aşama 4'te tam sync)
- Şimdilik: Auth → UID alındı → local Room DB kullan → Lobby

---

### 2.1 Splash / Yükleme Ekranı
**Dosya:** `SplashScreen.kt`  
**Amaç:** Oyun varlıklarını yüklerken atmosfer kur.

**Görsel:**
- Koyu obsidyen arka plan
- Kule silueti yavaşça beliriyor (fade-in animasyon)
- "Journey" gothic fontla ortada
- Altta: ince progress bar (yükleme göstergesi)

**Mantık:**
- Firebase Auth durumu kontrol et
- PlayerProfile var mı kontrol et
- → Auth ekranı / Lobby / Karakter oluşturma yönlendir

---

### 2.2 Karakter Oluşturma Ekranı
**Dosya:** `CharacterCreationScreen.kt`  
**Amaç:** Yeni oyunculara kimlik ve başlangıç hikayesi ver.

**3 Adımlı Akış:**

**Adım 1 — İsim Seç**
- Text field + önerilen isimler

**Adım 2 — Origin (Kader) Seç**

| Origin | Açıklama | Bonus |
|---|---|---|
| ⚔️ Fallen Knight | Eski düzeni yeniden kurmak için döndü | +20 HP, +15 Gold |
| 🌑 Void Touched | Karanlıktan güç çekiyor | +20 Aether, +1 Momentum (Covenant yönünde) |
| 🗡️ Wandering Rogue | Kimseye bağlı değil | +30 Gold, ücretsiz Scout (her kat başı) |
| 📖 Lore Keeper | Kulenin sırlarını biliyor | +1 Will başlangıç, +10 EXP/node |

**Adım 3 — Onay**
- Seçim özeti + "Tırmanış başlasın" butonu
- Küçük bir açılış flavor text (Origins'e göre özelleşmiş)

---

### 2.3 Ana Lobby
**Dosya:** `LobbyScreen.kt`  
**Amaç:** Her oturumun başlangıç noktası. Hızlı bilgi + aksiyona geç.

**Ekran Yapısı:**
```
┌─────────────────────────────────┐
│  [Avatar] OYUNCU ADI • RANK     │
│  Kat 7/100  ❤️80  ⚡7  ✨45     │
├─────────────────────────────────┤
│                                 │
│      [🏰 KULE'YE GİR]           │  ← Birincil aksiyon
│                                 │
│  [👤 Karakter]  [🎯 Görevler]   │
│  [📖 Günlük]    [🌍 Dış Dünya*] │  *kilitli <100.kat
├─────────────────────────────────┤
│  GÜNLÜK GÖREVLER  (1/3 ✓)      │
│  ☑ 3 node tamamla              │
│  ☐ 1 boss öldür                │
│  ☐ Bir karar al (zincir görev)  │
├─────────────────────────────────┤
│  Streak: 🔥 3 gün              │
└─────────────────────────────────┘
```

---

### 2.4 Kat Giriş Önizlemesi (Pre-Climb)
**Dosya:** `FloorPreviewScreen.kt`  
**Amaç:** Kuleye girmeden son hazırlık + kat bilgisi. OuterWorld'ün yerini alır.

> **Karar:** Dış Dünya (Outer World) kule içi hazırlık yerine **100. kat sonrası endgame** olarak tutulur.  
> Kule içi hazırlık bu ekran ve **Camp Node** ile karşılanır.

**İçerik:**
- Kat adı ve kısa atmosfer açıklaması
- Tehdit seviyesi (Normal / Tehlikeli / Apex)
- Mevcut statların özeti (HP/Will/Gold/Aether)
- "Camp'ta Dinlen" → Will doldur (ücretsiz, 1x/kat)
- "Girecek Teçhizatı Seç" → Aktif 3 slot seçimi
- Sonraki 3 node tipini göster (Scout özelliği burada)
- **"KULEYE GİR" butonu**

---

### 2.5 Kule Tırmanışı (Ana Oyun)
**Dosya:** `TowerScreen.kt`  
**Amaç:** Oyunun kalbi.

**Node Tipleri:**

| Node | İkon | Açıklama |
|---|---|---|
| NARRATIVE | 📜 | Hikaye anı, 2-3 seçenekli |
| COMBAT | ⚔️ | Düşman savaşı |
| BOSS | 💀 | Kat patronu |
| CHEST | 💎 | Sandık/hazine |
| SHRINE | 🔮 | Sunak, stat etkisi + Momentum etkisi |
| MERCHANT | 🛒 | Alışveriş |
| CAMP | 🏕️ | **[YENİ]** Her 5 nodeda bir camp — dinlenme |
| EVENT | ⚡ | **[YENİ]** Görev tetikleyici özel olaylar |
| SECRET | 🗝️ | Gizli içerik (yüksek Momentum/koşul gerektiren) |

**Camp Node Kuralları:**
- Her katta 1 adet (yaklaşık ortada)
- Ücretsiz: +2 Will doldurma
- Ücretli: +40 HP (30 Gold karşılığı)
- Burada mini tüccar da olabilir (küçük envanter)
- Bir sonraki yol dallanmasını görebilirsin (harita önizlemesi)

**Savaş Paneli:**
```
[Düşman Adı] — Faction ikonu
HP: ████████░░ (170/200)
Niyet: 🗡️ Saldırıya hazırlanıyor...

[Hafif Saldırı]  [Ağır Darbe ✨15]  [Bariyer 🛡️]

Oyuncu: ❤️ 60/100  🛡️ SHIELDED (1 tur)
Log: "Kristal kırık bir parça kolunuzu kanattı. -12 HP"
```

---

### 2.6 Görevler Ekranı
**Dosya:** `QuestsScreen.kt`  
**Amaç:** Kısa ve uzun vadeli hedefler — hikaye ilerlemesinin motoru.

**Görev Türleri:**

#### 🗓️ Günlük Görevler (Daily)
- Her gün 00:00'da sıfırlanır, 3 adet
- Basit hedefler: "5 node tamamla", "1 boss öldür"
- Ödül: Gold, Aether, Legacy Points

#### 📖 Hikaye Görevleri (Story)
- Ana hikaye zincirleri, katlarla bağlantılı
- Sıralı: Öncekini bitirmeden sonrakine geçilmez
- Örnekler:
  - "Kule'nin Sırrı I" — Kat 1-5 boyunca 3 Lore node'u tamamla
  - "Sanctum'un Çağrısı" — Momentum 70+ iken bir boss öldür
  - "Void'in Fısıltısı" — 5 Covenant kararı al ve Malakar'ı yeniden karşıla
- Ödül: Özel unvanlar, kalıcı Relic, lore açılımları

#### 🔗 Zincir Görevler (Chain)
- Birbiriyle bağlantılı görev serisi (3-5 aşama)
- Her aşama bir öncekinin sonucuna bağlı
- Örnekler:
  - "Kayıp Şövalye'nin İzleri": 
    1. Kat 3'te yaralı şövalyeyi iyileştir
    2. Kat 7'de şövalyenin silahını bul
    3. Kat 12'de onu düşman olarak karşıla — öldür veya affet
- Ödül: Büyük, hikaye açılımı + kalıcı eşya

#### 🗝️ Gizli Görevler (Hidden / Secret)
- Oyuncu ekranda görmez başlangıçta — tetiklenince açılır
- Tetikleyiciler: Belirli kararlar, belirli bir Momentum değeri, belirli bir kat
- Örnekler:
  - Kat 2'de tüccarı 3 kez geçip geçersen gizli NPC açılır
  - Momentum 90+ iken Shrine'a git → "Ascendant" gizli görevi başlar
- Ödül: Nadir unvanlar, gizli lore, özel silah

#### ⚔️ Macera Görevleri (Adventure / Exploration)
- Tırmanış sırasında spesifik event/node'lardan tetiklenir
- Tower ekranında küçük "!" ikonu ile belirtilir
- Anlık karar gerektiren, zaman baskılı olanlar
- Örnekler:
  - "Yankı Dehlizi'nde" bir fısıltı duyuyorsun → Araştır → Yan görev
  - Tüccardan çalınan eşyayı sonraki katlarda sorgulama
- Ödül: Bonus Gold, Aether, veya yan hikaye kolu

---

### 2.7 Karakter Sayfası
**Dosya:** `CharacterScreen.kt`

**Bölümler:**
- **Kimlik Kartı:** Ad, Rank, Origin, Unvan (equipped), Spirit Fracture sayısı
- **Momentum Barı:** Sanctum ↔ Covenant görseli + mevcut sınıf
- **İstatistikler:** Toplam karar, savaş, en yüksek kat
- **Unvanlar:** Grid görünümü, aktif olanı değiştir
- **Koleksiyon:** Kazanılan eşyalar ve Relic'ler
- **Legacy Unlocks:** Meta-progression tablosu

---

### 2.8 Karar Günlüğü
**Dosya:** `JournalScreen.kt`

**Görünüm:**
- Sayfa texture, gothic font
- Her karar bir "günlük girişi"
- Sanctum kararları: altın renk / Covenant kararları: mor
- Görev etiketleri: hangi görevle ilgili olan kararlar işaretlenir

---

### 2.9 Dış Dünya (Outer World) — 100. Kat Sonrası
**Dosya:** `OuterWorldScreen.kt`  
**Kilit:** Kat 100'ü tamamlayan oyunculara açılır.

**Karar Gerekçesi:**
> Kule tırmanışı sırasında "Dış Dünya" sekmesi mevcut olsun ama kilitli görünsün.
> Bu hem merak uyandırır ("100. katta ne olacak?") hem de oyunu odaklı tutar.
> 100. kat sonrası Outer World endgame içeriği sunar — yeni harita, özel maceralar, PvP hazırlığı.

**İçerik (Endgame):**
- Farklı coğrafyalar: "Void Wastes", "Celestial Peaks", "Blight Plains"
- Keşif görevleri (exploration quests)
- Nadir Relic avcılığı
- Gölge Savaşçılar (Asenkron PvP hazırlığı)

---

## BÖLÜM 3 — Feature Listesi & Durumları

### 3.1 Core Features

| # | Feature | Durum | Öncelik | Notlar |
|---|---|---|---|---|
| F01 | Kat navigasyonu (linear) | ✅ Var | — | Çalışıyor |
| F02 | Node sistemi (6 tip) | ✅ Var | — | Camp+Event eklenecek |
| F03 | Seçim sistemi (3 seçenek) | ✅ Var | — | Çalışıyor |
| F04 | Room DB kalıcılığı | ✅ Var | — | Çalışıyor |
| F05 | Dual-lang (TR/EN) | ✅ Var | — | Çalışıyor |
| F06 | Scouting sistemi | ✅ Var | — | Çalışıyor |
| F07 | Spirit Fracture (ölüm) | ✅ Var | — | Revize edilecek |
| F08 | Checkpoint sistemi | ✅ Var | — | Her 10 katta |
| F09 | Will (İrade) sistemi | ✅ Var | — | Korunuyor |
| F10 | Journal (Günlük) | ✅ Var | — | UI yenilenecek |
| F11 | Faction sistemi | ✅ Var | — | Momentum'a dönüşecek |
| F12 | Proc. floor generation | ✅ Var | — | Çalışıyor |
| F13 | Title/Unvan sistemi | ✅ Var | — | Genişletilecek |

### 3.2 Değiştirilecek Features

| # | Feature | Mevcut | Değişim | Öncelik |
|---|---|---|---|---|
| C01 | Stat sistemi | HP+Gleam+Pyre+Will+EXP | HP+Gold+Aether+Will | 🔴 YÜKSEK |
| C02 | Alignment | -100/+100 kalıcı | Momentum (organik, kısa vadeli) | 🔴 YÜKSEK |
| C03 | Savaş sistemi | Tek buton | 3 aksiyon + Intent + Status | 🟠 ORTA |
| C04 | Kat haritası | Düz liste | Dal+düğüm görsel harita | 🟠 ORTA |
| C05 | OuterWorld sekmesi | Her zaman açık | Kilitli → 100.kat sonrası açılır | 🟡 SONRA |

### 3.3 Yeni Eklenecek Features

| # | Feature | Açıklama | Öncelik | Aşama |
|---|---|---|---|---|
| N01 | Design System | Color/Type/Dimens temiz yapı | 🔴 YÜKSEK | 0 |
| N02 | UI Dosya Bölme | Modüler Compose mimarisi | 🔴 YÜKSEK | 0 |
| N03 | Dark Fantasy UI | Font, renk, animasyon | 🔴 YÜKSEK | 0 |
| N04 | Firebase Auth | Google + Email/Pass + Guest | 🔴 YÜKSEK | 0 |
| N05 | Karakter Oluşturma | Origin seçimi, isim | 🔴 YÜKSEK | 0 |
| N06 | Camp Node | Kule içi dinlenme noktası | 🟠 ORTA | 1 |
| N07 | Tactical Combat | 3 aksiyon + Intent + Status | 🟠 ORTA | 1 |
| N08 | Görev Sistemi (tam) | Daily+Story+Chain+Hidden+Adventure | 🟠 ORTA | 1 |
| N09 | Modüler JSON | `floor_X.json` mimarisi | 🟠 ORTA | 2 |
| N10 | Dallanan Yol | Alternatif güzergahlar | 🟡 SONRA | 2 |
| N11 | Story Flags | Kararların ilerideki katları etkilemesi | 🟡 SONRA | 2 |
| N12 | Daily Loop | Günlük görev + streak | 🟡 SONRA | 3 |
| N13 | Meta-Progression | Legacy Points + kalıcı unlocklar | 🟡 SONRA | 3 |
| N14 | Outer World (Endgame) | 100. kat sonrası harita | 🔵 İLERİDE | 4 |
| N15 | Firebase Firestore Sync | Bulut kayıt | 🔵 İLERİDE | 4 |
| N16 | Async PvP | Gölge savaşçılar | 🔵 İLERİDE | 4 |

### 3.4 Kaldırılan / Ertelenen Features

| # | Feature | Neden |
|---|---|---|
| R01 | Secret Boss combats | Yeniden tasarlanacak, şimdilik kaldırıldı |
| R02 | Guild/Klan savaşları | Backend — Aşama 4 |
| R03 | Gleam + Pyre (çift para) | Aether olarak birleştirildi |
| R04 | Abyss Scouting (Outer World mini-game) | OuterWorld yeniden tasarlandı |
| R05 | Outer World (kule içi hazırlık olarak) | 100.kat sonrası endgame oldu |

---

## BÖLÜM 4 — Oyun Sistemleri Detayları

### 4.1 Stat Sistemi

```
❤️ HP        — Can. 0'da Spirit Fracture.
💰 Gold      — Evrensel para birimi.
✨ Aether    — Enerji. Sanctum+Covenant kararlarından kazanılır.
               Rengi Momentum'a göre değişir: mavi-gold vs derin mor.
⚡ Will      — İrade. -1/node, -2/kat geçişi, -1/scouting.
📍 Momentum  — 0-100 arası, kısa vadeli hizalanma. 50=nötr.
               Sanctum kararları yukarı iter, Covenant aşağı iter.
               3 katta bir ±3 nötre döner (organik drift).
```

### 4.2 Momentum & Sınıf

| Aralık | Sınıf Adı | Özel |
|---|---|---|
| 0-24 | Void-Touched | Covenant özel diyaloglar, mor Aether |
| 25-45 | Shadow Walker | Kısmi Covenant bonusu |
| 46-54 | Wandering Soul | Nötr — hiçbir tarafa bonus yok |
| 55-74 | Lightseeker | Kısmi Sanctum bonusu |
| 75-100 | Sanctum Devoted | Sanctum özel diyaloglar, altın Aether |

### 4.3 Savaş Sistemi

**Akış:**
1. Düşman girer → flavor text + HP bar
2. **Intent göster** (düşmanın niyeti): Saldırı / Savunma / Özel Hamle
3. Oyuncu aksiyon seçer:
   - `Hafif Saldırı` — Ücretsiz, 15-25 hasar
   - `Ağır Darbe`   — 15 Aether, 40-60 hasar, %20 kritik
   - `Bariyer`      — 10 Aether, 2 tur kalkan (20 hasar emer)
4. Hasar hesapla + log göster
5. Düşman hamlesi (Intent'e göre)
6. Status efektleri güncelle
7. Döngü — birinin HP'si 0'a düşünce biter

**Status Effects:**

| Efekt | Etki | Süre |
|---|---|---|
| POISONED 🩸 | Her tur 8 hasar | 3 tur |
| STUNNED 💫 | Hamle yapamaz | 1 tur |
| BLESSED ✨ | +30% hasar | 2 tur |
| SHIELDED 🛡️ | 20 hasar emer | 2 tur |
| WEAKENED 💔 | -30% hasar verir | 2 tur |

### 4.4 Görev Sistemi (Detay)

**Görev Veri Modeli:**
```kotlin
data class Quest(
    val id: String,           // "daily_001", "story_003", "chain_001_step2"
    val type: QuestType,      // DAILY / STORY / CHAIN / HIDDEN / ADVENTURE
    val titleEn: String,
    val titleTr: String,
    val descriptionEn: String,
    val descriptionTr: String,
    val condition: QuestCondition,  // Tamamlanma koşulu
    val reward: QuestReward,
    val prerequisiteId: String?,    // Zincir görevler için: önceki görev ID'si
    val isVisible: Boolean = true,  // Gizli görevler false başlar
    val expiresAt: Long? = null     // Günlük görevler için timestamp
)
```

**Tetikleyiciler:**
- `DAILY` → Her gün 00:00'da sistem tarafından oluşturulur
- `STORY` → Oyuncu ilk kez o kata ulaşınca açılır
- `CHAIN` → Önceki zincir adımı tamamlanınca açılır
- `HIDDEN` → `StoryFlag` değişkenleri belirli koşulu sağlayınca açılır
- `ADVENTURE` → Tırmanış sırasında özel bir node tetikler

### 4.5 Firebase Auth Stratejisi

**Şimdi (Aşama 0):**
- Firebase Auth SDK eklenir
- Google Sign-In + Email/Password + Anonymous (Guest)
- Auth başarılı → UID alınır → Room DB'deki PlayerProfile'e kaydedilir
- Çevrimdışı: Room DB kullanılır, çevrimiçi olunca sync

**Sonra (Aşama 4):**
- Firestore: Her save noktasında profil buluta yüklenir
- Cihaz değişimde profil kurtarma
- Push notification (streak hatırlatıcı, sezon bildirimi)

---

## BÖLÜM 5 — Dark Fantasy Atmosfer Rehberi

### 5.1 Yazım Tonu
- **İngilizce:** Gothic, şiirsel, ağır. "The cold stones whisper of fallen kings."
- **Türkçe:** Ağır, lirik, karanlık. "Taşlar çöken krallıkları fısıldıyor."
- Emoji → sadece UI etiketlerinde. Hikaye metinlerinde **asla**.
- Hitap: "Sen" (TR) ve "You" (EN) — ikinci tekil.

### 5.2 Renk Semantiği

| Durum | Renk (Hex) | Kullanım |
|---|---|---|
| Sanctum / Işık | `#C8A94A` (Antique Gold) | Gleam, iyilik, Sanctum ikonları |
| Covenant / Karanlık | `#7B2FBE` (Deep Void Purple) | Void enerji, Covenant ikonları |
| Tehlike / Hasar | `#C62828` (Blood Red) | HP azalma, hasar uyarısı |
| İyileşme | `#2E7D32` (Forest Green) | HP artışı |
| Nötr / Sistem | `#78909C` (Slate Silver) | EXP, sistem mesajları |
| Boss | Kırmızı + Mor gradient | Boss savaş ekranı |
| Arkaplan | `#0A0A0F` (Void Black) | Ana arkaplan |
| Yüzey | `#12121A` (Dark Stone) | Kart arka planı |
| Kenarlık | `#2A2A3A` (Iron Edge) | Kart kenarlığı |

### 5.3 Font Ailesi

| Kullanım | Font | Özellik |
|---|---|---|
| Büyük başlıklar | Cinzel Decorative | Gothic serif, kule hissi |
| Ekran başlıkları | Cinzel (Regular) | Gothic serif, okunabilir |
| Gövde metin | Crimson Pro | Serif, lore metnine uygun |
| Etiketler / UI | Rajdhani | Küçük caps, modern |
| Sayılar / Stat | Rajdhani Bold | Okunaklı rakamlar |

### 5.4 Animasyon Kuralları

| Olay | Animasyon | Süre |
|---|---|---|
| Node açılışı | `spring` animasyon, kart yükselir | 250ms |
| HP düşme | Kırmızı flash + sayı kayar | 300ms |
| Boss girişi | Ekran kararır → Boss adı fade-in | 500ms |
| Zafer | Gold particle + ekran parlar | 600ms |
| Spirit Fracture | Ekran yavaş karartılır + "çatlak" efekti | 800ms |
| Momentum değişimi | Bar animasyonu kayar | 400ms |
| Camp noktası | Sıcak turuncu ışıma pulse | Döngüsel |

---

## BÖLÜM 6 — Tasarımcı Notları

> Bu bölüme kendi düşüncelerini, sorularını ve önerilerini yaz.  
> Her not sonraki geliştirme döngüsünde değerlendirilir.

<!-- Örnek format:
### [2026-06-10] Konu
- Not 1
- Not 2
-->

_Henüz not eklenmedi. İlk notunu buraya ekleyebilirsin._
