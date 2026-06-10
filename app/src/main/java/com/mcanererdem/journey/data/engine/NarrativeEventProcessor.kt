package com.mcanererdem.journey.data.engine

import com.mcanererdem.journey.data.model.PlayerProfile

/**
 * Branching Choice representation in the Narrative Event Processor.
 */
data class NarrativeBranchOption(
    val id: String,
    val textEn: String,
    val textTr: String,
    val alignmentImpact: Int = 0,
    val goldChange: Int = 0,
    val expReward: Int = 0,
    val aetherChange: Int = 0,
    val hpChange: Int = 0,
    val itemReward: String = "",
    val titleReward: String = "",
    val outcomeEn: String,
    val outcomeTr: String,
    val nextBranchId: String? = null,
    val triggersSecretBossId: String? = null
)

/**
 * Story narrative event with pre-requisite conditions.
 */
data class NarrativeEvent(
    val id: String,
    val titleEn: String,
    val titleTr: String,
    val descriptionEn: String,
    val descriptionTr: String,
    val preconditionDescEn: String,
    val preconditionDescTr: String,
    val checkPreconditions: (PlayerProfile) -> Boolean,
    val options: List<NarrativeBranchOption>
)

/**
 * Secret Boss Encounter with distinct unlock rules and high reward payouts.
 */
data class SecretBossEncounter(
    val id: String,
    val nameEn: String,
    val nameTr: String,
    val hp: Int,
    val atk: Int,
    val descriptionEn: String,
    val descriptionTr: String,
    val unlockRequirementEn: String,
    val unlockRequirementTr: String,
    val checkUnlock: (PlayerProfile) -> Boolean,
    val rewardGold: Int,
    val rewardAether: Int,
    val rewardItem: String = ""
)

object NarrativeEventProcessor {

