package com.example.data.engine

import com.example.data.model.PlayerProfile

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
    HIDDEN,
    CHAIN
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
    val rewardGleam: Int = 0,
    val rewardPyre: Int = 0
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
            requirementDescEn = "Reach Level 3 & Alignment +20 minimum",
            requirementDescTr = "Seviye 3'e ulaşın & En az +20 Hizalanma",
            hpBonus = 15,
            meetsPreconditions = { it.level >= 3 && it.alignment >= 20 }
        ),
        TitleDef(
            id = "void_reaper",
            nameEn = "Hollow Reaper of Void 💀",
            nameTr = "Karanlık Boşluk Tırpancısı 💀",
            descEn = "The shadows bend to your malicious gaze. A feared collector of corrupt essence.",
            descTr = "Gölgeler sizin karanlık bakışınız önünde eğiliyor. Çürümüş ruhların korkulan avcısı.",
            isHidden = false,
            requirementDescEn = "Reach Level 5 & Alignment -35 or lower",
            requirementDescTr = "Seviye 5'e ulaşın & -35 veya daha az Hizalanma",
            hpBonus = 25,
            meetsPreconditions = { it.level >= 5 && it.alignment <= -35 }
        ),
        TitleDef(
            id = "cosmic_observer",
            nameEn = "Cosmic Balance Arbiter ⚖️",
            nameTr = "Kozmik Denge Hakimi ⚖️",
            descEn = "A silent watcher holding the scales of creation in perfect alignment.",
            descTr = "Yaradılışın kefelerini kusursuz dengede tutan sessiz ve tarafsız izleyici.",
            isHidden = true,
            requirementDescEn = "Absolute Alignment Neutral (exactly 0) at Floor 10+",
            requirementDescTr = "Kulede 10. Kat veya üzerinde Hizalanma değerinizin tam Sıfır (0) olması",
            hpBonus = 30,
            meetsPreconditions = { it.currentFloor >= 10 && it.alignment == 0 }
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
            requirementDescEn = "Covenant Aligned with Level 15+ & Alignment -85 or lower",
            requirementDescTr = "Kara Ahit yeminli, Seviye 15 ve üzeri & En az -85 Hizalanma",
            hpBonus = 60,
            meetsPreconditions = { it.side == "COVENANT" && it.level >= 15 && it.alignment <= -85 }
        ),
        TitleDef(
            id = "archon_sage",
            nameEn = "Paragon Solar Archon 👑",
            nameTr = "Semavi Güneş Rehberi 👑",
            descEn = "Pure solar glory radiates from your actions. Spires bow to your presence.",
            descTr = "Eylemlerinizden saf güneş parlaklığı yayılıyor. Kuleler varlığınız önünde eğiliyor.",
            isHidden = false,
            requirementDescEn = "Sanctum Aligned with Level 15+ & Alignment +85 or higher",
            requirementDescTr = "Semavi yeminli, Seviye 15 ve üzeri & En az +85 Hizalanma",
            hpBonus = 60,
            meetsPreconditions = { it.side == "SANCTUM" && it.level >= 15 && it.alignment >= 85 }
        )
    )

    val quests = listOf(
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
            requirementEn = "Become Sanctum Aligned (Alignment +15 or higher)",
            requirementTr = "Semavi Birliğine Katılın (En az +15 Hizalanma)",
            checkProgress = { it.side == "SANCTUM" && it.alignment >= 15 },
            rewardExp = 250,
            rewardGold = 80,
            rewardGleam = 60
        ),
        QuestDef(
            id = "side_void_alliance",
            type = QuestType.SIDE,
            titleEn = "Deep Abyss Alliance",
            titleTr = "Derin Boşluk Anlaşması",
            descEn = "Whisper to the void, let the dark echo of the Covenant shape your shadows.",
            descTr = "Boşluğun sesini dinleyin, Kara Ahit'in karanlık tınısının gölgenizi şekillendirmesine izin verin.",
            requirementEn = "Become Covenant Aligned (Alignment -15 or lower)",
            requirementTr = "Kara Ahit Birliğine Katılın (En az -15 Hizalanma)",
            checkProgress = { it.side == "COVENANT" && it.alignment <= -15 },
            rewardExp = 250,
            rewardGold = 80,
            rewardPyre = 60
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
            requirementEn = "Reach Floor 15 while maintaining EXACTLY 0 Alignment",
            requirementTr = "15. Kata vardığınızda Hizalanma (Alignment) değerinizi TAM SIFIR (0) tutun",
            checkProgress = { it.currentFloor >= 15 && it.alignment == 0 },
            rewardExp = 500,
            rewardGold = 250,
            rewardTitle = "Guardian of Cosmic Neutrality"
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
)
