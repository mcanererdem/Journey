# Spires of the Cosmos (Cosmic Spires) RPG - Stratejik Yol Haritası (Roadmap)

Bu döküman, projemizin şu anki seviyesini analiz etmek, eksiklikleri belirlemek ve başarılı, yüksek kullanıcı sayısına sahip, akıcı ve keyifli bir rogue-lite RPG oyununa dönüştürülmesi için atılması gereken adımları planlamak amacıyla oluşturulmuştur.

---

## 1. Mevcut Mimari ve Durum Analizi (Architectural Audit)

### 📊 Mevcut Güçlü Yönler (Core Strengths)
1. **Sağlam Veri Katmanı (Data & Persistence)**: Room veritabanı (v3) ile veri bütünlüğü ve lokal kayıtlar güvende. `PlayerProfile` ve `JournalEntry` sorunsuz çalışıyor.
2. **Esnek Sistem Yönetimi**: `FloorBlueprintSystem`, `FloorStateManager`, ve `RewardGenerator` sayesinde prosedürel ve önceden el yapımı kurgulanmış kat yapıları bir arada işlenebiliyor.
3. **M3 Uyumlu Tema ve Arayüz**: Jetpack Compose ile yazılmış tam uyumlu, akıcı, tek-sayfadan yönetilen ama derin bir sekmeli sistem.
4. **Yeni Eklenen Gelişmiş Özellikler**:
   - **Kısıtlanmış Yol Mantığı**: Oyuncular sırayla hareket etmek ve sektörü tamamlamak zorunda.
   - **Gözlem (Scout) Sistemi**: Haritada ilerideki gizemli düğümlerin tiplerini İrade Gücü harcayarak açığa çıkarma mekaniği.
   - **Hover/Tooltip Bilgi Paneli**: Seçilen düğüme ait tehdit seviyesi (Hostile, Apex Overlord, vb.) ve İrade Karşılığı gösterimi.
   - **Görsel Bağlantı Vurguları (Pulsing Glow Line)**: Bulunulan düğüm ile bir sonraki geçerli düğüm arasındaki aktif geçiş hattının parlaması.
   - **Kronolojik Karar Takibi**: Tamamlanmış ve geri dönülen düğümlerde oyuncunun geçmişte aldığı kararların ("YOUR DECISION") günceden çekilerek gösterilmesi.
   - **Kompakt Bottom Nav**: Metin kalabalığından arındırılmış, yalnızca minimalist ikonlar barındıran modern navigasyon çubuğu.

---

## 2. Eksiklerimiz ve Geliştirilmesi Gereken Alanlar (The Gaps)

| Alan | Mevcut Durum | Hedeflenen Durum (Kompleks Seviye) | Aksiyon Alacak Taraf |
| :--- | :--- | :--- | :--- |
| **Savaş Sistemi (Combat)** | Log tabanlı, tek tıklamayla basitleştirilmiş zar atma duellosu. | **Taktiksel Savaş Kontrolü**: Aktif yetenekler (Mana/Gleam maliyetli), kalkan (Shield) yenileme, kritik darbe, zayıflatıcı (Debuff) ve statü etkileri (Zehir, Şok, Kutsama). | **Yapay Zeka (AI)** |
| **Strateji & Kat Yapıları** | Her kat düz doğrusal bir çizgiden (Node 0'dan N'e) ibaret. | **Dallanan Yol Haritası (Forking Paths)**: Slay the Spire tarzı, kat ortasında oyuncuya tercih sunan (Evler sola, canavarlar sağa) matris veya ağaç yapılı kat haritası. | **Yapay Zeka & Sen** |
| **Faction & Guild Yapısı** | Alignment (Hizalanma) değerine bağlı basit bir tarafgirlik (Sanctum/Covenant). | **Klan / İttifak Görevleri (Guild/Skirmish)**: Belli katlarda taraflara özel gizli sığınaklar, taraf dükkanları ve dinamik itibar (Faction Reputation) sisteminin entegre edilmesi. | **Yapay Zeka (AI)** |
| **Hikaye & Seçimler** | `floors_blueprint.json` içindeki statik seçimler. | **Dinamik Seçim Etkileri**: Alınan bir kararın 5 kat sonra karşımıza yeni bir olay veya düşman olarak çıkması (Global Olay Değişkenleri Tablosu). | **Sen (Yazarlık/Tasarım)** |
| **Skirmish / Asenkron PvP** | Yok (Tamamen solo). | **Gölge Savaşçılar (Skirmish)**: Diğer başarılı oyuncuların profillerinin "Gölge Düşman" olarak kuleye yerleştirilmesi (Asenkron Rekabet). | **Yapay Zeka (AI)** |
| **Görsel Tasarım & UI** | Standart M3 kartları ve koyu tema. | **Cosmic Dark Elite UI**: Özel neon partiküller, gradyan çizgiler, her canavar tipine özel küçük vektör/çizim kartları, zafere özel ekran efektleri. | **Yapay Zeka & Sen** |

---

## 3. Yol Haritası Adımları (Milestones)

