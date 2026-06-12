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
                titleTr = "Hükümdar Tahtı: Ebedi Düello",
                descriptionEn = "You have reached the pinnacle of the Tower. The Eternal Blight has breached the final seals. Before you stands the Avatar of the Core. Your side demands the absolute sacrifice. An Eternal Duel must occur to resolve who governs the final seal.",
                descriptionTr = "Kule'nin zirvesine ulaştın. Ebedi Çürüme son mühürleri de kırdı. Karşında Çekirdeğin Avatarı duruyor. Tarafın mutlak fedakarlık talep ediyor. Son mührü kimin yöneteceğini belirlemek için Ebedi Düello başlamalı.",
                optionA = GameOption(
                    textEn = "Annihilate with Light & Seal the Core (Triumph Ending)",
                    textTr = "Işık ile Yok Et ve Çekirdeği Mühürle (Zafer Sonu)",
                    alignmentShift = 40,
                    aetherChange = 500,
                    hpChange = -20,
                    journalEn = "You sealed the Core in blinding Light, achieving a glorious Triumph for the Sanctum.",
                    journalTr = "Çekirdeği göz alıcı bir Işıkla mühürledin, Sanctum için şanlı bir Zafer elde ettin."
                ),
                optionB = GameOption(
                    textEn = "Absorb the Void Core and Evolve (Hold Ending)",
                    textTr = "Boşluk Çekirdeğini Em ve Evril (Direnç Sonu)",
                    alignmentShift = -40,
                    aetherChange = 500,
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
                val num = floor
                FloorScenario(
                    floor = num,
                    titleEn = "Exarch Grand Council (Floor $num)",
                    titleTr = "Büyük Egzark Meclisi (Kat $num)",
                    descriptionEn = "An Exarch is crowned on this floor. High economic and militaristic strategies are debated here.",
                    descriptionTr = "Bu katta bir Egzark taçlandırılıyor. Yüksek ekonomik ve askeri stratejiler burada tartışılır.",
                    optionA = GameOption(
                        textEn = "Fund the Celestial Fleet (+Aether, -Gold)",
                        textTr = "Semavi Filoyu Fonla (+Aether, -Gold)",
                        alignmentShift = 15,
                        goldChange = -100,
                        aetherChange = 150,
                        journalEn = "Funded the White Spires fleet on Floor $num.",
                        journalTr = "Beyaz Kuleler filosunu $num. Katta fonladın."
                    ),
                    optionB = GameOption(
                        textEn = "Smuggle Void Weapons (+Aether, -Gold)",
                        textTr = "Boşluk Silahları Kaçır (+Aether, -Gold)",
                        alignmentShift = -15,
                        goldChange = -100,
                        aetherChange = 150,
                        journalEn = "Smuggled Void armaments on Floor $num.",
                        journalTr = "$num. Katta Boşluk mühimmatı kaçırdın."
                    ),
                    optionC = GameOption(
                        textEn = "Sell Intel (+Gold)",
                        textTr = "İstihbarat Sat (+Gold)",
                        alignmentShift = 0,
                        goldChange = 300,
                        journalEn = "Played both sides on Floor $num.",
                        journalTr = "$num. Katta iki tarafı da oynattın."
                    )
                )
            }
            else -> {
                val bracketIndex = ((floor - 1) / 10).coerceIn(0, 9)
                generateNormalFloor(floor, bracketIndex)
            }
        }
    }

    private fun generateNormalFloor(floor: Int, bracketIndex: Int): FloorScenario {
        return when (bracketIndex) {
            0 -> FloorScenario(
                floor = floor,
                titleEn = "The Blighted Foothills - F$floor",
                titleTr = "Musibetli Yamaçlar - K$floor",
                descriptionEn = "The base of the tower is thick with pulsating roots.",
                descriptionTr = "Kulenin temeli zonklayan köklerle kaplı.",
                optionA = GameOption("Purify roots", "Kökleri arındır", 8, aetherChange = 25, journalEn = "Purified roots on F$floor", journalTr = "Kökleri arındırdın"),
                optionB = GameOption("Absorb blight", "Musibeti em", -8, aetherChange = 30, journalEn = "Absorbed blight on F$floor", journalTr = "Musibeti emdin"),
                optionC = GameOption("Loot area", "Bölgeyi yağmala", 0, goldChange = 50, journalEn = "Looted area on F$floor", journalTr = "Bölgeyi yağmaladın")
            )
            // ... truncated for brevity, but matching the structure
            else -> FloorScenario(
                floor = floor,
                titleEn = "Tower Ascent - F$floor",
                titleTr = "Kule Yükselişi - K$floor",
                descriptionEn = "Continuing the climb through the tower's mysterious halls.",
                descriptionTr = "Kulenin gizemli salonlarında tırmanışa devam ediliyor.",
                optionA = GameOption("Focus light", "Işığa odaklan", 10, aetherChange = 40, journalEn = "Focused light on F$floor", journalTr = "Işığa odaklandın"),
                optionB = GameOption("Embrace shadow", "Gölgeyi kucakla", -10, aetherChange = 40, journalEn = "Embraced shadow on F$floor", journalTr = "Gölgeyi kucakladın"),
                optionC = GameOption("Search for gold", "Altın ara", 0, goldChange = 50, journalEn = "Searched for gold on F$floor", journalTr = "Altın aradın")
            )
        }
    }
}
