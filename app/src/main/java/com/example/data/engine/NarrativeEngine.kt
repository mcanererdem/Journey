package com.example.data.engine

data class GameOption(
    val textEn: String,
    val textTr: String,
    val alignmentShift: Int, // Positive for Sanctum (+), Negative for Void (-)
    val goldChange: Int = 0,
    val gleamChange: Int = 0,
    val pyreChange: Int = 0,
    val hpChange: Int = 0,
    val journalEn: String,
    val journalTr: String
)

data class FloorScenario(
    val floor: Int,
    val titleEn: String,
    val titleTr: String,
    val descriptionEn: String,
    val descriptionTr: String,
    val optionA: GameOption, // Celestial Sanctum styled (Order/Light/Sacrifice)
    val optionB: GameOption, // Void Covenant styled (Power/Shadow/Adaptation)
    val optionC: GameOption  // Neutral / Tactical styled (Rest/Inquiry/Mercenary)
)

object NarrativeEngine {

    fun getScenarioForFloor(floor: Int): FloorScenario {
        if (floor in 1..3) {
            val blueprint = FloorBlueprintSystem.getBlueprintForFloor(floor)
            return blueprint.introScenario
        }
        return when {
            floor == 100 -> FloorScenario(
                floor = 100,
                titleEn = "The Sovereign Throne: The Eternal Duel",
                titleTr = "Hükümdar Tahtı: Ebedi Düello",
                descriptionEn = "You have reached the pinnacle of the Tower. The Eternal Blight has breached the final seals. Before you stands the Avatar of the Core. Your side demands the absolute sacrifice. An Eternal Duel must occur to resolve who governs the final seal.",
                descriptionTr = "Kule'nin zirvesine ulaştın. Ebedi Çürüme son mühürleri de kırdı. Karşında Çekirdeğin Avatarı duruyor. Tarafın mutlak fedakarlık talep ediyor. Son mührü kimin yöneteceğini belirlemek için Ebedi Düello başlamalı.",
                optionA = GameOption(
                    textEn = "Annihilate with Light & Seal the Core (Triumph Ending)",
                    textTr = "Işık ile Yok Et ve Çekirdeği Mühürle (Zafer Sonu)",
                    alignmentShift = 40,
                    gleamChange = 500,
                    hpChange = -20,
                    journalEn = "You sealed the Core in blinding Light, achieving a glorious Triumph for the Sanctum.",
                    journalTr = "Çekirdeği göz alıcı bir Işıkla mühürledin, Sanctum için şanlı bir Zafer elde ettin."
                ),
                optionB = GameOption(
                    textEn = "Absorb the Void Core and Evolve (Hold Ending)",
                    textTr = "Boşluk Çekirdeğini Em ve Evril (Direnç Sonu)",
                    alignmentShift = -40,
                    pyreChange = 500,
                    hpChange = -20,
                    journalEn = "You absorbed the Blight into your Void Covenant, holding the dark tide by sheer evolutionary dominance.",
                    journalTr = "Musibeti Void Covenant'ına kattın, saf evrimsel üstünlükle karanlık dalgayı bastırdın."
                ),
                optionC = GameOption(
                    textEn = "Shatter the Seals & Free the Outer World (Fracture Ending)",
                    textTr = "Mühürleri Parçala ve Dış Dünyayı Özgür Bırak (Kırılma Sonu)",
                    alignmentShift = 0,
                    goldChange = 1000,
                    hpChange = -80,
                    journalEn = "You broke all seals, letting the outer world face its destiny, falling to a grand Spirit Fracture.",
                    journalTr = "Tüm mühürleri kırdın, dış dünyayı kaderiyle baş başa bıraktın ve büyük bir Ruh Kırılması yaşadın."
                )
            )
            floor % 25 == 0 -> {
                // Exarch Floor: 25, 50, 75
                val num = floor
                FloorScenario(
                    floor = num,
                    titleEn = "Exarch Grand Council (Floor $num)",
                    titleTr = "Büyük Egzark Meclisi (Kat $num)",
                    descriptionEn = "An Exarch is crowned on this floor. High economic and militaristic strategies are debated here. Factions are declaring localized wars to capture the tower sectors. You must declare your stance.",
                    descriptionTr = "Bu katta bir Egzark taçlandırılıyor. Yüksek ekonomik ve askeri stratejiler burada tartışılır. Kule sektörlerini ele geçirmek için yerel savaşlar ilan ediliyor. Duruşunu belirlemelisin.",
                    optionA = GameOption(
                        textEn = "Fund the Celestial Fleet & Impose Order (+Gleam, -Gold)",
                        textTr = "Semavi Filoyu Fonla ve Düzeni Sağla (+Gleam, -Gold)",
                        alignmentShift = 15,
                        goldChange = -100,
                        gleamChange = 150,
                        journalEn = "You funded the White Spires fleet on Floor $num to enforce strict Sanctum regulations.",
                        journalTr = "Beyaz Kuleler filosunu $num. Katta fonlayarak katı Sanctum kurallarını dayattın."
                    ),
                    optionB = GameOption(
                        textEn = "Smuggle Void-infused Weapons to rebels (+Pyre, -Gold)",
                        textTr = "Asilere Boşluk katkılı Silahlar Kaçır (+Pyre, -Gold)",
                        alignmentShift = -15,
                        goldChange = -100,
                        pyreChange = 150,
                        journalEn = "You smuggled forbidden Void armaments on Floor $num to trigger a rebellion.",
                        journalTr = "$num. Katta isyanı tetiklemek için yasaklanmış Boşluk mühimmatı kaçırdın."
                    ),
                    optionC = GameOption(
                        textEn = "Sell Intel to both sides for immense Mercenary profit (+Gold)",
                        textTr = "Muazzam Paralı Asker kârı için iki tarafa da İstihbarat Sat (+Gold)",
                        alignmentShift = 0,
                        goldChange = 300,
                        journalEn = "You played both sides on Floor $num as a shadow broker, amassing clean gold.",
                        journalTr = "Gölgeler simsarı gibi $num. Katta iki tarafı da oynatarak temiz altın biriktirdin."
                    )
                )
            }
            floor % 10 == 0 -> {
                // Arbiter Floor: 10, 20, 30, 40, 60, 70, 80, 90
                val num = floor
                FloorScenario(
                    floor = num,
                    titleEn = "Arbiter's Threshold Trial (Floor $num)",
                    titleTr = "Hakem Eşiği Sınavı (Kat $num)",
                    descriptionEn = "An Arbiter stands in your path, testing your commitment with a trial of blade and spirit. The Blight is concentrated here. Will you force your way, or make a dark offering?",
                    descriptionTr = "Yolunda bir Hakem duruyor ve çelik ile ruh sınavıyla bağlılığını sınıyor. Musibet burada yoğunlaşmış durumda. Yolunu kaba kuvvetle mi açacaksın yoksa karanlık bir adak mı sunacaksın?",
                    optionA = GameOption(
                        textEn = "Invoke the Pure Aegis to smite the Arbiter (-20 HP)",
                        textTr = "Hakemi çarpmak için Saf Aegis'i çağır (-20 HP)",
                        alignmentShift = 20,
                        gleamChange = 100,
                        hpChange = -20,
                        journalEn = "You battled through the Floor $num Arbiter trial with pure Celestial shield.",
                        journalTr = "Saf Semavi kalkan ile $num. Kat Hakem sınavını savaşarak aştın."
                    ),
                    optionB = GameOption(
                        textEn = "Offer a piece of your Soul to bypass the ward (-10 HP, Cruel)",
                        textTr = "Engeli aşmak için Ruhundan bir parça sun (-10 HP, Acımasız)",
                        alignmentShift = -20,
                        pyreChange = 120,
                        hpChange = -10,
                        journalEn = "You fed the Void Covenant's flame on Floor $num by sacrificing raw life force.",
                        journalTr = "Yaralı can enerjini feda ederek $num. Katta Boşluk Ahdi alevini besledin."
                    ),
                    optionC = GameOption(
                        textEn = "Pay heavy bribery gold to buy safe safe-passage (-150 Gold)",
                        textTr = "Güvenli geçiş hakkı satın almak için ağır rüşvet öde (-150 Altın)",
                        alignmentShift = 0,
                        goldChange = -150,
                        journalEn = "You bribed the Arbiter guides on Floor $num to bypass conflict.",
                        journalTr = "Çatışmayı atlamak için $num. Katta Hakem rehberlerine rüşvet verdin."
                    )
                )
            }
            else -> {
                // Standard dynamic floors
                val themeIndex = (floor % 7)
                generateNormalFloor(floor, themeIndex)
            }
        }
    }

