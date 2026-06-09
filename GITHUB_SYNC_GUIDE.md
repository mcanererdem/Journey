# Geliştirme Ortamları Eşitleme Kılavuzu (GitHub & IDE Sync Guide)

Bu kılavuz, yerel bilgisayarındaki IDE (Android Studio, VS Code, vb.) ile tarayıcıdaki **Google AI Studio** ortamını aynı anda nasıl sorunsuz, çakışmasız kılacağını ve güvenle GitHub'a yükleyeceğini (push) açıklar.

---

## 💻 İki Farklı Ortamda Çalışmak Sorun Yaratır mı?

**Evet, eğer doğru eşitleme protokolüne uyulmazsa "Merge Conflict" (Kod Birleştirme Çakışmaları) yaşanır.**
AI Studio ile yerel IDE'niz aynı Git dalını (Branch - genelde `main`) hedefliyorsa, AI Studio'nun yazdığı bir satır ile senin yerel bilgisayarında yazdığın satır çakışabilir. Bu da Git geçmişinde kırılmaya ("divergent histories") neden olur.

Ancak **aşağıdaki kuralları uygulayarak bu süreci tamamen güvenli ve aşırı verimli hale getirebilirsin.**

---

## 🛠️ Çakışmaları Önlemek İçin En Güvenli Protokol

İki tarafı senkronize tutmak için 3 alternatif yöntemimiz bulunmaktadır. Senin için en uygun olanını seçebilirsin:

### 🌟 ALTERNATİF A: "Zaman Dilimi Ayrımı" Protokolü (En Kolay ve Tavsiye Edilen)
En temiz yöntem, her iki tarafın aynı anda kod geliştirmemesidir.

1.  **AI Studio'dan Çıkarken (Bana görev verdiğinde)**:
    *   Ben görevimi bitirip projeyi sorunsuz derlediğimde (`compile_applet` başarılı olduğunda), AI Studio arayüzündeki ayarlar menüsünden **Push to GitHub** butonuna tıkla.
2.  **Lokal Bilgisayarına (IDE) Geçtiğinde**:
    *   **HİÇBİR EDİT YAPMADAN ÖNCE**, projenin kurulu olduğu terminalde ya da IDE'ndeki Git menüsünde mutlaka şu komutu çalıştır:
        ```bash
        git pull origin main
        ```
    *   Bu, AI Studio'da benim yaptığım tüm dosyaları lokal bilgisayarına çeker ve çakışmayı sıfıra indirir.
3.  **Lokalde İşin Bittiğinde (Commit & Push)**:
    *   Lokalde kodunu yaz, test et ve GitHub'a push et:
        ```bash
        git add .
        git commit -m "feat: kat hikayeleri güncellendi"
        git push origin main
        ```
4.  **Tekrar AI Studio'ya Döndüğünde**:
    *   Benimle sohbete başlamadan önce, AI Studio file explorer üzerinden veya sohbet ekranındaki GitHub entegrasyonu yardımıyla **"Pull latest changes from GitHub"** tetiğini çalıştır. Böylece senin bilgisayarında yazdıkların buradaki tarayıcıya akar.

---

### 🛡️ ALTERNATİF B: "Branching" (Dallandırma) Protokolü (Profesyonel Yöntem)
Daha bağımsız çalışmak ve kendi geliştirmelerini benimkilerden soyutlamak istiyorsan:

1.  **Lokal Bilgisayarında Yeni Bir Dal Aç**:
    *   Yerelinde her zaman `dev-lokal` veya `feat-hikaye` gibi ayrı dallarda çalış:
        ```bash
        git checkout -b feat-hikaye
        ```
    *   Değişikliklerini bu dala push et.
2.  **AI Studio Dert Etmesin**:
    *   Ben AI Studio içinde `main` dalında kod yamaya, veritabanını yükseltmeye devam ederim.
3.  **Merge (Birleştirme) İşlemi**:
    *   İki tarafın kodlarını GitHub üzerinde bir **Pull Request (PR)** açarak veya lokaline çekip birleştirerek entegre et:
        ```bash
        git checkout main
        git pull origin main
        git merge feat-hikaye
        # Çakışma varsa IDE yardımıyla çöz, ardından push et.
        git push origin main
        ```

---

## 📝 Benle Çalışırken Dikkat Etmen Gereken "Altın Git Kuralları"

1.  **Her Seferinde Tek Görev**: Bana tek bir seferde birden fazla karmaşık görev verme. Örneğin *"Savaş sistemini yaz ve SQL tablosunu değiştir"* demek yerine önce *"SQL tablosunu ekle"* de, bitsin, push et, sonra savaş sistemine geçelim.
2.  **Bilinçli Kütüphane Güncellemesi**: Lokalde dependencies (`build.gradle.kts` veya `libs.versions.toml`) değiştirdiysen bana ilk mesajında *"dependencies güncelledim, lütfen pull edip compile_applet çalıştır"* de ki uyumlu kalalım.
3.  **Derleme Kontrolleri**: AI Studio'daki işin bitince mutlaka benim derleme durumumu (`compile_applet` sonucunu) gör. Başarılı olmuşsa GitHub'a pushla. Kırık kod pushlamak iki tarafı da zora sokar.