### 📍 [Adım 1: Savaş Sisteminin Derinleştirilmesi (Milestone S-1)]
*   **Amaç**: Savaşları sadece "Dövüş" butonuna ardı ardına basılan bir log olmaktan çıkarıp, taktiksel bir mini oyuna dönüştürmek.
*   **Aksiyon**:
    *   Savaşa 3 temel yetenek ekle: **Hafif Saldırı (0 Bedel)**, **Ağır Darbe (10 Gleam)**, **Koruyucu Bariyer (Can yeniler/Kalkan ekler)**.
    *   Düşmanın bir sonraki hamlesini (Saldırıya hazırlanıyor, Savunma yapacak) oyuncuya göstererek ("Intent System") taktik yapmasını sağla.

### 📍 [Adım 2: Kat Çeşitlendirmesi ve Kat Soru Sistemleri (Milestone Q-1)]
*   **Öneri Sorun**: *"floor quest title’lar ile ilgili bilgileri nerede tutuyoruz kat kat ayırmak mantıklı mı mesela floor1, floor2 gibi?"*
*   **Analiz ve Karar**:
    *   **Kesinlikle Kat Kat Ayırmak En Mantıklı Yaklaşımdır.**
    *   Şu an tüm veriler tek bir dev `floors_blueprint.json` içinde tutuluyor. Bu dosya ileride hikaye büyüdükçe (100 kat için) megabaytlarca büyüyecek ve okunması/yazılması zorlaşacaktır.
    *   **Yeni Plan**: Blueprints klasörünü `blueprints/floor_1.json`, `blueprints/floor_2.json` şeklinde modüler dosyalara ayıracağız. Run-time sırasında sadece bulunulan katın JSON'ı belleğe yüklenecek. Bu bellek yönetimini optimize ederken senin de yeni katlar yazmanı aşırı kolaylaştırır.

### 📍 [Adım 3: İttifak, Guild ve Bölge Savaşları (Milestone G-1)]
*   **Amaç**: Kulenin katlarında kalıcı ittifaklar (Sanctum vs Covenant) kurmak.
*   **Aksiyon**:
    *   Her 5 katta bir **"Faction Shrine"** ve **"Faction Outpost"** yerleştirilmesi.
    *   Burada "Guild Skirmish" paneli açılarak taraf puanlarının (Reputation) artırılması ve o tarafa özel "Koleksiyoncu Ekipmanları" alınması.

---

## 4. Görev Paylaşımı (Division of Labor)

### 🤖 Yapay Zeka (Benim Yapabileceklerim)
1.  **Karmaşık Algoritmalar & Savaş Motoru**: Hamle analiz fırıldakları, düşman yapay zekası, durum etkileri (Status Effects) yönetimi ve bunların `ViewModel` ve Room veri katmanlarına entegrasyonu.
2.  **Modüler JSON Yükleyici**: Mevcut `FloorBlueprintSystem`'i tek dosyadan parçalı dosya mimarisine (`floor_X.json`) geçirecek kod altyapısını yazmak.
3.  **Performans & Ekran Boyutları Uyumlaştırma**: Tablet, dikey/yatay modlar ve katlanır ekranlar için Jetpack Compose performans iyileştirmeleri (Recomposition sıklığını azaltma).
4.  **Otomatik Test Altyapısı**: Robolectric ve Roborazzi ile savaş mekaniklerini, dükkan satın alımlarını arka planda doğrulayan test senaryolarını kodlama.

### 🧑‍💻 Sen (Senin Yapabileceklerin)
1.  **Dramatik İçerik & Hikaye Yazarlığı**: `floor_1.json`, `floor_2.json` vb. modular dosyalara muhteşem Türkçe ve İngilizce hikaye metinleri yazmak.
2.  **Karar Sonuç Dengesi**: Hangi kararın ne kadar İrade Gücü, Hasar veya Altın vereceğini oyun tecrübene dayanarak optimize etmek.
3.  **İnce Ayar (Balancing)**: Canavarların HP ve Atk değerlerini, dükkan fiyatlarını test ederek zorluk eğrisini yapılandırmak.
4.  **Dış Entegrasyonlar**: Reklam izleme sıklığı, oyun içi satın alım fiyat sınırları gibi oyuncu psikolojisine dokunuşlar.

---

## 5. Başarıya Giden 3 Altın Kural (Retention & High User Count)

1.  **"Easy to Learn, Hard to Master" Savaş Hazzı**: Oyuncu bir dövüşü kazandığında şansıyla değil, doğru zamanda doğru yeteneği (kalkan/saldırı) kullandığı için kazandığını hissetmeli.
2.  **Anlam Taşıyan Seçimler**: Bir katta "Sonsuz Işık Çeşmesi"nden çalınan suyun, sonraki katta karanlık tarikat üyeleriyle karşılaşıldığında "Hırsız!" olarak damgalanmamıza yol açması.
3.  **Koleksiyon ve Statü**: Kazanılan nadir unvanların (Sovereign Title), teçhizatların ve taraftar zırhlarının oyuncu profil kartında görsel birer parıltı (Neon glow, özel rozet) olarak parlaması.
