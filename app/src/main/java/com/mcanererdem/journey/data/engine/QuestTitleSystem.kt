package com.mcanererdem.journey.data.engine

import com.mcanererdem.journey.data.model.PlayerProfile

/**
 * Definition of a Title with preconditions, descriptions and stats indicators.
 */
data class TitleDef(
    val id: String,
    val nameEn: String,
    val nameTr: String,
    val descEn: String,
    val descTr: String,
    val isHidden: Boolean,
    val requirementDescEn: String,
    val requirementDescTr: String,
    val hpBonus: Int = 0,
    val goldBonusPercent: Int = 0,
    val meetsPreconditions: (PlayerProfile) -> Boolean
)

/**
 * Quest Category types mapped to requested categories.
 */
enum class QuestType {
    MAIN,
    SIDE,
    NORMAL,
    SPECIAL,
    CHAIN,
    HIDDEN,
    EVENT
}

/**
 * Status tracking values for Quests as defined in requested specifications.
 */
enum class QuestStatusType {
    LOCKED,     // Prerequisites or preconditions are not met
    ACTIVE,     // Preconditions met, task is currently active/in-progress
    COMPLETED,  // Completed and rewards have been claimed
    HIDDEN      // Completely mystified from the quest log boards until unlocked
}

/**
 * Definition of a Quest with prerequisite check, preconditions met check, state verification, and rewards.
 */
data class QuestDef(
    val id: String,
    val type: QuestType,
    val titleEn: String,
    val titleTr: String,
    val descEn: String,
    val descTr: String,
    val requirementEn: String,
    val requirementTr: String,
    val prerequisiteQuestId: String? = null,
    val meetsPreconditions: (PlayerProfile) -> Boolean = { true },
    val checkProgress: (PlayerProfile) -> Boolean,
    val rewardGold: Int = 0,
    val rewardExp: Int = 0,
    val rewardItem: String? = null,
    val rewardTitle: String? = null,
    val rewardAether: Int = 0
)

/**
 * Engine Registry tracking all available Titles & Quests in Light & Darkness.
 */
object QuestTitleSystem {