    val events = listOf(
        NarrativeEvent(
            id = "obelisk_whispers",
            titleEn = "The Singing Chrono-Obelisk",
            titleTr = "Şarkı Söyleyen Zaman Dikilitaşı",
            descriptionEn = "A pulsating obelisk of solid obsidian hums with alien spatial energy. It speaks in branching paths directly to your mind.",
            descriptionTr = "Katı obsidyenden yapılmış nabız gibi atan bir dikilitaş, yabancı boyutsal enerjiyle mırıldanıyor. Doğrudan zihninize ulaşan dallanıp budaklanan yollar fısıldıyor.",
            preconditionDescEn = "Character Level >= 2 and Momentum is close to Neutral (30 to 70)",
            preconditionDescTr = "Karakter Seviyesi >= 2 ve Momentum Kararlı Nötr olmalı (30 ile 70 arası)",
            checkPreconditions = { it.level >= 2 && it.momentum in 30..70 },
            options = listOf(
                NarrativeBranchOption(
                    id = "obelisk_opt_light",
                    textEn = "Tune the frequency to the Holy Sunbeams (+15 Momentum, +30 Aether)",
                    textTr = "Frekansı Kutsal Güneş Işınlarına uyarla (+15 Momentum, +30 Aether)",
                    alignmentImpact = 15,
                    aetherChange = 30,
                    expReward = 50,
                    outcomeEn = "The obelisk projects a warm blinding beam, purificating your physical aura.",
                    outcomeTr = "Dikilitaş sıcak, göz kamaştırıcı bir ışın yayarak fiziksel auranızı arındırıyor ve kutsuyor."
                ),
                NarrativeBranchOption(
                    id = "obelisk_opt_void",
                    textEn = "Shatter the crystal to harness pure Void resonance (-15 Momentum, +30 Aether)",
                    textTr = "Kristali parçala ve saf Boşluk tınısını çek (-15 Momentum, +30 Aether)",
                    alignmentImpact = -15,
                    aetherChange = 30,
                    hpChange = -5,
                    expReward = 55,
                    outcomeEn = "Sharp dark shards slice your palms, but the flowing chaotic feedback fuels your inner pyre.",
                    outcomeTr = "Keskin kara parçaları avuçlarınızı kesiyor fakat ruhunuza akan kaotik dalga içinizdeki ateşi harlıyor."
                ),
                NarrativeBranchOption(
                    id = "obelisk_opt_scholarly",
                    textEn = "Patiently decode the glyphs (+15 Gold, +40 EXP, +1 Will)",
                    textTr = "Sabırla sembollerin şifresini çöz (+15 Altın, +40 Tecrübe, +1 İrade)",
                    alignmentImpact = 0,
                    goldChange = 15,
                    expReward = 80,
                    outcomeEn = "You decode a spatial shortcut formula, preserving your willpower and gaining valuable ancient wisdom.",
                    outcomeTr = "Mekansal bir kısayol formülünü çözerek iradenizi korudunuz ve değerli kadim bilgelikler elde ettiniz."
                )
            )
        ),
        NarrativeEvent(
            id = "shadow_bazaar",
            titleEn = "Smuggler's Underbelly Bazaar",
            titleTr = "Karanlık Gölgeler Pazarı",
            descriptionEn = "Deep inside structural chambers, a covert smuggler broker offers rare, illegal dimensional bypass seals.",
            descriptionTr = "Yapısal odaların derinliklerinde, gizli bir kaçakçı simsarı nadir bulunan, illegal çeper mühürleri satıyor.",
            preconditionDescEn = "Gold >= 150 and Momentum <= 35 (Void leaning development)",
            preconditionDescTr = "Altın >= 150 ve Momentum Boşluk yöneliminde olmalı (<= 35)",
            checkPreconditions = { it.gold >= 150 && it.momentum <= 35 },
            options = listOf(
                NarrativeBranchOption(
                    id = "bazaar_opt_buy",
                    textEn = "Purchase Cursed Seal (-100 Gold, -15 HP, +Item)",
                    textTr = "Lanetli Geçit Mührünü Satın Al (-100 Altın, -15 HP, +Eşya)",
                    goldChange = -100,
                    hpChange = -15,
                    itemReward = "Cursed Abyssal Eye",
                    outcomeEn = "The heavy emblem feels freezing cold, unlocking forbidden deep-sea abyss vision.",
                    outcomeTr = "Ağır amblem dondurucu derecede soğuk hissettiriyor, yasaklanmış derin deniz uçurum vizyonunu açtı."
                ),
                NarrativeBranchOption(
                    id = "bazaar_opt_betray",
                    textEn = "Report him to Sanctum Templars (+20 Momentum, +40 Aether)",
                    textTr = "Onu Sanctum Tapınakçılarına bildir (+20 Momentum, +40 Aether)",
                    alignmentImpact = 20,
                    aetherChange = 40,
                    expReward = 60,
                    outcomeEn = "Sanctum paladins raid the outpost. The broker retreats but leaves a reward purse behind.",
                    outcomeTr = "Sanctum şövalyeleri karakola baskın düzenledi. Simsar kaçtı ama ödül dolu bir keseyi geride bıraktı."
                )
            )
        ),
        NarrativeEvent(
            id = "celestial_solstice",
            titleEn = "The Grand Celestial Sun-Altar",
            titleTr = "Yüce Semavi Güneş Mihrabı",
            descriptionEn = "A holy beacon reflects stellar radiation. A voice demands a symbolic blood pact in exchange for eternal ascension.",
            descriptionTr = "Kutsal bir fener yıldız radyasyonunu yansıtıyor. Bir ses, ebedi yükseliş karşılığında sembolik bir kan anlaşması talep ediyor.",
            preconditionDescEn = "Momentum >= 80 (Celestial leaning development) and Player Level >= 3",
            preconditionDescTr = "Momentum Semavi nizamda (>= 80) ve Karakter Seviyesi >= 3 olmalı",
            checkPreconditions = { it.momentum >= 80 && it.level >= 3 },
            options = listOf(
                NarrativeBranchOption(
                    id = "sun_opt_blood",
                    textEn = "Drip blood onto solar flames (-20 HP, +120 EXP, +Title)",
                    textTr = "Güneş alevlerine kan damlat (-20 HP, +120 Tecrübe, +Unvan)",
                    hpChange = -20,
                    expReward = 120,
                    titleReward = "Sunforged Harbinger",
                    outcomeEn = "Your body burns with divine vigor, forging a sunfire seal on your heart.",
                    outcomeTr = "Vücudunuz ilahi bir güçle yanıyor, kalbinizin üzerinde güneş ateşinden bir mühür yapılıyor."
                ),
                NarrativeBranchOption(
                    id = "sun_opt_meditate",
                    textEn = "Meditate near the heat waves (+10 Momentum, +3 Will)",
                    textTr = "Isı dalgalarının yanında meditasyon yap (+10 Momentum, +3 İrade)",
                    alignmentImpact = 10,
                    outcomeEn = "The cosmic heat purges physical toxins and restores mental focus.",
                    outcomeTr = "Kozmik sıcaklık fiziksel toksinlerinizi temizler ve zihinsel odağınızı geri kazandırır."
                )
            )
        )
    )

