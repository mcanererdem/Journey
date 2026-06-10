# Journey — Dark Fantasy Tower RPG: Stratejik Yol Haritası (v2)

> **Son Güncelleme:** Haziran 2026  
> Bu döküman projenin şu anki durumunu, kararlaştırılmış değişiklikleri ve önceliklendirilmiş geliştirme adımlarını içerir. Tüm geliştirme kararları bu belge üzerinden alınır.

---

## 1. Vizyon & Hedef

**Hedef:** Yüksek kullanıcı sayısına sahip, oyuncuların tekrar tekrar döndüğü, karanlık fantasy atmosferini tam anlamıyla yaşatan bir text-based rogue-lite tower RPG.

**Temel Oyun Döngüsü (Core Loop):**
```
Hazırlanma → Tırmanma → Karar Ver → Savaş → Ödül → Ölüm/İlerleme → Tekrar
```

**Oyun Kimliği:**
- Atmosfer: Gotik karanlık fantasy, yıkık kozmik kule, iki rakip güç (Sanctum vs Covenant)
- Tür: Text-based rogue-lite RPG, seçim-sonuç ağaclı narratif
- Platform: Android (Jetpack Compose)
- Dil: Türkçe & İngilizce (dual-lang)

---

## 2. Mevcut Durum Analizi

### ✅ Çalışan & Güçlü Sistemler
| Sistem | Durum | Notlar |
|---|---|---|
| Room Veritabanı (v3) | ✅ Sağlam | PlayerProfile + JournalEntry stabil çalışıyor |
| FloorBlueprintSystem | ✅ İyi | Katlar 1-3 el yapımı, 4-100 prosedürel |
| LocalizationManager | ✅ Çalışıyor | EN/TR dual-lang |
| Scouting Sistemi | ✅ Var | İrade harcayarak ileriki node tiplerini görmek |
| Checkpoint Sistemi | ✅ Var | Her 10 katta checkpoint |
| Will (İrade) Kaynağı | ✅ Var | Hareketler irade tüketiyor |
| Faction Sistemi | ✅ Temel | Sanctum / Covenant / Neutral |
| Compose Navigasyon | ✅ Var | Tab tabanlı navigasyon |

### ❌ Eksik & Sorunlu Sistemler
| Sistem | Mevcut | Sorun |
|---|---|---|
| Savaş Derinliği | Tek buton dövüşü | Taktiksel değil, sıkıcı |
| Meta-Progression | Yok | Ölüm = sadece ceza, kalıcı kazanım yok |
| Retention Döngüsü | Yok | Oyuncu yarın neden dönsün? |
| UI Mimarisi | TEK dosya (4900+ satır) | Geliştirilemez, yönetilemez |
| Design System | Dağınık | Renkler/fontlar inline yazılmış |
| Onboarding | Yok | İlk 5 dakika tasarlanmamış |
| Analytics | Yok | Oyuncu davranışı izlenemiyor |

---

## 3. Kararlaştırılan Değişiklikler (Feature Simplification)

### 3.1 Stat Sistemi Sadeleştirmesi

**Kaldırılan / Birleştirilen:**

| Eski | Yeni | Karar |
|---|---|---|
| HP + MaxHP | HP (maks sabit başlangıçta) | Korundu |
| Gleam + Pyre (2 ayrı para) | **TEK Enerji: Aether** | Birleştirildi — iki para birimi karmaşıklık yaratıyor, oyuncu taktik yerine resource management yapıyor |
| Will (İrade) ayrı sistem | Will korunuyor | Seyahat maliyeti olarak anlamlı |
| EXP + Level + MaxExp | Level sistemi basitleştirildi | Level milestone bazlı (1,5,10,20...) olacak, her kat geçişte otomatik |
| Alignment (-100/+100) | **Momentum** (0-100, sıfırlanır) | Anlık karar ağırlığı, faction lock olmayacak |

**Sonuç: Oyuncu yönettiği şeyler:**
```
❤️ HP  |  💰 Gold  |  ✨ Aether (eski Gleam/Pyre yerine)  |  ⚡ Will
```

### 3.2 Faction Sistemi Revizyonu

- Sanctum / Covenant çekişmesi korunuyor — **tema ve atmosferin ruhu bu**
- Oyuncu kesin bir tarafa geçmek zorunda değil; **Momentum** sistemi kararlarını yansıtır
- Momentum yükseldikçe o tarafa ait özel olaylar ve diyaloglar tetiklenir

### 3.3 Kaldırılan Özellikler (Şimdilik)

- ❌ Skirmish / Asenkron PvP (backend gerektiriyor, sonraya)
- ❌ Guild/Klan savaşları (aşama 2)
- ❌ Google Play Billing (simülasyon kalacak, gerçek entegrasyon sonra)
- ❌ Secret Boss combats (karmaşıklık/değer oranı düşük, yeniden tasarlanacak)

---

## 4. UI Mimarisi Planı

### 4.1 Dosya Bölme Stratejisi

Mevcut `RpgGameScreen.kt` (4900+ satır) aşağıdaki yapıya bölünecek:

