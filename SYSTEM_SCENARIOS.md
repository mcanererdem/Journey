AI & TEST INSTRUCTION: This document dictates the high-level structural flow of the application. Every screen transition, state transformation, and resource mutation must conform to the macroscopic behavioral scenarios defined below.

MİMARİ MERKEZ: RpgGameScreen (SCAFFOLD & TAB CONTROLLER)
Tüm ekranları içinde barındıran, alt navigasyon barını ve genel iskeleti yöneten ana kabuktur.

Durum ve Geçiş Şartnamesi (Macro Flow)
Girdi (Input): Oyuncu alt navigasyon barından (Bottom Navigation) farklı bir sekmeye tıklar.

İşlem (Process): Jetpack Compose NavHost mekanizması tetiklenir. Mevcut ekranın state yapısı arka planda korunurken, yeni seçilen ekranın ViewModel katmanı ayağa kaldırılır.

Beklenen Çıktı (Expected Output): Eğer currentFloor < 100 ise OuterWorld sekmesi kilitli (disabled) veya tıklanamaz kalır. Diğer sekmeler (Tower, Character, Quests, Journal) arasında pürüzsüz geçiş sağlanır.

SENSE 1: AuthScreen & Character Creation (GİRİŞ VE BAŞLANGIÇ)
Durum ve Geçiş Şartnamesi (Macro Flow)
Girdi (Input): Oyuncu uygulamayı ilk kez açar, Firebase Auth üzerinden misafir veya kalıcı oturum tetikler.

İşlem (Process): Sistem Room veritabanında uid kontrolü yapar. Veri yoksa Karakter Oluşturma (Origin/Kader Seçimi) durumunu aktifleştirir.

Beklenen Çıktı (Expected Output): Seçilen Origin bonuslarına göre gold, aether, will ve storyFlags ilk değerleri atanarak PlayerProfile tablosuna destructive yöntemle sıfırdan yazılır ve ana ekrana yönlendirilir.

SENSE 2: TowerScreen (KULE TIRMANIŞ ETKİLEŞİM MOTORU)
Kendi içinde NARRATIVE, COMBAT, CAMP ve MERCHANT alt ekranlarını (Section) barındıran ana oyun döngüsüdür.

Durum ve Geçiş Şartnamesi (Macro Flow)
Girdi (Input): Oyuncu bir NARRATIVE düğümde (AdventureNode) seçim yapar.

İşlem (Process): Seçeneğin taşıdığı ağırlığa (ChoiceWeight) göre kaynak mutasyonları hesaplanır ve momentum mutlak suretle .coerceIn(0, 100) aralığında kısıtlanarak kaydedilir. Eğer corruptionShift varsa gizli yozlaşma puanı arka planda güncellenir.

Beklenen Çıktı (Expected Output): Alınan karar JournalEntry tablosuna sadece lokalizasyon anahtarı (actionKey) ile işlenir. Düğüm tamamlandığında bir sonraki düğüm veya kat sonu BOSS düğümü yüklenir. Kat geçişlerinde will (İrade) otomatik olarak eksiltilir.

SENSE 3: CharacterScreen (KARAKTER PROFIL VE UNVAN YÖNETİMİ)
Durum ve Geçiş Şartnamesi (Macro Flow)
Girdi (Input): Oyuncu kuleden kazandığı kalıcı bir unvanı (Title) kuşanmak ister.

İşlem (Process): ProfileViewModel ilgili unvanın ön koşul (Precondition) rünlerinin oyuncunun storyFlags listesinde olup olmadığını doğrular.

Beklenen Çıktı (Expected Output): Koşul doğrulanırsa unvan activeTitleId olarak profile yazılır. UI üzerindeki sınıf ismi, rün durumları ve stat barları (Aether/Gold/HP) güncel Momentum aralığına göre (Örn: 0-24 arası mor Void parlaması) anında yeniden çizilir.

SENSE 4: QuestsScreen (DINAMIK GÖREV DENETLEYİCİSİ)
Durum ve Geçiş Şartnamesi (Macro Flow)
Girdi (Input): Oyuncu görev sekmesini açar veya Kule'de ilerlerken bir hikaye rünü tetiklenir.

İşlem (Process): QuestTitleSystem oyuncunun storyFlags rün havuzunu tarayarak koşulları eşleşen gizli (Hidden) görevleri görünür (isVisible = true) yapar.

Beklenen Çıktı (Expected Output): Tamamlanan görevlerin ödülleri (goldChange, aetherChange) tetiklendiğinde doğrudan oyuncu profiline aktarılır ve görev "Tamamlandı" havuzuna kaydırılır.

SENSE 5: JournalScreen (KRONOLOJİK KARAR GÜNLÜĞÜ)
Durum ve Geçiş Şartnamesi (Macro Flow)
Girdi (Input): Oyuncu geçmişte aldığı kararları incelemek için günlük sayfasını açar.

İşlem (Process): Room veritabanından JournalEntry kayıtları kronolojik olarak çekilir. Kayıtlar ham metin içermediği için arayüze gönderilirken cihazın aktif dil seçeneğine göre çözülür.

Beklenen Çıktı (Expected Output): sideAlignmentShift değeri "EVIL" olan yozlaşmış kararlar parşömen üzerinde kan kırmızısı (#9C0000), Sanctum kararları altın sarısı (#C8A94A) renk kodlarıyla yüksek seviyeli listelenir.

SENSE 6: OuterWorldScreen (KULE DIŞI ENDGAME SEFERLERİ)
Durum ve Geçiş Şartnamesi (Macro Flow)
Girdi (Input): Oyuncu bu sekmeyi tetikler.

İşlem (Process): Sistem PlayerProfile.currentFloor değerini kontrol eder.

Beklenen Çıktı (Expected Output): Eğer değer 100'den küçükse ekran gotik bir zincirle kilitli kalır. Değer 100 ve üzerindeyse kule dışı Grey Krallıklar, dış görevler ve fraksiyon skirmish haritası bütünüyle erişime açılır.