    val titles = listOf(
        TitleDef(
            id = "lightseeker",
            nameEn = "Zealous Lightseeker ✨",
            nameTr = "Gayretli Işık Arayıcısı ✨",
            descEn = "You are driven by the pure, unyielding radiant beam of the Sanctum.",
            descTr = "Sizi ileri taşıyan güç Semavi'nin saf ve boyun eğmez kutsal radyasyonudur.",
            isHidden = false,
            requirementDescEn = "Reach Level 3 & Momentum 70 minimum",
            requirementDescTr = "Seviye 3'e ulaşın & En az 70 Momentum",
            hpBonus = 15,
            meetsPreconditions = { it.level >= 3 && it.momentum >= 70 }
        ),
        TitleDef(
            id = "void_reaper",
            nameEn = "Hollow Reaper of Void 💀",
            nameTr = "Karanlık Boşluk Tırpancısı 💀",
            descEn = "The shadows bend to your malicious gaze. A feared collector of corrupt essence.",
            descTr = "Gölgeler sizin karanlık bakışınız önünde eğiliyor. Çürümüş ruhların korkulan avcısı.",
            isHidden = false,
            requirementDescEn = "Reach Level 5 & Momentum 15 or lower",
            requirementDescTr = "Seviye 5'e ulaşın & -35 veya daha az Hizalanma",
            hpBonus = 25,
            meetsPreconditions = { it.level >= 5 && it.momentum <= 15 }
        ),
        TitleDef(
            id = "cosmic_observer",
            nameEn = "Cosmic Balance Arbiter ⚖️",
            nameTr = "Kozmik Denge Hakimi ⚖️",
            descEn = "A silent watcher holding the scales of creation in perfect alignment.",
            descTr = "Yaradılışın kefelerini kusursuz dengede tutan sessiz ve tarafsız izleyici.",
            isHidden = true,
            requirementDescEn = "Absolute Momentum Neutral (exactly 50) at Floor 10+",
            requirementDescTr = "Kulede 10. Kat veya üzerinde Hizalanma değerinizin tam Sıfır (0) olması",
            hpBonus = 30,
            meetsPreconditions = { it.currentFloor >= 10 && it.momentum == 50 }
        ),
        TitleDef(
            id = "immortal_phantom",
            nameEn = "Undying Fractured Specter 👻",
            nameTr = "Ölümsüz Kırık Hayalet 👻",
            descEn = "Your soul has fractured many times, yet your core remains stubbornly unbroken.",
            descTr = "Ruhunuz defalarca parçalandı fakat iradeniz inatla parçalanmayı reddediyor.",
            isHidden = true,
            requirementDescEn = "Suffer 5+ Spirit Fractures (Deaths) in your journey",
            requirementDescTr = "Yolculuğunuzda en az 5 kez Parçalanma (Ölüm) yaşayın",
            hpBonus = 40,
            meetsPreconditions = { it.totalFractures >= 5 }
        ),
        TitleDef(
            id = "spire_conqueror",
            nameEn = "Legendary Spire Pioneer 🏔️",
            nameTr = "Efsanevi Kule Öncüsü 🏔️",
            descEn = "You have conquered half of the world's most dangerous tower heights.",
            descTr = "Dünyanın en tehlikeli kule tırmanışının yarısını başarıyla fethettiniz.",
            isHidden = false,
            requirementDescEn = "Reach Floor 50 or higher",
            requirementDescTr = "En az 50. Kata ulaşın",
            hpBonus = 50,
            meetsPreconditions = { it.currentFloor >= 50 }
        ),
        TitleDef(
            id = "gold_hoarder",
            nameEn = "Spire Guild Tycoon 💰",
            nameTr = "Kule Loncası Baronu 💰",
            descEn = "Riches beyond imagination. Gold is your ultimate tool of manipulation.",
            descTr = "Zenginliği zırh edinmiş bir baron. Altın en büyük manipülasyon aracınız.",
            isHidden = false,
            requirementDescEn = "Amass 500+ Gold coins",
            requirementDescTr = "En az 500 Altın biriktirin",
            hpBonus = 10,
            meetsPreconditions = { it.gold >= 500 }
        ),
        TitleDef(
            id = "doomsday_bringer",
            nameEn = "Sovereign of Absolute Void 🪐",
            nameTr = "Mutlak Boşluğun Hükümdarı 🪐",
            descEn = "The embodiment of cosmic decay. You are the doomsday itself.",
            descTr = "Kozmik çürümenin ete kemiğe bürünmüş hali. Siz bizzat kıyametsiniz.",
            isHidden = true,
            requirementDescEn = "Covenant Aligned with Level 15+ & Momentum 15 or lower",
            requirementDescTr = "Kara Ahit yeminli, Seviye 15 ve üzeri & En az 15 veya daha az Momentum",
            hpBonus = 60,
            meetsPreconditions = { it.side == "COVENANT" && it.level >= 15 && it.momentum <= 15 }
        ),
        TitleDef(
            id = "archon_sage",
            nameEn = "Paragon Solar Archon 👑",
            nameTr = "Semavi Güneş Rehberi 👑",
            descEn = "Pure solar glory radiates from your actions. Spires bow to your presence.",
            descTr = "Eylemlerinizden saf güneş parlaklığı yayılıyor. Kuleler varlığınız önünde eğiliyor.",
            isHidden = false,
            requirementDescEn = "Sanctum Aligned with Level 15+ & Momentum 85 or higher",
            requirementDescTr = "Semavi yeminli, Seviye 15 ve üzeri & En az 85 Momentum",
            hpBonus = 60,
            meetsPreconditions = { it.side == "SANCTUM" && it.level >= 15 && it.momentum >= 85 }
        ),
        TitleDef(
            id = "plague_vanquisher",
            nameEn = "Plague Vanquisher 🐀",
            nameTr = "Veba Fatihi 🐀",
            descEn = "Slayer of Golgoth, the corrupted Plague Rat King of floor 1.",
            descTr = "1. katın yozlaşmış Veba Lağım Faresi Kralı Golgoth'u dize getiren kahraman.",
            isHidden = false,
            requirementDescEn = "Complete Floor 1 Quest",
            requirementDescTr = "1. Kat Görevini Tamamlayın",
            hpBonus = 15,
            meetsPreconditions = { it.currentFloor > 1 }
        ),
        TitleDef(
            id = "shard_bearer",
            nameEn = "Shard Bearer 💎",
            nameTr = "Kristal Sancaktarı 💎",
            descEn = "Slayer of Clarith, the Shimmering Crystal Guardian of floor 2.",
            descTr = "2. katın Işıldayan Kristal Muhafızı Clarith'i yıkan kahraman.",
            isHidden = false,
            requirementDescEn = "Complete Floor 2 Quest",
            requirementDescTr = "2. Kat Görevini Tamamlayın",
            hpBonus = 25,
            meetsPreconditions = { it.currentFloor > 2 }
        ),
        TitleDef(
            id = "crypt_breaker",
            nameEn = "Crypt Breaker ⚰️",
            nameTr = "Mezar Kırıcı ⚰️",
            descEn = "Slayer of Malakar, the Shadow Necromancer Oracle of floor 3.",
            descTr = "3. katın Gölge Necromancer Kahini Malakar'ı kovup mezarı fetheden kahraman.",
            isHidden = false,
            requirementDescEn = "Complete Floor 3 Quest",
            requirementDescTr = "3. Kat Görevini Tamamlayın",
            hpBonus = 35,
            meetsPreconditions = { it.currentFloor > 3 }
        )
    )

