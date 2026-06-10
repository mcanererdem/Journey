package com.mcanererdem.journey.data.engine

data class GameOption(
    val textEn: String,
    val textTr: String,
    val alignmentShift: Int, // Positive for Sanctum (+), Negative for Void (-)
    val goldChange: Int = 0,
    val aetherChange: Int = 0,
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
                titleTr = "Hükümdar Tahtý: Ebedi Düello",
                descriptionEn = "You have reached the pinnacle of the Tower. The Eternal Blight has breached the final seals. Before you stands the Avatar of the Core. Your side demands the absolute sacrifice. An Eternal Duel must occur to resolve who governs the final seal.",
                descriptionTr = "Kule'nin zirvesine ulaþtýn. Ebedi Çürüme son mühürleri de kýrdý. Karþýnda Çekirdeðin Avatarý duruyor. Tarafýn mutlak fedakarlýk talep ediyor. Son mührü kimin yöneteceðini belirlemek için Ebedi Düello baþlamalý.",
                optionA = GameOption(
                    textEn = "Annihilate with Light & Seal the Core (Triumph Ending)",
                    textTr = "Iþýk ile Yok Et ve Çekirdeði Mühürle (Zafer Sonu)",
                    alignmentShift = 40,
                    aetherChange = 500,
                    hpChange = -20,
                    journalEn = "You sealed the Core in blinding Light, achieving a glorious Triumph for the Sanctum.",
                    journalTr = "Çekirdeði göz alýcý bir Iþýkla mühürledin, Sanctum için þanlý bir Zafer elde ettin."
                ),
                optionB = GameOption(
                    textEn = "Absorb the Void Core and Evolve (Hold Ending)",
                    textTr = "Boþluk Çekirdeðini Em ve Evril (Direnç Sonu)",
                    alignmentShift = -40,
                    aetherChange = 500,
                    hpChange = -20,
                    journalEn = "You absorbed the Blight into your Void Covenant, holding the dark tide by sheer evolutionary dominance.",
                    journalTr = "Musibeti Void Covenant'ýna kattýn, saf evrimsel üstünlükle karanlýk dalgayý bastýrdýn."
                ),
                optionC = GameOption(
                    textEn = "Shatter the Seals & Free the Outer World (Fracture Ending)",
                    textTr = "Mühürleri Parçala ve Dýþ Dünyayý Özgür Býrak (Kýrýlma Sonu)",
                    alignmentShift = 0,
                    goldChange = 1000,
                    hpChange = -80,
                    journalEn = "You broke all seals, letting the outer world face its destiny, falling to a grand Spirit Fracture.",
                    journalTr = "Tüm mühürleri kýrdýn, dýþ dünyayý kaderiyle baþ baþa býraktýn ve büyük bir Ruh Kýrýlmasý yaþadýn."
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
                    descriptionTr = "Bu katta bir Egzark taçlandýrýlýyor. Yüksek ekonomik ve askeri stratejiler burada tartýþýlýr. Kule sektörlerini ele geçirmek için yerel savaþlar ilan ediliyor. Duruþunu belirlemelisin.",
                    optionA = GameOption(
                        textEn = "Fund the Celestial Fleet & Impose Order (+Aether, -Gold)",
                        textTr = "Semavi Filoyu Fonla ve Düzeni Saðla (+Aether, -Gold)",
                        alignmentShift = 15,
                        goldChange = -100,
                        aetherChange = 150,
                        journalEn = "You funded the White Spires fleet on Floor $num to enforce strict Sanctum regulations.",
                        journalTr = "Beyaz Kuleler filosunu $num. Katta fonlayarak katý Sanctum kurallarýný dayattýn."
                    ),
                    optionB = GameOption(
                        textEn = "Smuggle Void-infused Weapons to rebels (+Aether, -Gold)",
                        textTr = "Asilere Boþluk katkýlý Silahlar Kaçýr (+Aether, -Gold)",
                        alignmentShift = -15,
                        goldChange = -100,
                        aetherChange = 150,
                        journalEn = "You smuggled forbidden Void armaments on Floor $num to trigger a rebellion.",
                        journalTr = "$num. Katta isyaný tetiklemek için yasaklanmýþ Boþluk mühimmatý kaçýrdýn."
                    ),
                    optionC = GameOption(
                        textEn = "Sell Intel to both sides for immense Mercenary profit (+Gold)",
                        textTr = "Muazzam Paralý Asker kârý için iki tarafa da Ýstihbarat Sat (+Gold)",
                        alignmentShift = 0,
                        goldChange = 300,
                        journalEn = "You played both sides on Floor $num as a shadow broker, amassing clean gold.",
                        journalTr = "Gölgeler simsarý gibi $num. Katta iki tarafý da oynatarak temiz altýn biriktirdin."
                    )
                )
            }
            floor % 10 == 0 -> {
                // Arbiter Floor: 10, 20, 30, 40, 60, 70, 80, 90
                val num = floor
                FloorScenario(
                    floor = num,
                    titleEn = "Arbiter's Threshold Trial (Floor $num)",
                    titleTr = "Hakem Eþiði Sýnavý (Kat $num)",
                    descriptionEn = "An Arbiter stands in your path, testing your commitment with a trial of blade and spirit. The Blight is concentrated here. Will you force your way, or make a dark offering?",
                    descriptionTr = "Yolunda bir Hakem duruyor ve çelik ile ruh sýnavýyla baðlýlýðýný sýnýyor. Musibet burada yoðunlaþmýþ durumda. Yolunu kaba kuvvetle mi açacaksýn yoksa karanlýk bir adak mý sunacaksýn?",
                    optionA = GameOption(
                        textEn = "Invoke the Pure Aegis to smite the Arbiter (-20 HP)",
                        textTr = "Hakemi çarpmak için Saf Aegis'i çaðýr (-20 HP)",
                        alignmentShift = 20,
                        aetherChange = 100,
                        hpChange = -20,
                        journalEn = "You battled through the Floor $num Arbiter trial with pure Celestial shield.",
                        journalTr = "Saf Semavi kalkan ile $num. Kat Hakem sýnavýný savaþarak aþtýn."
                    ),
                    optionB = GameOption(
                        textEn = "Offer a piece of your Soul to bypass the ward (-10 HP, Cruel)",
                        textTr = "Engeli aþmak için Ruhundan bir parça sun (-10 HP, Acýmasýz)",
                        alignmentShift = -20,
                        aetherChange = 120,
                        hpChange = -10,
                        journalEn = "You fed the Void Covenant's flame on Floor $num by sacrificing raw life force.",
                        journalTr = "Yaralý can enerjini feda ederek $num. Katta Boþluk Ahdi alevini besledin."
                    ),
                    optionC = GameOption(
                        textEn = "Pay heavy bribery gold to buy safe safe-passage (-150 Gold)",
                        textTr = "Güvenli geçiþ hakký satýn almak için aðýr rüþvet öde (-150 Altýn)",
                        alignmentShift = 0,
                        goldChange = -150,
                        journalEn = "You bribed the Arbiter guides on Floor $num to bypass conflict.",
                        journalTr = "Çatýþmayý atlamak için $num. Katta Hakem rehberlerine rüþvet verdin."
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
                descriptionTr = "Ebedi Çürüme'nin kalýn mor kökleri ufalanan taþ sandýklarý sarýyor. Gümüþ zýrhlý iskeletler dua ederken donup kalmýþ.",
                optionA = GameOption(
                    textEn = "Purify the bones and lay them to rest (+Aether)",
                    textTr = "Kemikleri arýndýr ve huzura kavuþtur (+Aether)",
                    alignmentShift = 8,
                    aetherChange = 25,
                    journalEn = "On Floor $floor, you consecrated fallen soldiers of the Sanctum.",
                    journalTr = "$floor. Katta Sanctum'un düþmüþ askerlerini takdis ettin."
                ),
                optionB = GameOption(
                    textEn = "Drain the residual Blight essence (+Aether)",
                    textTr = "Kalan Musibet özünü içine çek (+Aether)",
                    alignmentShift = -8,
                    aetherChange = 30,
                    journalEn = "On Floor $floor, you harvested corrupted Blight nodes to fuel Void rituals.",
                    journalTr = "$floor. Katta Boþluk ritüellerini beslemek için yozlaþmýþ Musibet özlerini topladýn."
                ),
                optionC = GameOption(
                    textEn = "Loot the gold coins from the frozen coffers (+Gold)",
                    textTr = "Donmuþ kasalardaki altýn sikkeleri yaðmala (+Altýn)",
                    alignmentShift = 0,
                    goldChange = 50,
                    journalEn = "On Floor $floor, you prioritized material gold over spiritual matters.",
                    journalTr = "$floor. Katta maddi altýný ruhani meselelerin üstünde tuttun."
                )
            )
            1 -> FloorScenario(
                floor = floor,
                titleEn = "The Shimmering Mirror - F$floor",
                titleTr = "Iþýldayan Ayna - K$floor",
                descriptionEn = "A pool of heavy liquid starlight reflects your true alignment. It whispers your secret class potential and beckons your soul to commit.",
                descriptionTr = "Aðýr, sývý yýldýz ýþýðýndan bir havuz gerçek hizalanmaný yansýtýyor. Gizli sýnýf potansiyelini fýsýldayarak ruhunu seçime çaðýrýyor.",
                optionA = GameOption(
                    textEn = "Kneel and pledge to the White Spires (+Aether, -10 HP)",
                    textTr = "Diz çök ve Beyaz Kuleler'e baðlýlýk sözü ver (+Aether, -10 HP)",
                    alignmentShift = 10,
                    aetherChange = 40,
                    hpChange = -10,
                    journalEn = "On Floor $floor, you knelt before the White Spires mirror pool.",
                    journalTr = "$floor. Katta Beyaz Kuleler ayna havuzunun önünde diz çöktün."
                ),
                optionB = GameOption(
                    textEn = "Shatter the mirror to absorb the cosmic shadow (+Aether, -10 HP)",
                    textTr = "Kozmik gölgeyi emmek için aynayý tuzla buz et (+Aether, -10 HP)",
                    alignmentShift = -10,
                    aetherChange = 45,
                    hpChange = -10,
                    journalEn = "On Floor $floor, you shattered the reflective pool, drawing power from its shards.",
                    journalTr = "$floor. Katta yansýtýcý havuzu kýrarak parçalarýndan güç çektin."
                ),
                optionC = GameOption(
                    textEn = "Drink from it to restore physical vitality (+20 HP)",
                    textTr = "Fiziksel canlýlýðýný geri kazanmak için ondan iç (+20 HP)",
                    alignmentShift = 0,
                    hpChange = 20,
                    journalEn = "On Floor $floor, you drank the mystical waters to heal your broken shell.",
                    journalTr = "$floor. Katta kýrýk kabuðunu iyileþtirmek için mistik sularý içtin."
                )
            )
            2 -> FloorScenario(
                floor = floor,
                titleEn = "The Ruined Shrine of Sealing - F$floor",
                titleTr = "Yýkýk Mühürleme Tapýnaðý - K$floor",
                descriptionEn = "A broken altar still floats in mid-air, casting flickering golden protection spells. It is slowly collapsing under the pressure of the Blight shadows.",
                descriptionTr = "Yýkýk bir sunak hâlâ havada süzülüyor ve titrek altýn koruma büyüleri saçýyor. Musibet gölgelerinin baskýsý altýnda yavaþça çöküyor.",
                optionA = GameOption(
                    textEn = "Sacrifice blood to rejuvenate the ward (-15 HP, +Aether)",
                    textTr = "Efsunu canlandýrmak için kan feda et (-15 HP, +Aether)",
                    alignmentShift = 12,
                    aetherChange = 50,
                    hpChange = -15,
                    journalEn = "On Floor $floor, you sacrificed blood to keep the ancient seals alive.",
                    journalTr = "$floor. Katta antik mühürleri yaþatmak için kanýný feda ettin."
                ),
                optionB = GameOption(
                    textEn = "Infect the altar with Void-essence to extract Pyre (+Aether)",
                    textTr = "Pyre elde etmek için sunaðý Boþluk özüyle enfekte et (+Aether)",
                    alignmentShift = -12,
                    aetherChange = 55,
                    journalEn = "On Floor $floor, you desecrated the sealing altar to fuel your dark covenants.",
                    journalTr = "$floor. Katta karanlýk ahitlerini beslemek için mühür sunaðýný kirlettin."
                ),
                optionC = GameOption(
                    textEn = "Scavenge gold-infused runes from the pillars (+Gold)",
                    textTr = "Sütunlardan altýn kaplama rünleri sök (+Altýn)",
                    alignmentShift = 0,
                    goldChange = 60,
                    journalEn = "On Floor $floor, you pried rare runes from the ancient walls.",
                    journalTr = "$floor. Katta antik duvarlardaki nadir rünleri söktün."
                )
            )
            3 -> FloorScenario(
                floor = floor,
                titleEn = "The Spires Caravan - F$floor",
                titleTr = "Kuleler Kervaný - K$floor",
                descriptionEn = "Neutral nomadic traders traveling along the safe sectors are selling artifacts. They whisper of a fierce skirmish nearby and are willing to barter or trade info.",
                descriptionTr = "Güvenli sektörlerde seyahat eden tarafsýz göçebe tüccarlar eserler satýyor. Yakýnlardaki þiddetli bir çatýþmayý fýsýldayýp takas yapmaya hazýrlar.",
                optionA = GameOption(
                    textEn = "Buy Sanctum holy holy relic (-80 Gold, +Aether)",
                    textTr = "Sanctum kutsal yadigarýný satýn al (-80 Altýn, +Aether)",
                    alignmentShift = 10,
                    goldChange = -80,
                    aetherChange = 80,
                    journalEn = "On Floor $floor, you purchased a holy relic from the wandering caravan.",
                    journalTr = "$floor. Katta gezgin kervandan kutsal bir yadigar satýn aldýn."
                ),
                optionB = GameOption(
                    textEn = "Buy Covenant forbidden forbidden grimoire (-80 Gold, +Aether)",
                    textTr = "Covenant yasaklý kara kitabýný satýn al (-80 Altýn, +Aether)",
                    alignmentShift = -10,
                    goldChange = -80,
                    aetherChange = 85,
                    journalEn = "On Floor $floor, you acquired a forbidden codex of Void spells.",
                    journalTr = "$floor. Katta yasaklanmýþ bir Boþluk büyüleri el yazmasý edindin."
                ),
                optionC = GameOption(
                    textEn = "Sell your old equipment for safety items (+Gold, +10 HP)",
                    textTr = "Güvenlik eþyalarý için eski ekipmanýný sat (+Altýn, +10 HP)",
                    alignmentShift = 0,
                    goldChange = 70,
                    hpChange = 10,
                    journalEn = "On Floor $floor, you traded equipment with merchants and patched up.",
                    journalTr = "$floor. Katta tüccarlarla takas yapýp yaralarýný sardýn."
                )
            )
            4 -> FloorScenario(
                floor = floor,
                titleEn = "The Weeping Void Outpost - F$floor",
                titleTr = "Aðlayan Boþluk Karakolu - K$floor",
                descriptionEn = "A vanguard outpost of the Void Covenant is in disarray. Dying scouts are muttering about the White Spires purifying squads hunting them down.",
                descriptionTr = "Void Covenant'ýn bir öncü karakolu darmadaðýn olmuþ. Ölmekte olan gözcüler, kendilerini avlayan Beyaz Kule arýndýrma ekiplerinden mýrýldanýyor.",
                optionA = GameOption(
                    textEn = "Deliver peaceful mercy-kills to scouts (+Aether, Spirit alignment)",
                    textTr = "Gözcülere merhamet dolu acýsýz sonlar sun (+Aether, Iþýk)",
                    alignmentShift = 10,
                    aetherChange = 40,
                    journalEn = "On Floor $floor, you offered mercy to the dying Void outcasts.",
                    journalTr = "$floor. Katta ölmekte olan Boþluk sürgünlerine merhamet gösterdin."
                ),
                optionB = GameOption(
                    textEn = "Harvest their life embers to resurrect them as void thralls (+Aether)",
                    textTr = "Onlarý boþluk kölesi olarak diriltmek için can közlerini topla (+Aether)",
                    alignmentShift = -14,
                    aetherChange = 60,
                    journalEn = "On Floor $floor, you animated the dead scouts into mindless thralls of the Covenant.",
                    journalTr = "$floor. Katta ölü gözcüleri akýlsýz Boþluk kölesi olarak canlandýrdýn."
                ),
                optionC = GameOption(
                    textEn = "Search their bags for forgotten supply bags (+Gold)",
                    textTr = "Unutulmuþ erzak torbalarý için çantalarýný ara (+Altýn)",
                    alignmentShift = 0,
                    goldChange = 80,
                    journalEn = "On Floor $floor, you scavenged abandoned supplies from the warzone.",
                    journalTr = "$floor. Katta savaþ alanýndaki terk edilmiþ malzemeleri yaðmaladýn."
                )
            )
            5 -> FloorScenario(
                floor = floor,
                titleEn = "The Whispering Fount - F$floor",
                titleTr = "Fýsýldayan Çeþme - K$floor",
                descriptionEn = "A natural steam geyser filled with hot mineral water is shimmering. The air smells of sulphur, and whispers of the ancient Sovereign echoes in the mist.",
                descriptionTr = "Sýcak mineral suyuyla dolu doðal bir buhar gayzeri ýþýldýyor. Hava kükürt kokuyor ve sisin içinde kadim Hükümdar'ýn fýsýltýlarý yankýlanýyor.",
                optionA = GameOption(
                    textEn = "Perform an Order cleansing ritual (+Aether, -10 HP)",
                    textTr = "Düzen temizleme ritüeli gerçekleþtir (+Aether, -10 HP)",
                    alignmentShift = 10,
                    aetherChange = 50,
                    hpChange = -10,
                    journalEn = "On Floor $floor, you sanctied the mineral hotspring.",
                    journalTr = "$floor. Katta mineral kaplýcasýný kutsadýn."
                ),
                optionB = GameOption(
                    textEn = "Infuse it with Blight blood to unlock power (+Aether, -10 HP)",
                    textTr = "Güç kilidini açmak için onu Musibet kanýyla aþýla (+Aether, -10 HP)",
                    alignmentShift = -10,
                    aetherChange = 50,
                    hpChange = -10,
                    journalEn = "On Floor $floor, you tainted the fount to summon chaotic whispers.",
                    journalTr = "$floor. Katta kaotik fýsýltýlarý çaðýrmak için kaynaðý kirlettin."
                ),
                optionC = GameOption(
                    textEn = "Bathe in the hot mineral waters to restore vitality (+30 HP, -20 Gold)",
                    textTr = "Canlýlýk için sýcak mineral sularýnda yýkan (+30 HP, -20 Altýn)",
                    alignmentShift = 0,
                    goldChange = -20,
                    hpChange = 30,
                    journalEn = "On Floor $floor, you rested in the mineral fount, restoring major strength.",
                    journalTr = "$floor. Katta mineral kaynaðýnda dinlenerek gücünü büyük ölçüde geri kazandýn."
                )
            )
            else -> FloorScenario(
                floor = floor,
                titleEn = "The Iron Threshold - F$floor",
                titleTr = "Demir Eþik - K$floor",
                descriptionEn = "Heavy blast doors block the tunnel. Inscriptions demand a tribute of alignment or pure gold to open.",
                descriptionTr = "Aðýr zýrhlý kapýlar tüneli kapatýyor. Kitabeler açýlmak için hizalanma veya saf altýn haraç talep ediyor.",
                optionA = GameOption(
                    textEn = "Force the iron doors using pure faith (+Aether, -15 HP)",
                    textTr = "Saf inanç kullanarak demir kapýlarý zorla (+Aether, -15 HP)",
                    alignmentShift = 10,
                    aetherChange = 40,
                    hpChange = -15,
                    journalEn = "On Floor $floor, you forced the Iron Threshold with Holy zeal.",
                    journalTr = "$floor. Katta Demir Eþiði Kutsal inanýþla zorlayarak açtýn."
                ),
                optionB = GameOption(
                    textEn = "Sunder the locks using Void lightning (+Aether, -15 HP)",
                    textTr = "Boþluk yýldýrýmýyla kilitleri fýrlat (+Aether, -15 HP)",
                    alignmentShift = -10,
                    aetherChange = 40,
                    hpChange = -15,
                    journalEn = "On Floor $floor, you tore the blast doors down with Void lightning.",
                    journalTr = "$floor. Katta koruyucu kapýlarý Boþluk yýldýrýmýyla havaya uçurdun."
                ),
                optionC = GameOption(
                    textEn = "Pay the automated tribute mechanism (-50 Gold)",
                    textTr = "Otomatik haraç mekanizmasýna ödeme yap (-50 Altýn)",
                    alignmentShift = 0,
                    goldChange = -50,
                    journalEn = "On Floor $floor, you bypassed the gatekeeper's tax using gold.",
                    journalTr = "$floor. Katta kapýcýnýn vergisini altýnla ödeyip geçtin."
                )
            )
        }
    }
}