    private fun generateNormalFloor(floor: Int, themeIndex: Int): FloorScenario {
        return when (themeIndex) {
            0 -> FloorScenario(
                floor = floor,
                titleEn = "The Blighted Vaults - F$floor",
                titleTr = "Musibetli Mahzenler - K$floor",
                descriptionEn = "Thick purple roots of the Eternal Blight wrap around crumbling stone chests. Skeletons dressed in silver armor lie frozen in prayer.",
                descriptionTr = "Ebedi Çürüme'nin kalın mor kökleri ufalanan taş sandıkları sarıyor. Gümüş zırhlı iskeletler dua ederken donup kalmış.",
                optionA = GameOption(
                    textEn = "Purify the bones and lay them to rest (+Gleam)",
                    textTr = "Kemikleri arındır ve huzura kavuştur (+Gleam)",
                    alignmentShift = 8,
                    gleamChange = 25,
                    journalEn = "On Floor $floor, you consecrated fallen soldiers of the Sanctum.",
                    journalTr = "$floor. Katta Sanctum'un düşmüş askerlerini takdis ettin."
                ),
                optionB = GameOption(
                    textEn = "Drain the residual Blight essence (+Pyre)",
                    textTr = "Kalan Musibet özünü içine çek (+Pyre)",
                    alignmentShift = -8,
                    pyreChange = 30,
                    journalEn = "On Floor $floor, you harvested corrupted Blight nodes to fuel Void rituals.",
                    journalTr = "$floor. Katta Boşluk ritüellerini beslemek için yozlaşmış Musibet özlerini topladın."
                ),
                optionC = GameOption(
                    textEn = "Loot the gold coins from the frozen coffers (+Gold)",
                    textTr = "Donmuş kasalardaki altın sikkeleri yağmala (+Altın)",
                    alignmentShift = 0,
                    goldChange = 50,
                    journalEn = "On Floor $floor, you prioritized material gold over spiritual matters.",
                    journalTr = "$floor. Katta maddi altını ruhani meselelerin üstünde tuttun."
                )
            )
            1 -> FloorScenario(
                floor = floor,
                titleEn = "The Shimmering Mirror - F$floor",
                titleTr = "Işıldayan Ayna - K$floor",
                descriptionEn = "A pool of heavy liquid starlight reflects your true alignment. It whispers your secret class potential and beckons your soul to commit.",
                descriptionTr = "Ağır, sıvı yıldız ışığından bir havuz gerçek hizalanmanı yansıtıyor. Gizli sınıf potansiyelini fısıldayarak ruhunu seçime çağırıyor.",
                optionA = GameOption(
                    textEn = "Kneel and pledge to the White Spires (+Gleam, -10 HP)",
                    textTr = "Diz çök ve Beyaz Kuleler'e bağlılık sözü ver (+Gleam, -10 HP)",
                    alignmentShift = 10,
                    gleamChange = 40,
                    hpChange = -10,
                    journalEn = "On Floor $floor, you knelt before the White Spires mirror pool.",
                    journalTr = "$floor. Katta Beyaz Kuleler ayna havuzunun önünde diz çöktün."
                ),
                optionB = GameOption(
                    textEn = "Shatter the mirror to absorb the cosmic shadow (+Pyre, -10 HP)",
                    textTr = "Kozmik gölgeyi emmek için aynayı tuzla buz et (+Pyre, -10 HP)",
                    alignmentShift = -10,
                    pyreChange = 45,
                    hpChange = -10,
                    journalEn = "On Floor $floor, you shattered the reflective pool, drawing power from its shards.",
                    journalTr = "$floor. Katta yansıtıcı havuzu kırarak parçalarından güç çektin."
                ),
                optionC = GameOption(
                    textEn = "Drink from it to restore physical vitality (+20 HP)",
                    textTr = "Fiziksel canlılığını geri kazanmak için ondan iç (+20 HP)",
                    alignmentShift = 0,
                    hpChange = 20,
                    journalEn = "On Floor $floor, you drank the mystical waters to heal your broken shell.",
                    journalTr = "$floor. Katta kırık kabuğunu iyileştirmek için mistik suları içtin."
                )
            )
            2 -> FloorScenario(
                floor = floor,
                titleEn = "The Ruined Shrine of Sealing - F$floor",
                titleTr = "Yıkık Mühürleme Tapınağı - K$floor",
                descriptionEn = "A broken altar still floats in mid-air, casting flickering golden protection spells. It is slowly collapsing under the pressure of the Blight shadows.",
                descriptionTr = "Yıkık bir sunak hâlâ havada süzülüyor ve titrek altın koruma büyüleri saçıyor. Musibet gölgelerinin baskısı altında yavaşça çöküyor.",
                optionA = GameOption(
                    textEn = "Sacrifice blood to rejuvenate the ward (-15 HP, +Gleam)",
                    textTr = "Efsunu canlandırmak için kan feda et (-15 HP, +Gleam)",
                    alignmentShift = 12,
                    gleamChange = 50,
                    hpChange = -15,
                    journalEn = "On Floor $floor, you sacrificed blood to keep the ancient seals alive.",
                    journalTr = "$floor. Katta antik mühürleri yaşatmak için kanını feda ettin."
                ),
                optionB = GameOption(
                    textEn = "Infect the altar with Void-essence to extract Pyre (+Pyre)",
                    textTr = "Pyre elde etmek için sunağı Boşluk özüyle enfekte et (+Pyre)",
                    alignmentShift = -12,
                    pyreChange = 55,
                    journalEn = "On Floor $floor, you desecrated the sealing altar to fuel your dark covenants.",
                    journalTr = "$floor. Katta karanlık ahitlerini beslemek için mühür sunağını kirlettin."
                ),
                optionC = GameOption(
                    textEn = "Scavenge gold-infused runes from the pillars (+Gold)",
                    textTr = "Sütunlardan altın kaplama rünleri sök (+Altın)",
                    alignmentShift = 0,
                    goldChange = 60,
                    journalEn = "On Floor $floor, you pried rare runes from the ancient walls.",
                    journalTr = "$floor. Katta antik duvarlardaki nadir rünleri söktün."
                )
            )
            3 -> FloorScenario(
                floor = floor,
                titleEn = "The Spires Caravan - F$floor",
                titleTr = "Kuleler Kervanı - K$floor",
                descriptionEn = "Neutral nomadic traders traveling along the safe sectors are selling artifacts. They whisper of a fierce skirmish nearby and are willing to barter or trade info.",
                descriptionTr = "Güvenli sektörlerde seyahat eden tarafsız göçebe tüccarlar eserler satıyor. Yakınlardaki şiddetli bir çatışmayı fısıldayıp takas yapmaya hazırlar.",
                optionA = GameOption(
                    textEn = "Buy Sanctum holy holy relic (-80 Gold, +Gleam)",
                    textTr = "Sanctum kutsal yadigarını satın al (-80 Altın, +Gleam)",
                    alignmentShift = 10,
                    goldChange = -80,
                    gleamChange = 80,
                    journalEn = "On Floor $floor, you purchased a holy relic from the wandering caravan.",
                    journalTr = "$floor. Katta gezgin kervandan kutsal bir yadigar satın aldın."
                ),
                optionB = GameOption(
                    textEn = "Buy Covenant forbidden forbidden grimoire (-80 Gold, +Pyre)",
                    textTr = "Covenant yasaklı kara kitabını satın al (-80 Altın, +Pyre)",
                    alignmentShift = -10,
                    goldChange = -80,
                    pyreChange = 85,
                    journalEn = "On Floor $floor, you acquired a forbidden codex of Void spells.",
                    journalTr = "$floor. Katta yasaklanmış bir Boşluk büyüleri el yazması edindin."
                ),
                optionC = GameOption(
                    textEn = "Sell your old equipment for safety items (+Gold, +10 HP)",
                    textTr = "Güvenlik eşyaları için eski ekipmanını sat (+Altın, +10 HP)",
                    alignmentShift = 0,
                    goldChange = 70,
                    hpChange = 10,
                    journalEn = "On Floor $floor, you traded equipment with merchants and patched up.",
                    journalTr = "$floor. Katta tüccarlarla takas yapıp yaralarını sardın."
                )
            )
            4 -> FloorScenario(
                floor = floor,
                titleEn = "The Weeping Void Outpost - F$floor",
                titleTr = "Ağlayan Boşluk Karakolu - K$floor",
                descriptionEn = "A vanguard outpost of the Void Covenant is in disarray. Dying scouts are muttering about the White Spires purifying squads hunting them down.",
                descriptionTr = "Void Covenant'ın bir öncü karakolu darmadağın olmuş. Ölmekte olan gözcüler, kendilerini avlayan Beyaz Kule arındırma ekiplerinden mırıldanıyor.",
                optionA = GameOption(
                    textEn = "Deliver peaceful mercy-kills to scouts (+Gleam, Spirit alignment)",
                    textTr = "Gözcülere merhamet dolu acısız sonlar sun (+Gleam, Işık)",
                    alignmentShift = 10,
                    gleamChange = 40,
                    journalEn = "On Floor $floor, you offered mercy to the dying Void outcasts.",
                    journalTr = "$floor. Katta ölmekte olan Boşluk sürgünlerine merhamet gösterdin."
                ),
                optionB = GameOption(
                    textEn = "Harvest their life embers to resurrect them as void thralls (+Pyre)",
                    textTr = "Onları boşluk kölesi olarak diriltmek için can közlerini topla (+Pyre)",
                    alignmentShift = -14,
                    pyreChange = 60,
                    journalEn = "On Floor $floor, you animated the dead scouts into mindless thralls of the Covenant.",
                    journalTr = "$floor. Katta ölü gözcüleri akılsız Boşluk kölesi olarak canlandırdın."
                ),
                optionC = GameOption(
                    textEn = "Search their bags for forgotten supply bags (+Gold)",
                    textTr = "Unutulmuş erzak torbaları için çantalarını ara (+Altın)",
                    alignmentShift = 0,
                    goldChange = 80,
                    journalEn = "On Floor $floor, you scavenged abandoned supplies from the warzone.",
                    journalTr = "$floor. Katta savaş alanındaki terk edilmiş malzemeleri yağmaladın."
                )
            )
            5 -> FloorScenario(
                floor = floor,
                titleEn = "The Whispering Fount - F$floor",
                titleTr = "Fısıldayan Çeşme - K$floor",
                descriptionEn = "A natural steam geyser filled with hot mineral water is shimmering. The air smells of sulphur, and whispers of the ancient Sovereign echoes in the mist.",
                descriptionTr = "Sıcak mineral suyuyla dolu doğal bir buhar gayzeri ışıldıyor. Hava kükürt kokuyor ve sisin içinde kadim Hükümdar'ın fısıltıları yankılanıyor.",
                optionA = GameOption(
                    textEn = "Perform an Order cleansing ritual (+Gleam, -10 HP)",
                    textTr = "Düzen temizleme ritüeli gerçekleştir (+Gleam, -10 HP)",
                    alignmentShift = 10,
                    gleamChange = 50,
                    hpChange = -10,
                    journalEn = "On Floor $floor, you sanctied the mineral hotspring.",
                    journalTr = "$floor. Katta mineral kaplıcasını kutsadın."
                ),
                optionB = GameOption(
                    textEn = "Infuse it with Blight blood to unlock power (+Pyre, -10 HP)",
                    textTr = "Güç kilidini açmak için onu Musibet kanıyla aşıla (+Pyre, -10 HP)",
                    alignmentShift = -10,
                    pyreChange = 50,
                    hpChange = -10,
                    journalEn = "On Floor $floor, you tainted the fount to summon chaotic whispers.",
                    journalTr = "$floor. Katta kaotik fısıltıları çağırmak için kaynağı kirlettin."
                ),
                optionC = GameOption(
                    textEn = "Bathe in the hot mineral waters to restore vitality (+30 HP, -20 Gold)",
                    textTr = "Canlılık için sıcak mineral sularında yıkan (+30 HP, -20 Altın)",
                    alignmentShift = 0,
                    goldChange = -20,
                    hpChange = 30,
                    journalEn = "On Floor $floor, you rested in the mineral fount, restoring major strength.",
                    journalTr = "$floor. Katta mineral kaynağında dinlenerek gücünü büyük ölçüde geri kazandın."
                )
            )
            else -> FloorScenario(
                floor = floor,
                titleEn = "The Iron Threshold - F$floor",
                titleTr = "Demir Eşik - K$floor",
                descriptionEn = "Heavy blast doors block the tunnel. Inscriptions demand a tribute of alignment or pure gold to open.",
                descriptionTr = "Ağır zırhlı kapılar tüneli kapatıyor. Kitabeler açılmak için hizalanma veya saf altın haraç talep ediyor.",
                optionA = GameOption(
                    textEn = "Force the iron doors using pure faith (+Gleam, -15 HP)",
                    textTr = "Saf inanç kullanarak demir kapıları zorla (+Gleam, -15 HP)",
                    alignmentShift = 10,
                    gleamChange = 40,
                    hpChange = -15,
                    journalEn = "On Floor $floor, you forced the Iron Threshold with Holy zeal.",
                    journalTr = "$floor. Katta Demir Eşiği Kutsal inanışla zorlayarak açtın."
                ),
                optionB = GameOption(
                    textEn = "Sunder the locks using Void lightning (+Pyre, -15 HP)",
                    textTr = "Boşluk yıldırımıyla kilitleri fırlat (+Pyre, -15 HP)",
                    alignmentShift = -10,
                    pyreChange = 40,
                    hpChange = -15,
                    journalEn = "On Floor $floor, you tore the blast doors down with Void lightning.",
                    journalTr = "$floor. Katta koruyucu kapıları Boşluk yıldırımıyla havaya uçurdun."
                ),
                optionC = GameOption(
                    textEn = "Pay the automated tribute mechanism (-50 Gold)",
                    textTr = "Otomatik haraç mekanizmasına ödeme yap (-50 Altın)",
                    alignmentShift = 0,
                    goldChange = -50,
                    journalEn = "On Floor $floor, you bypassed the gatekeeper's tax using gold.",
                    journalTr = "$floor. Katta kapıcının vergisini altınla ödeyip geçtin."
                )
            )
        }
    }
}