    val quests = listOf(
        // --- FLOOR PROGRESSION QUESTS ---
        QuestDef(
            id = "floor_1_rat_king",
            type = QuestType.MAIN,
            titleEn = "Floor 1 Apex: Plague Vanquisher",
            titleTr = "1. Kat Zirvesi: Veba Fatihi",
            descEn = "Defeat Golgoth, the Plague Rat King who sits bloated upon his pile of garbage.",
            descTr = "Çöp yığınları üzerine kurulmuş olan devasa Veba Faresi Kralı Golgoth'u dize getirin.",
            requirementEn = "Conquer Floor 1 Boss and ascend to Floor 2",
            requirementTr = "1. Kat Patronunu yenip 2. Kata yükselin",
            checkProgress = { it.currentFloor > 1 },
            rewardGold = 100,
            rewardExp = 150,
            rewardItem = "Familiar: Pet Ember Kitten",
            rewardTitle = "Plague Vanquisher 🐀"
        ),
        QuestDef(
            id = "floor_2_crystal_heart",
            type = QuestType.MAIN,
            titleEn = "Floor 2 Apex: Shard Unearther",
            titleTr = "2. Kat Zirvesi: Parça Çıkarıcı",
            descEn = "Unearth crystal hearts by defeating Clarith, the Shimmering Crystal Guardian.",
            descTr = "Işıldayan Kristal Muhafız Clarith'i yenerek kozmik kristal kalbini ortaya çıkarın.",
            requirementEn = "Conquer Floor 2 Boss and ascend to Floor 3",
            requirementTr = "2. Kat Patronunu yenip 3. Kata yükselin",
            prerequisiteQuestId = "floor_1_rat_king",
            checkProgress = { it.currentFloor > 2 },
            rewardGold = 150,
            rewardExp = 200,
            rewardItem = "Companion: Holographic Pixie",
            rewardTitle = "Shard Bearer 💎"
        ),
        QuestDef(
            id = "floor_3_oracle_scourge",
            type = QuestType.MAIN,
            titleEn = "Floor 3 Apex: Oracle Banisher",
            titleTr = "3. Kat Zirvesi: Kahin Kovucu",
            descEn = "Banish Malakar, the Shadow Necromancer Oracle guarding the deep sub-cathedrals.",
            descTr = "Derin mahzenleri ve gölgeli kutsal alanları koruyan Gölge Kahini Malakar'ı dehlizlerden kovun.",
            requirementEn = "Conquer Floor 3 Boss and ascend to Floor 4",
            requirementTr = "3. Kat Patronunu yenip 4. Kata yükselin",
            prerequisiteQuestId = "floor_2_crystal_heart",
            checkProgress = { it.currentFloor > 3 },
            rewardGold = 200,
            rewardExp = 300,
            rewardItem = "Familiar: Petite Void Drake",
            rewardTitle = "Crypt Breaker ⚰️"
        ),

        // --- MAIN QUESTS ---
        QuestDef(
            id = "main_foothold",
            type = QuestType.MAIN,
            titleEn = "Tower Foothold",
            titleTr = "Kulede Tutunmak",
            descEn = "Prove you have the stamina to climb the lower segments of the tower floors.",
            descTr = "Kulenin alt kesimlerindeki tehlikelere dayanabileceğinizi kanıtlayın.",
            requirementEn = "Climb to Floor 5 or higher",
            requirementTr = "5. Kat veya üzerine tırmanın",
            checkProgress = { it.currentFloor >= 5 },
            rewardGold = 80,
            rewardExp = 120
        ),
        QuestDef(
            id = "main_midpoint",
            type = QuestType.MAIN,
            titleEn = "Aegis Midpoint",
            titleTr = "Aegis Orta Noktası",
            descEn = "Survive the grueling trial and rise beyond the midpoint of lower floors.",
            descTr = "Zorlu kule tırmanışında alt katların orta noktasına kadar yükselip hayatta kalın.",
            requirementEn = "Climb to Floor 15 or higher",
            requirementTr = "15. Kat veya üzerine tırmanın",
            checkProgress = { it.currentFloor >= 15 },
            rewardGold = 180,
            rewardExp = 250,
            rewardItem = "Aegis Shard of Eternity"
        ),

        // --- SIDE QUESTS ---
        QuestDef(
            id = "side_wealth",
            type = QuestType.SIDE,
            titleEn = "Merchant Barter Guild",
            titleTr = "Tüccar Takas Birliği",
            descEn = "Earning enough treasure allows you to trade secrets with the high nomads.",
            descTr = "Yeterince zengin olmak göçebelerle gizli ticaret yapabilmenizi sağlar.",
            requirementEn = "Acquire 300+ Gold coins",
            requirementTr = "En az 300 Altın biriktirin",
            checkProgress = { it.gold >= 300 },
            rewardExp = 200,
            rewardItem = "Aetherweave Cloak of Sanctum"
        ),
        QuestDef(
            id = "side_sanctum_purity",
            type = QuestType.SIDE,
            titleEn = "Vow of Celestial Purity",
            titleTr = "Semavi Saflık Andı",
            descEn = "Embrace the solar radiance. Prove your alignment with the divine Sanctum faction.",
            descTr = "Güneşin hararetini kucaklayın. Semavi birliğine olan bağlılığınızı kanıtlayın.",
            requirementEn = "Become Sanctum Aligned (Momentum 65 or higher)",
            requirementTr = "Semavi Birliğine Katılın (En az +15 Hizalanma)",
            checkProgress = { it.side == "SANCTUM" && it.momentum >= 65 },
            rewardExp = 250,
            rewardGold = 80,
            rewardAether = 60
        ),
        QuestDef(
            id = "side_void_alliance",
            type = QuestType.SIDE,
            titleEn = "Deep Abyss Alliance",
            titleTr = "Derin Boşluk Anlaşması",
            descEn = "Whisper to the void, let the dark echo of the Covenant shape your shadows.",
            descTr = "Boşluğun sesini dinleyin, Kara Ahit'in karanlık tınısının gölgenizi şekillendirmesine izin verin.",
            requirementEn = "Become Covenant Aligned (Momentum 35 or lower)",
            requirementTr = "Kara Ahit Birliğine Katılın (En az -15 Hizalanma)",
            checkProgress = { it.side == "COVENANT" && it.momentum <= 35 },
            rewardExp = 250,
            rewardGold = 80,
            rewardAether = 60
        ),

        // --- CHAIN QUESTS ---
        QuestDef(
            id = "chain_ascension_1",
            type = QuestType.CHAIN,
            titleEn = "Initiate's Trial (Chain I)",
            titleTr = "Çömezin Sınavı (Aşama I)",
            descEn = "Every hero begins in shadows. Train your core skills to become an expert crawler.",
            descTr = "Her kahraman gölgelerde başlar. Kulede ustalaşmak için kendinizi eğitin.",
            requirementEn = "Reach Character Level 4+",
            requirementTr = "Karakter Seviyesini En Az 4 Yapın",
            checkProgress = { it.level >= 4 },
            rewardExp = 100,
            rewardGold = 40,
            rewardTitle = "Tower Novice Apprentice"
        ),
        QuestDef(
            id = "chain_ascension_2",
            type = QuestType.CHAIN,
            titleEn = "Expert Ascendancy (Chain II)",
            titleTr = "Uzman Yükselişi (Aşama II)",
            descEn = "You have proven your basics. Now steel your soul against deeper corridor horrors.",
            descTr = "Temelinizi kanıtladınız. Şimdi daha derin koridorlardaki dehşetlere karşı ruhunuzu çelikleştirin.",
            requirementEn = "Reach Character Level 12+",
            requirementTr = "Karakter Seviyesini En Az 12 Yapın",
            prerequisiteQuestId = "chain_ascension_1",
            checkProgress = { it.level >= 12 },
            rewardExp = 300,
            rewardGold = 150,
            rewardItem = "Celesta Dawnbreaker Sword"
        ),
        QuestDef(
            id = "chain_ascension_3",
            type = QuestType.CHAIN,
            titleEn = "Apex Overlord Decree (Chain III)",
            titleTr = "Derebeyi Fermanı (Aşama III)",
            descEn = "The final segment of human limits. Unleash infinite spiritual potential to reach maximum tier.",
            descTr = "İnsan sınırlarının son dilimi. En yüksek aşamaya ulaşmak için sonsuz ruhsal gücü serbest bırakın.",
            requirementEn = "Reach Character Level 20+",
            requirementTr = "Karakter Seviyesini En Az 20 Yapın",
            prerequisiteQuestId = "chain_ascension_2",
            checkProgress = { it.level >= 20 },
            rewardExp = 800,
            rewardGold = 400,
            rewardTitle = "Apex Ascendant Sovereign"
        ),

        // --- HIDDEN QUESTS ---
        QuestDef(
            id = "hidden_fractured_soul",
            type = QuestType.HIDDEN,
            titleEn = "??? (Fractured Resiliency)",
            titleTr = "??? (Kırık İncelik)",
            descEn = "A secret path revealed only to those whose spirits have shattered repeatedly.",
            descTr = "Sadece ruhları defalarca kez parçalanmış olanlara fısıldanan gizli bir yol.",
            requirementEn = "Trigger 4 or more Spirit Fractures (Deaths)",
            requirementTr = "En az 4 kez Ruh Parçalanması (Ölüm) yaşayın",
            checkProgress = { it.totalFractures >= 4 },
            rewardExp = 400,
            rewardItem = "Shattered Skull of Resiliency",
            rewardTitle = "Fracture Vanguard Hero"
        ),
        QuestDef(
            id = "hidden_perfect_balance",
            type = QuestType.HIDDEN,
            titleEn = "??? (True Neutrality)",
            titleTr = "??? (Mutlak Denge)",
            descEn = "Walk the tightrope of destiny without leaning to heaven or hell up to Floor 15.",
            descTr = "Kader ipinde her iki tarafa da sapmadan 15. Kata kadar yürüyün.",
            requirementEn = "Reach Floor 15 while maintaining EXACTLY 50 Momentum",
            requirementTr = "15. Kata vardığınızda Hizalanma (Alignment) değerinizi TAM SIFIR (0) tutun",
            checkProgress = { it.currentFloor >= 15 && it.momentum == 50 },
            rewardExp = 500,
            rewardGold = 250,
            rewardTitle = "Guardian of Cosmic Neutrality"
        ),

        // --- NORMAL QUESTS ---
        QuestDef(
            id = "normal_climb_3",
            type = QuestType.NORMAL,
            titleEn = "The First Steps",
            titleTr = "İlk Adımlar",
            descEn = "Every long trek begins with a few courageous steps up the tower base.",
            descTr = "Her büyük yolculuk kulenin eteklerinde atılan birkaç cesur adımla başlar.",
            requirementEn = "Ascend to Floor 3 or higher",
            requirementTr = "3. Kat veya üzerine tırmanın",
            checkProgress = { it.currentFloor >= 3 },
            rewardGold = 50,
            rewardExp = 80
        ),

        // --- SPECIAL QUESTS ---
        QuestDef(
            id = "special_alignment_pioneer",
            type = QuestType.SPECIAL,
            titleEn = "Avenue of Conviction",
            titleTr = "İnanç Patikası",
            descEn = "Commit strongly to either the celestial sun rays or the depth of the void.",
            descTr = "Göksel güneş ışıklarına ya da boşluğun karanlık fısıltılarına güçlü bir bağlılık gösterin.",
            requirementEn = "Momentum >= 75 (Celestial) or <= 25 (Void)",
            requirementTr = "Hizalanma >= +45 veya <= -45 olmalı",
            checkProgress = { it.momentum >= 75 || it.momentum <= 25 },
            rewardGold = 150,
            rewardExp = 200,
            rewardItem = "Scroll of Conviction"
        ),

        // --- EVENT QUESTS ---
        QuestDef(
            id = "event_solar_zenith",
            type = QuestType.EVENT,
            titleEn = "Sovereign Meridian Zenith ☀️",
            titleTr = "Mutlak Güneş Şöleni Enlemi ☀️",
            descEn = "A temporal star solstice has aligned. Unleash your soul's level capabilities.",
            descTr = "Kozmik bir göksel hizalama gerçekleşti. Ruhunuzun seviyesini yükseltip andı kutlayın.",
            requirementEn = "Reach Character Level 6+",
            requirementTr = "Karakter Seviyesini En Az 6 Yapın",
            checkProgress = { it.level >= 6 },
            rewardGold = 120,
            rewardExp = 150,
            rewardAether = 40
        )
    )

