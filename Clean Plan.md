# LIGHT & DARKNESS — HİKAYE VE KAT GENİŞLETME PLANI (PHASE 1)

Bu plan, tamamlanan mimari temizlik (Aşama 0) sonrası kule katlarının (Kat 2, Kat 3 ve sonrası) dinamik olarak eklenebilmesini ve yeni eklenecek dillerle (i18n) %100 uyumlu olmasını hedefler.

---

## MEVCUT DURUM VE HEDEF

### Neredeyiz?
- **Altyapı Hazır**: Çift dil i18n temizlendi, metinler `en.json` ve `tr.json` dosyalarına taşındı. ViewModel ve ekranlar bölündü.
- **Sistem Key-Based**: Loglar ve eylemler anahtar kelimeler (keys) üzerinden çalışıyor.

### Hedefimiz Nedir?
- **Kotlin Koduna Dokunmadan Yeni Kat Ekleme**: Kat tasarımları, olaylar (events) ve diyaloglar Kotlin kodunda hardcoded kalmayacak. Yeni kat eklemek sadece JSON blueprints ve locale dosyalarını güncellemekten ibaret olacak.
- **Dil Bağımsızlığı**: Gelecekte eklenecek yeni diller (Almanca, Fransızca vb.) sadece `de.json` veya `fr.json` eklenerek aktif olacak; kod düzeyinde hiçbir `if (lang == "TR")` kontrolü kalmayacak.

---

## YAPILACAK İŞLER — AŞAMALAR

### AŞAMA 1 — Dinamik Kat Blueprint & JSON Altyapısı
**Amaç:** Kat 2, Kat 3 ve gelecek tüm katların (4-100) kurgularının harici JSON dosyalarından okunması ve kod bağımlılığının sıfırlanması.

1. **Kat Blueprints Dışsallaştırılması**:
   - Kat 1, 2 ve 3 için mevcut olan şablon kurallarının tamamen `floor_1.json`, `floor_2.json`, `floor_3.json` vb. blueprint dosyalarından çekilmesi.
   - Bu blueprint dosyalarının içinde hardcoded hiçbir metin olmaması, bunun yerine `floor.1.node.0.title` gibi i18n key'leri taşıması.
2. **Dinamik Seçenek (Choice) ve Efekt Eşleme**:
   - Blueprint içindeki seçimlerin (choices) vereceği ödüllerin, alignment shift (momentum) değişimlerinin ve HP etkilerinin JSON içindeki `ChoiceEffects` modellerinden okunması.

---

### AŞAMA 2 — Çoklu Dil ve Yeni Dil Uyum Paketi
**Amaç:** Gelecekte eklenecek yeni diller için kod tarafında sıfır maliyet bırakılması.

1. **Locale Yapısının Standardizasyonu**:
   - `en.json` ve `tr.json` dosyalarındaki hiyerarşik `floor`, `enemy`, `item` ve `combat` şemalarının kesinleştirilmesi.
   - `LocalizationManager.getFloorString(lang, floor, path)` metodunun test edilerek yeni dillerde de sorunsuz çalıştığının doğrulanması.
2. **Dinamik Faction ve Karakter Sınıfları**:
   - Faction isimleri ve kuşanılan unvanların (`QuestTitleSystem`) tamamen i18n üzerinden çevrilmesi, böylece yeni bir dil eklendiğinde unvanların da otomatik çevrilebilir olması.

---

### AŞAMA 3 — Yetenek (Skills) & Savaş Sistemi Entegrasyonu
**Amaç:** Aşama 0.7'de taslağı çizilen skill sisteminin aktifleştirilmesi ve dinamikleştirilmesi.

1. **Skill Tanımlarının JSON'a Taşınması**:
   - `global_skills.json` katalogu oluşturularak yeteneklerin hasar, aether maliyeti ve etki sürelerinin tanımlanması.
   - Yetenek isimleri ve açıklamalarının i18n key'ler ile locale dosyalarından okunması.
2. **Combat Savaş Ekranı Güncellemesi**:
   - `CombatSection.kt` içerisindeki hardcoded yetenek listesi yerine, oyuncunun faction ve momentum durumuna göre kullanılabilir yeteneklerin dinamik listelenmesi.

---

## BAŞARI KRİTERLERİ (PHASE 1)
- [ ] Yeni bir kat eklemek için sadece `blueprints/floor_X.json` ve locale JSON'larında (`en.json`, `tr.json`) karşılık gelen anahtarları tanımlamak yeterlidir.
- [ ] Yeni bir dil (örneğin İspanyolca `es.json`) eklendiğinde, koddaki hiçbir satır değiştirilmeden tüm kat metinleri, savaş logları ve eşya açıklamaları otomatik çevrilir.
- [ ] Savaş logları ve skill bilgileri tamamen key-based çalışır.