```
ui/
├── theme/
│   ├── Color.kt          — Tüm renk sabitleri (genişletilecek)
│   ├── Type.kt           — Font ailesi + typography scale (yeniden yazılacak)
│   ├── Dimens.kt         — [YENİ] Spacing/radius/elevation sabitleri
│   └── Theme.kt          — MaterialTheme konfigürasyonu
│
├── components/           — [YENİ KLASÖR] Tekrar kullanılabilir bileşenler
│   ├── StatBar.kt        — HP / Will / Aether progress bar bileşeni
│   ├── NodeCard.kt       — Adventure node kartı
│   ├── CombatPanel.kt    — Savaş arayüzü bileşeni
│   ├── FactionBadge.kt   — Sanctum/Covenant/Neutral rozeti
│   ├── ActionBanner.kt   — Durum mesajı banner
│   └── DarkFantasyButton.kt — Tema uyumlu buton
│
├── screens/
│   ├── RpgGameScreen.kt  — Sadece scaffold + navigasyon (ince tutulacak)
│   ├── TowerScreen.kt    — Kule tırmanma ekranı
│   ├── OuterWorldScreen.kt — Dinlenme/hazırlık ekranı
│   ├── CharacterScreen.kt  — Karakter sayfası
│   ├── QuestsScreen.kt   — Görevler
│   └── JournalScreen.kt  — Karar günlüğü
│
└── viewmodel/
    └── GameViewModel.kt  — (mevcut, refactor edilecek)
```

### 4.2 Design System Kuralları

**Renkler (Color.kt):**
- Tüm renkler semantic isimle tanımlanacak: `DarkBackground`, `SanctumAccent`, `CovenantAccent`, `DangerRed`, `HealGreen`
- Inline `Color(0xFF...)` yazmak **yasak** — her zaman named constant kullanılacak

**Typography (Type.kt):**
- `CinzelDecorative` — Başlıklar (dark fantasy serif)
- `UnifrakturMaguntia` veya `MedievalSharp` — Özel başlıklar (rune hissi)
- `Crimson Pro` — Gövde metni (okunabilir, gothic serif)
- Tüm fontlar Google Fonts'tan yüklenecek

**Dimens (Dimens.kt):**
```kotlin
object Dimens {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val PaddingLarge = 24.dp
    val CardRadius = 12.dp
    val NodeCardRadius = 16.dp
    // ...
}
```

### 4.3 Dark Fantasy UI Yenileme Hedefleri

- **Arkaplan:** Saf siyah değil, koyu taş/obsidyen doku hissi veren gradient arka plan
- **Kartlar:** Hafif iç ışıma (inner glow), faction rengine göre kenarlık
- **Butonlar:** Mineral/rün dokulu, basıldığında pulsing animasyon
- **HP Barı:** Kırmızı→turuncu degradesi, düşükken titreme animasyonu
- **Typografi:** Serif başlıklar, küçük caps etiketler (label), italic journal metni

---

## 5. Yol Haritası Adımları (Öncelik Sırasıyla)

### 🔴 AŞAMA 0 — Altyapı ve UI (Şu An Başlanacak)
> **Hedef:** Temiz, ölçeklenebilir, dark fantasy atmosferli bir temel oluştur.

**0.1 Design System Kurulumu**
- [ ] `Dimens.kt` dosyası oluştur
- [ ] `Type.kt` sıfırdan yaz: Google Fonts entegrasyonu, tüm text style'lar
- [ ] `Color.kt` genişlet: semantic renk adları, faction renkleri, durum renkleri
- [ ] `Theme.kt` güncelle: dark fantasy Sanctum ve Covenant temaları

**0.2 UI Dosya Bölme**
- [ ] `ui/components/` klasörü oluştur
- [ ] `RpgGameScreen.kt`'dan her ekranı ayrı dosyaya çıkar
- [ ] Tekrar kullanılan bileşenler `components/`'e taşı
- [ ] Her dosya max ~400 satır olacak şekilde sınırla

**0.3 Stat Sadeleştirmesi**
- [ ] `PlayerProfile`'dan `gleam` ve `pyre` kaldır, `aether` ekle
- [ ] `alignment` → `momentum` olarak yeniden adlandır
- [ ] ViewModel ve UI'daki tüm referansları güncelle
- [ ] JSON blueprint'lerde `gleamChange`/`pyreChange` → `aetherChange`

---

### 🟠 AŞAMA 1 — Savaş Derinliği (S-1)
> **Hedef:** Savaşı taktiksel bir mini oyuna dönüştür.

**1.1 Temel Yetenekler**
- [ ] 3 combat action: `LIGHT_STRIKE` (ücretsiz), `HEAVY_BLOW` (15 Aether), `BARRIER` (HP/kalkan yeniler)
- [ ] Düşman `Intent` sistemi: düşmanın sıradaki hamlesi ekranında görünür

**1.2 Status Effects**
- [ ] `POISONED`, `STUNNED`, `BLESSED`, `SHIELDED` durum etkileri
- [ ] Her durum 1-3 tur sürer, ikonla gösterilir