    /**
     * Finds a title by its id index.
     */
    fun getTitleDef(id: String): TitleDef? {
        return titles.find { it.id == id }
    }

    /**
     * Analyzes and returns all currently qualified titles for the player.
     */
    fun getEligibleTitles(player: PlayerProfile): List<TitleDef> {
        return titles.filter { it.meetsPreconditions(player) }
    }

    /**
     * Scans and returns quest definitions with statuses mapped for UI list binding.
     */
    fun getQuestProgress(player: PlayerProfile): List<QuestStatus> {
        val completedSet = player.completedQuestsEncoded.split(",")
            .filter { it.isNotBlank() }
            .toSet()

        return quests.map { q ->
            val isCompleted = completedSet.contains(q.id)
            val prerequisiteMet = q.prerequisiteQuestId == null || completedSet.contains(q.prerequisiteQuestId)
            val meetsPrecon = q.meetsPreconditions(player)
            val isUnlocked = prerequisiteMet && meetsPrecon
            val requirementMet = q.checkProgress(player)

            val statusType = when {
                isCompleted -> QuestStatusType.COMPLETED
                !isUnlocked -> if (q.type == QuestType.HIDDEN) QuestStatusType.HIDDEN else QuestStatusType.LOCKED
                else -> QuestStatusType.ACTIVE
            }

            QuestStatus(
                quest = q,
                status = statusType,
                isCompleted = isCompleted,
                isUnlocked = isUnlocked,
                requirementMet = requirementMet && isUnlocked && !isCompleted
            )
        }
    }
}

