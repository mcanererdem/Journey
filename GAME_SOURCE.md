# LIGHT & DARKNESS — GAME BIBLE v1.0
> Bu döküman tüm tasarım kararlarının merkezi referansıdır.
> Çelişki durumunda bu döküman kazanır.

---

## 1. ÖZÜN ÖZÜ (One-Sentence Pitch)

**"Antik bir Kule'nin katlarını tırmanan oyuncu, her seçimiyle hem kendini hem dünyayı dönüştürür — ta ki Kule'nin gerçek amacını öğrenene kadar."**

---

## 2. TON & RUHSAL REFERANSLAR

### Ana Ton
- **Ağırlık:** Epik & Destansı (birincil)
- **Renk:** Karanlık ama umut var (ikincil)
- **Temel Hisler:** Koyu + acımasız katmanlar arası köprü

Her sahne şu soruyu sormalı:
> *"Oyuncu bunu okurken hem ürperiyor hem de devam etmek istiyor mu?"*

### Referans Dünyalar

| Kaynak | Aldığımız Şey | Almadığımız Şey |
|---|---|---|
| **Solo Leveling** | Güç eskalasyonu, gate/dungeon hiyerarşisi, "zayıftan güçlüye" yolculuk | Isekai kopuşu, modern Japonya bağlamı |
| **Made in Abyss** | Derinleştikçe artan bedel/bozulma, merak vs tehlike dengesi, relics sistemi | Çocuk karakterler, sevimli estetik |
| **Vinland Saga** | Kimlik sorgulaması, ihanet ve dönüşüm, gerçek güç nedir sorusu | Tarihsel gerçekçilik |
| **Darkest Dungeon** | Kalıcı sonuçlar, atmosferik baskı, kaynak yönetimi | Roguelike reset döngüsü |
| **Disco Elysium** | Felsefi seçimler, kimlik üzerinden mekanik | Modern güncel dünya |

### Referans Oyunlar
- **Fallen London:** Metin zenginliği, faction politikası, delayed consequence
- **Slay the Spire:** Deck/loadout mantığı
- **Darkest Dungeon:** Atmosferik baskı, kalıcı sonuç

---

## 3. DÜNYA TEMEL KURGUSU

### 3.1 Kule Nedir?
Çökmüş bir uygarlığın inşa ettiği **teknolojik-büyüsel bir cihaz**. Aydınlık ve Karanlık bu cihazın iki işletim modudur. Kule'nin gerçek amacı: **Yutucu'yu (The Devourer) mühürlemek**.

Mühür ancak iki tarafın birlikte çalışmasıyla tutar. Bu gerçeği yalnızca en üst yöneticiler bilir.

### 3.2 İki Taraf
**Lawful Choir (Işık):**
"Mührü öngörülebilir ayinle koru. Disiplin, hiyerarşi, kanon."
Estetik: Beyaz-altın, gümüş, düzenli semboller, katı ritüeller.

**Chaotic Vow (Karanlık):**
"Düşman sınırsız; biz de sınırsız olmalıyız. Adaptasyon, yetenek, gizli ağlar."
Estetik: Mor-kızıl, asimetrik semboller, organik büyüme.

Her iki taraf da kısmen haklı, kısmen kör.

### 3.3 Yutucu (The Devourer)
- Oynanabilir değil — tehdit ve içerik kaynağı
- Kule içi tezahürler: Blight Boss'lar (7., 14., 21.… katlarda mini; 33., 66., 99.'da büyük)
- Dış tezahür: Outer Realm'de corruption zone'ları
- Pakt İmanı düşen oyuncular zamanla Yutucu'nun aracına dönüşebilir

### 3.4 Ölüm Felsefesi
Ölüm = iradenin tükenmesi. Kule mühüre yakıt olarak oyuncu iradesini kullanır. Yutucu ölen karakterin son iradesini emer. Bu yüzden her ölüm "küçük bir mühür sızıntısı"dır.

---

## 4. OYUNCU KİMLİĞİ SİSTEMİ

### 4.1 Alignment Modeli (3 Katman)