    val secretBosses = listOf(
        SecretBossEncounter(
            id = "abyssal_beast_boss",
            nameEn = "Kharon, Sovereign Shadow Devourer",
            nameTr = "Gölge Yutan Kadim canavar Kharon",
            hp = 550,
            atk = 32,
            descriptionEn = "A colossal nightmare bound by raw shadow. He claims souls fractured by excessive tower restarts and dark oaths.",
            descriptionTr = "Ham gölgeler arasından fırlayan devasa bir kabus. Çok sayıda zaman döngüsünde ruhları ufalanmış olan kurbanları yutmaya gelir.",
            unlockRequirementEn = "Momentum <= 10 (Deep Void Affinity) and total Spirit Fractures >= 2",
            unlockRequirementTr = "Momentum <= 10 (Derin Boşluk) ve Toplam Ruh Kırılması >= 2 olmalı",
            checkUnlock = { it.momentum <= 10 && it.totalFractures >= 2 },
            rewardGold = 250,
            rewardAether = 100,
            rewardItem = "Voidreaver Edge Plate"
        ),
        SecretBossEncounter(
            id = "celestial_avatar_boss",
            nameEn = "Uriel, Archon of Radiant Truth",
            nameTr = "Işığın Görkemli Başmeleği Uriel",
            hp = 600,
            atk = 28,
            descriptionEn = "The absolute herald of white sunbeams. He descends to challenge the pure ones, examining if combat steel matches structural faith.",
            descriptionTr = "Beyaz güneş ışınlarının mutlak elçisi. Saf inanç sahiplerine meydan okumak için iner, çeliğin inançlarıyla eşleşip eşleşmediğini sınar.",
            unlockRequirementEn = "Momentum >= 100 (Absolute Celestial Devotion) and Character Level >= 5",
            unlockRequirementTr = "Momentum >= 100 (Mutlak Semavi Bağlılık) ve Seviye >= 5 olmalı",
            checkUnlock = { it.momentum >= 100 && it.level >= 5 },
            rewardGold = 200,
            rewardAether = 100,
            rewardItem = "Sunspire Crest Seal"
        ),
        SecretBossEncounter(
            id = "sentinel_relic_boss",
            nameEn = "Arch-Sentinel Chrono-Golem",
            nameTr = "Başmuhafız Zaman Dev Obeliski",
            hp = 700,
            atk = 24,
            descriptionEn = "A robotic guardian activated when high amounts of ancient items and wealth resonate with floor coordinates.",
            descriptionTr = "Değerli antik eşyalar ve yüksek miktarda zenginlik kule koordinatlarıyla tınlaştığında canlanan robotik eski muhafız.",
            unlockRequirementEn = "Player Gold >= 250 and carries at least 2 key items (itemsEncoded has contents)",
            unlockRequirementTr = "Karakter Altını >= 250 ve en az 2 adet tescilli ekipmana sahip olmalı",
            checkUnlock = { it.gold >= 250 && it.itemsEncoded.split(",").filter { i -> i.isNotBlank() }.size >= 2 },
            rewardGold = 400,
            rewardAether = 100,
            rewardItem = "Chrono-Core Fragment"
        )
    )

    /**
     * Evaluates active events based on player's current progression profile.
     */
    fun getAvailableEvents(player: PlayerProfile): List<NarrativeEvent> {
        return events.filter { it.checkPreconditions(player) }
    }

    /**
     * Evaluates active secret bosses based on player's current progression profile.
     */
    fun getAvailableSecretBosses(player: PlayerProfile): List<SecretBossEncounter> {
        return secretBosses.filter { it.checkUnlock(player) }
    }

    /**
     * Process selection choice inside a Narrative Branch and award immediate payouts.
     */
    fun processNarrativeChoice(player: PlayerProfile, choice: NarrativeBranchOption): PlayerProfile {
        val newMomentum = (player.momentum + choice.alignmentImpact).coerceIn(0, 100)
        val newGold = (player.gold + choice.goldChange).coerceAtLeast(0)
        val newAether = (player.aether + choice.aetherChange).coerceAtLeast(0)
        
        var newHp = player.currentHp + choice.hpChange
        if (newHp < 1 && choice.hpChange < 0) {
            newHp = 1 // Prevent narrative death from simple choices, leave at critical 1 HP
        }

        // Apply item reward
        var currentItems = if (player.itemsEncoded.isEmpty()) emptyList() else player.itemsEncoded.split(",")
        if (choice.itemReward.isNotEmpty() && !currentItems.contains(choice.itemReward)) {
            currentItems = currentItems + choice.itemReward
        }
        val newItemsEncoded = currentItems.filter { it.isNotBlank() }.joinToString(",")

        // Apply title reward
        var currentTitles = if (player.titlesEncoded.isEmpty()) emptyList() else player.titlesEncoded.split(",")
        if (choice.titleReward.isNotEmpty() && !currentTitles.contains(choice.titleReward)) {
            currentTitles = currentTitles + choice.titleReward
        }
        val newTitlesEncoded = currentTitles.filter { it.isNotBlank() }.joinToString(",")

        // Apply EXP & Leveling up logic
        var newExp = player.exp + choice.expReward
        var newLevel = player.level
        var newMaxExp = player.maxExp
        var newMaxHp = player.maxHp
        while (newExp >= newMaxExp && newLevel < 100) {
            newExp -= newMaxExp
            newLevel++
            newMaxExp = newLevel * 100
            newMaxHp += 20
            newHp += 20
        }

        val activeFactionSide = if (player.side == "NEUTRAL" && choice.alignmentImpact != 0) {
            if (choice.alignmentImpact > 0 && newMomentum > 70) "SANCTUM"
            else if (choice.alignmentImpact < 0 && newMomentum < 30) "COVENANT"
            else "NEUTRAL"
        } else {
            player.side
        }

        return player.copy(
            momentum = newMomentum,
            gold = newGold,
            aether = newAether,
            currentHp = newHp.coerceAtMost(newMaxHp),
            maxHp = newMaxHp,
            itemsEncoded = newItemsEncoded,
            titlesEncoded = newTitlesEncoded,
            level = newLevel,
            exp = newExp,
            maxExp = newMaxExp,
            side = activeFactionSide,
            lastUpdated = System.currentTimeMillis()
        )
    }
}