/**
 * Transient state carrying status metrics for quests.
 */
data class QuestStatus(
    val quest: QuestDef,
    val status: QuestStatusType,
    val isCompleted: Boolean,
    val isUnlocked: Boolean,
    val requirementMet: Boolean
) {
    fun getProgressLabelAndFraction(player: PlayerProfile, isTr: Boolean): Pair<String, Float> {
        if (isCompleted) return Pair(if (isTr) "%100 Tamamlandı" else "100% Completed", 1f)
        if (!isUnlocked) return Pair(if (isTr) "Kilitli" else "Locked", 0f)
        val q = quest
        return when (q.id) {
            "main_foothold" -> {
                val curr = player.currentFloor
                val target = 5
                val label = if (isTr) "Kat $curr / $target" else "Floor $curr / $target"
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "main_midpoint" -> {
                val curr = player.currentFloor
                val target = 15
                val label = if (isTr) "Kat $curr / $target" else "Floor $curr / $target"
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "side_wealth" -> {
                val curr = player.gold
                val target = 300
                val label = if (isTr) "Altın $curr / $target" else "Gold $curr / $target"
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "side_sanctum_purity" -> {
                if (player.side != "SANCTUM") {
                    Pair(if (isTr) "Semavi Değilsiniz" else "Not Sanctum Aligned", 0f)
                } else {
                    val curr = player.momentum
                    val target = 65
                    val label = if (isTr) "Momentum $curr / $target" else "Momentum $curr / $target"
                    Pair(label, ((curr - 50).toFloat() / 15f).coerceIn(0f, 1f))
                }
            }
            "side_void_alliance" -> {
                if (player.side != "COVENANT") {
                    Pair(if (isTr) "Kara Ahit Değilsiniz" else "Not Covenant Aligned", 0f)
                } else {
                    val curr = (50 - player.momentum).coerceAtLeast(0)
                    val target = 15
                    val label = if (isTr) "Karanlık Momentum $curr / $target" else "Void Momentum $curr / $target"
                    Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
                }
            }
            "chain_ascension_1" -> {
                val curr = player.level
                val target = 4
                val label = if (isTr) "Seviye $curr / $target" else "Level $curr / $target"
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "chain_ascension_2" -> {
                val curr = player.level
                val target = 12
                val label = if (isTr) "Seviye $curr / $target" else "Level $curr / $target"
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "chain_ascension_3" -> {
                val curr = player.level
                val target = 20
                val label = if (isTr) "Seviye $curr / $target" else "Level $curr / $target"
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "hidden_fractured_soul" -> {
                val curr = player.totalFractures
                val target = 4
                val label = if (isTr) "Ruh Kırılması $curr / $target" else "Spirit Fractures $curr / $target"
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "hidden_perfect_balance" -> {
                val currFl = player.currentFloor
                val align = player.momentum
                val isBalanced = align == 50
                if (!isUnlocked) {
                    Pair("???", 0f)
                } else if (!isBalanced) {
                    val displayVal = align - 50
                    Pair(if (isTr) "Denge Bozuldu (Değer: $displayVal)" else "Imbalanced (Alignment: $displayVal)", 0f)
                } else {
                    val label = if (isTr) "Dengeli Kat $currFl / 15" else "Balanced Floor $currFl / 15"
                    Pair(label, (currFl.toFloat() / 15).coerceIn(0f, 1f))
                }
            }
            "floor_1_rat_king" -> {
                val curr = player.currentFloor
                val label = if (isTr) "Kat $curr / 2" else "Floor $curr / 2"
                Pair(label, (if (curr >= 2) 1f else 0.5f))
            }
            "floor_2_crystal_heart" -> {
                val curr = player.currentFloor
                val label = if (isTr) "Kat $curr / 3" else "Floor $curr / 3"
                Pair(label, (if (curr >= 3) 1f else (if (curr == 2) 0.5f else 0f)))
            }
            "floor_3_oracle_scourge" -> {
                val curr = player.currentFloor
                val label = if (isTr) "Kat $curr / 4" else "Floor $curr / 4"
                Pair(label, (if (curr >= 4) 1f else (if (curr == 3) 0.5f else 0f)))
            }
            "normal_climb_3" -> {
                val curr = player.currentFloor
                val target = 3
                val label = if (isTr) "Kat $curr / $target" else "Floor $curr / $target"
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            "special_alignment_pioneer" -> {
                val dist = Math.abs(player.momentum - 50)
                val target = 25
                val label = if (isTr) "Momentum Farkı $dist / $target" else "Momentum Diff $dist / $target"
                Pair(label, (dist.toFloat() / target).coerceIn(0f, 1f))
            }
            "event_solar_zenith" -> {
                val curr = player.level
                val target = 6
                val label = if (isTr) "Seviye $curr / $target" else "Level $curr / $target"
                Pair(label, (curr.toFloat() / target).coerceIn(0f, 1f))
            }
            else -> Pair("", 1f)
        }
    }
}