```
KATMAN 1 — SIDE (Binary, Görünür)
  Lawful Choir | Chaotic Vow
  → Prologue'da seçilir, sadece Betrayal Quest ile değişir

KATMAN 2 — GOOD ↔ EVIL (-100..+100, Görünür)
  Saintly | Good | Stern | Cruel | Monstrous
  → Her seçimde good_evil_delta ile değişir

KATMAN 3 — PAKT İMANI ↔ YUTUCU YOZLAŞMASI (-100..+100, TAMAMEN GİZLİ)
  → Integer asla gösterilmez. Sadece narrative ipuçları.
  → -60 altında: Devourer's Pact gizli içerik açılır
  → +60 üstünde: Pact Trial katkı çarpanı +%30
```

### 4.2 Reflection Class (6 Hücre)
| | Lawful Choir | Chaotic Vow |
|---|---|---|
| Good | Adamantine | Tempest |
| Stern | Codifier | Wildhand |
| Evil | Iron Throne | Carrion |

Betrayal sonrası: aynı moral band, karşı side. (Adamantine ↔ Tempest)

### 4.3 Title Sistemi
- **Persistent:** Asla silinmez. Stat/mekanik etkisi olan. Para ile alınamaz.
- **Season-bound:** Sezon sonunda sıfırlanır. Kozmetik/sembolik.

---

## 5. KULE MİMARİSİ

### 5.1 Kat Hiyerarşisi
```
Kat 100     → Ultra Özel (Ortak — Pakt'ın mühür odası)
Kat 75      → Daha Özel Hub (Işık ağırlıklı)
Kat 66      → Büyük Blight Boss
Kat 50      → Daha Özel Hub (Ortak — Total War sahnesi)
Kat 33      → Büyük Blight Boss
Kat 25      → Daha Özel Hub (Karanlık ağırlıklı)
Kat 10,20.. → Özel Katlar (haftalık yönetici)
Kat 99      → Büyük Blight Boss
Kat 1-9..  → Normal Katlar (günlük yönetici)
Kat 0       → Pakt Girişi (Ortak — güvenli başlangıç)
```

### 5.2 Kat Tipleri
| Tip | Seçim Sayısı | Yönetici Süresi |
|---|---|---|
| Normal | 4 | Günlük |
| Özel (×10) | 6 | Haftalık |
| Daha Özel (25/50/75) | 10 | Sezonluk aday |
| Ultra Özel (0/100) | 18 | 3 aylık |

### 5.3 Blight Yayılımı (Made in Abyss Paraleli)
Derinleştikçe artan bedel:
- **Kat 1-9:** Hafif tehlike. Yeni başlayanlar hayatta kalabilir.
- **Kat 10-24:** Tehlike somutlaşır. İlk kalıcı kayıplar burada.
- **Kat 25-49:** Geri dönüş maliyetli. Korku normal.
- **Kat 50-74:** Gerçek güç sınavı. Zayıflar çıkamaz.
- **Kat 75-99:** Yalnızca güçlüler. Blight her yerde.
- **Kat 100:** Gerçek açıklanır. Herkes değişir.

---

## 6. SEÇIM SİSTEMİ KURALLARI

### 6.1 Seçim Tipleri (14 Tip)
1. **Basit** — A veya B, koşulsuz
2. **Çoklu** — 3-5 seçenek
3. **Stat-gated** — STR/AGI/INT/CHA/LUK eşiği
4. **Alignment-gated** — band veya sayısal eşik
5. **Title-gated** — belirli title gerektirir
6. **Item-gated** — envanterde item gerektirir
7. **Timed** — N saniyede karar verilmezse default
8. **Hidden** — ön koşul sağlanınca görünür
9. **Chained** — önceki seçimden tetiklenir
10. **Irreversible** — geri alınamaz, ekstra UI onayı
11. **Risk/Probabilistik** — şans, AGI/LUK etkili
12. **Sosyal** — CHA check, NPC ikna
13. **Season-bound** — yalnız belirli sezonda
14. **Whisper** — yalnız yüksek Yozlaşma bandında görünür

### 6.2 Seçim Ağırlıkları
```
trivial  (1)  — küçük atmosferik seçimler
minor    (3)  — hafif alignment etkisi
moderate (7)  — belirgin sonuç
major    (15) — kalıcı değişim
heavy    (35) — genellikle irreversible ile birlikte
```