**1.3 Boss Özellikleri**
- [ ] Boss'ların 2 fazı olsun (HP %50'de faz geçişi)
- [ ] Faz geçişinde özel dialogue satırı

---

### 🟡 AŞAMA 2 — Kat Yapısı ve İçerik (Q-1)
> **Hedef:** Modüler içerik mimarisi + dallanan yol.

**2.1 Modüler JSON Mimarisi**
- [ ] `assets/blueprints/floor_1.json`, `floor_2.json`... yapısına geç
- [ ] `FloorBlueprintSystem`'i modüler yükleyiciye çevir
- [ ] Bellek optimizasyonu: sadece aktif kat yüklü

**2.2 Dallanan Yol Haritası**
- [ ] Her katta en az iki alternatif güzergah (Slay the Spire tarzı)
- [ ] Güzergah seçimi: "Kolayı al vs zorlayı al" dengesi (riski-ödülü)
- [ ] Görsel yol haritası (node graph) ekranı

**2.3 Hikaye Bağlantısı**
- [ ] Global event değişkenleri tablosu (`StoryFlags`)
- [ ] Örnek: Kat 3'te verilen karar, kat 8'de karşılaşmayı etkiler

---

### 🟢 AŞAMA 3 — Retention & Meta-Progression (R-1)
> **Hedef:** Oyuncu ertesi gün neden dönsün sorusunu yanıtla.

**3.1 Kalıcı İlerleme (Meta)**
- [ ] `Legacy Points` sistemi: Her run'dan kalıcı kazanım
- [ ] `Permanent Unlocks`: Başlangıç bonusları, pasif yetenekler kilit açma
- [ ] `Relic Collection`: Özel runlarda bulunan kalıcı koleksiyon parçaları

**3.2 Daily Loop**
- [ ] Günlük 3 görev sistemi (Daily Quests)
- [ ] Streak ödülleri (3, 7, 14, 30 gün)
- [ ] Günlük giriş bonusu

**3.3 Sezon Sistemi**
- [ ] 4 haftalık sezonlar
- [ ] Sezon özel içerik (kat teması, özel boss)
- [ ] Sezon sonu ödülleri (kalıcı unvan, görsel rozet)

---

### 🔵 AŞAMA 4 — Backend & Sosyal (B-1)
> **Hedef:** Oyuncular arası etkileşim ve rekabet.

**4.1 Firebase Entegrasyonu**
- [ ] Firestore: Oyuncu profili bulut yedeklemesi
- [ ] Firebase Auth: Hesap oluşturma
- [ ] Push Notifications: Streak hatırlatıcı, sezon bildirimi

**4.2 Asenkron PvP (Gölge Savaşçılar)**
- [ ] Başarılı oyuncuların profilleri "Gölge Düşman" olarak rastgele katlara eklenir
- [ ] Yenince özel ödül kazanılır

**4.3 Liderlik Tablosu**
- [ ] Haftalık en yüksek kata ulaşma tablosu
- [ ] Faction bazlı genel skor tablosu

---

## 6. Görev Paylaşımı

### 🤖 Yapay Zeka (AI) Sorumluluğu
1. **Tüm Kotlin kodu**: ViewModel, UI bileşenler, engine, Room migration
2. **Altyapı tasarımı**: Modüler dosya mimarisi, design system kurulumu
3. **Algoritma**: Savaş motoru, procedural generation, stat formülleri
4. **Test altyapısı**: Unit test ve Robolectric testleri

### 🧑‍💻 Kullanıcı (Oyun Tasarımcısı) Sorumluluğu
1. **Hikaye içeriği**: `floor_X.json` dosyaları için Türkçe/İngilizce narratif metinler
2. **Balans kararları**: Düşman HP/ATK değerleri, ödül miktarları, zorluk eğrisi
3. **Tasarım onayı**: Mockup ve UI kararlarının onaylanması
4. **İçerik stratejisi**: Hangi katlara hangi event'in geleceği planı

---

## 7. Başarı Metrikleri (Definition of Success)

| Metrik | Hedef |
|---|---|
| D1 Retention | > %50 |
| D7 Retention | > %25 |
| D30 Retention | > %10 |
| Ortalama Oturum Süresi | > 8 dakika |
| Kullanıcı Başına Ortalama Kat | > Kat 15 |
| 5 Yıldızlı Yorum Oranı | > %60 |

---

## 8. Teknik Borç & Kural Listesi

> Bu kurallar her geliştirme adımında geçerlidir:

1. **Tek sorumluluk:** Her `.kt` dosyası tek bir ekran veya bileşeni kapsar
2. **Inline değer yasak:** Renk/boyut değerleri hiçbir zaman inline yazılmaz
3. **Max satır:** Hiçbir dosya 500 satırı geçmez
4. **Türkçe/İngilizce:** Her kullanıcı mesajının her iki dil versiyonu olur
5. **Test önce:** Savaş mekaniklerinde değişiklik = birim test zorunlu
6. **JSON doğrulama:** Blueprint JSON değişikliklerinde şema doğrulaması yapılır