### 6.3 Consequence Halkaları
- **Halka 0:** Anında (aynı sahnede)
- **Halka 1:** 1-3 kat içinde
- **Halka 2:** 5-10 kat içinde
- **Halka 3:** 20+ kat sonra (dramatic reveal)

### 6.4 Seçim Metadatası (Her seçimde zorunlu)
```json
{
  "id": "unique_id",
  "type": ["basit", "hidden"],
  "weight": "moderate",
  "good_evil_delta": 5,
  "pact_corruption_delta": -2,
  "is_whisper": false,
  "pacifist_safe": true,
  "irreversible": false,
  "prereq": { ... },
  "effects": [ ... ],
  "consequence_ring": 1
}
```

---

## 7. SAVAŞ SİSTEMİ

### 7.1 Format
- **Temel:** Turn-based metin (solo)
- **Party:** 2-4 kişi (özel katlarda)
- **Raid:** 8 kişi (ultra özel, sezon final)
- **Skirmish:** 5-10 oyuncu
- **Guild War:** 15-30 oyuncu
- **Total War:** 50-200 oyuncu (Floor 50, sezonda 2 kez)

### 7.2 Loadout/Deck Sistemi
- Savaş öncesi skill deck kurulur
- **Lawful Choir:** 3 slot (disiplin)
- **Chaotic Vow:** 4 slot (kaos)
- Savaş otomatik ilerler (Watch Mode / Quick Mode)
- **Intervention Window:** Oyuncunun kendi turunda 2 saniyelik pencere (Item / Flee)
- **Pact Interrupt:** Boss'a karşı, bir kerelik (Covenant Call / Pact Surge)

### 7.3 Ölüm Cezası
- -%10 tüm stats, 30 dakika
- Kat düşüşü: kat × %5 (min 1, max 5)
- 60 saniye kat girilemez

---

## 8. YÖNETİCİ SİSTEMİ (Floor Governance)

### 8.1 Seçim/Atama Matrisi
| Kat | Mod | Süre |
|---|---|---|
| Normal | Seçim (topluluk oyu) | Günlük |
| ×10 | Seçim (ağırlıklı oy) | Haftalık |
| 25/50/75 | Seçim (peer + senior) | Sezonluk |
| 0/100 | Atama (Warden + outgoing) | 3 Aylık |

### 8.2 Temel Yönetici Yetkileri
- Günlük quest yayınlama
- O katta pasif buff (combat stat YOK)
- Manager dashboard + rozet
- Özel kat: haftada 1 event, ekonomi ±%5, shortcut quest
- Daha özel: Guild War başlatma, kısa geçit engelleme (max 2 saat)
- Ultra özel: Decree of the Watch, sezon teması oylaması, lore-canonized not

### 8.3 İhanet Maliyeti
- Side flip zorunlu (Lawful ↔ Chaotic)
- Good↔Evil bandı KORUNUR
- Reflection Class flip (aynı band, karşı side)
- Eski title'lar kaybedilir, "Betrayer" kalıcı kazanılır
- 14 gün seçim adayı olamaz

---

## 9. SEZON SİSTEMİ

### 9.1 Sezon Yapısı (3 Ay)
- Ay 1-2: Faction warfare, kat ele geçirme
- Ay 3 başı: Pre-Trial mobilizasyonu
- Son 7-14 gün: **Pact Trial** — Yutucu baskısı zirveye çıkar

### 9.2 Pact Trial Sonuçları
- **Triumph:** Her iki faction ödüllenir, sonraki sezon threat düşer
- **Hold:** Mütevazı ödüller, stabil
- **Fracture:** Bazı katlar kilitlenir, blight genişler, factions birbirini suçlar

### 9.3 Trial Will Ağırlıkları
- Pakt Görevleri: %30
- Tırmanılan Kat: %20
- Savaş Gücü: %20
- Pakt İmanı: %20
- Sınıf Evrimi: %10

---

## 10. KAT YAZIM KURALLARI

### 10.1 Her Kat İçin Zorunlu Elementler
```
[ ] Kat numarası ve ismi
[ ] Atmosfer tipi (Normal/Tense/Mysterious/Hopeless/Serene/Void/Cursed/Blessed/Misty)
[ ] Tema ve mood (1-2 cümle)
[ ] Referans paralel (hangi SL/MiA/VS sahnesine benziyor)
[ ] Node sayısı ve tipi
[ ] Boss veya mini-boss
[ ] İmza mekaniği (ilk kez öğrettiği şey)
[ ] Alignment etkisi (GE ve PC delta aralığı)
[ ] Seçim sayısı (tier'a göre)
[ ] Verdiği başlıca title/item
[ ] Delayed consequence (hangi katlarda tetiklenir)
[ ] Yönetici notu (bu katta yönetici ne yapabilir)
```

### 10.2 Ton Kalibrasyonu (Kat Aralığına Göre)
```
1-9   → Öğrenme + merak. Tehlike var ama öğrenilebilir. [Solo Leveling: E-rank dungeon]
10-24 → İlk gerçek bedeller. Optimizm kırılmaya başlar. [MiA: 2. tabaka]
25-49 → Kimlik sınavı. "Bu kişi kim?" sorusu. [Vinland Saga: kölelik dönemi]
50-74 → Gerçek güç. Zayıflar çıkamaz. [MiA: 4. tabaka, Bonedrawing]
75-99 → Dönüşüm ya da çöküş. [Berserk: Eclipse]
100   → Gerçek. Her şey yeniden yazılır. [Made in Abyss: Reg'in geçmişi]
```

### 10.3 Tekrar Eden Motifler (Flavor Çerçevesi)
- Her 7. katta küçük Blight tezahürü
- Her 10. katta yönetici değişimi sahnelenir (narrative event)
- Her 25. katta "Pakt'ın sesi" duyulur (lore açıklaması)
- Her 33/66/99'da büyük Blight Boss
- Kule dışarısı Outer Realm ile bağlantı: bazı katlarda "kapı" açılır

---

## 11. EKONOMİ ÖZETİ

| Para | Kaynak | Kullanım |
|---|---|---|
| GOLD | Evrensel | Trade, auction, sink |
| GLEAM | Işık aktiviteleri | Işık içerikleri |
| PYRE | Karanlık aktiviteleri | Karanlık içerikleri |
| TOKEN | Yönetici görevleri | Event/kozmetik havuzu |
| GEMS | Gerçek para | Sadece kozmetik |

GLEAM ↔ PYRE dönüşümü yok. Pillar: Hiçbir mekanik etkili içerik para ile alınamaz.

---

## 12. ENDGAME ROLLER

| Rol | Koşul | Ödül |
|---|---|---|
| Pacifist | Hiç sentient yaratık kasıtla öldürmemek | Unique mercy title |
| Warlord | Sezon savaş skoru zirvesi | Savaş skarı kozmetik |
| Hunter | Devourer/Blight boss kill'leri | Pact Brand kozmetik |
| Pilgrim / Beaconbearer | Yük/bedel/kurtarma quest hattı | Derin lore erişimi |
| Reverse Climber | 100'den 0'a inişi tamamlamak | Unique title |
| Shortcut-Only | Sadece gizli geçişlerle tırmanmak | Lore-canonized note |
| Steward Politician | 3+ farklı tier'da yöneticilik | Governance title |
| Warden | Ultra özel yönetici | Sezon karar hakları |
| Devourer Hunter | Yüksek Yozlaşma + özel hücre | Pact Brand (üst tier) |

---

## 13. AÇIK KARARLAR (Sonraki Aşamalarda Netleşecek)

- [ ] Outer Realm haritası ve biome'ları (post-launch)
- [ ] Reflection Class skill ağaçları (detay)
- [ ] Kingdom-Building sistemi (post-launch)
- [ ] Sezon teması isimleri (1. sezon için)
- [ ] Kat 0 tam tasarımı (prologue + tutorial entegrasyonu)
- [ ] NPC tekrarlayan karakter listesi (Kule'nin sakinleri)

---

## 14. YAZILMIŞ KATLAR

| Kat | İsim | Durum |
|---|---|---|
| 1 | — | Yazılacak |
| 2 | The Shadowed Corridors | Mevcut JSON var, revize edilecek |

---

*Son güncelleme: v1.0 — Başlangıç*
*Sonraki adım: Kat 1-10 yazımı*
