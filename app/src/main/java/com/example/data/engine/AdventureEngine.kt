package com.example.data.engine

import kotlin.random.Random
import com.example.data.model.PlayerProfile

enum class NodeType {
    NARRATIVE,
    COMBAT,
    BOSS,
    CHEST,
    SHRINE,
    MERCHANT
}

data class AdventureNode(
    val index: Int,
    val type: NodeType,
    val title: String,
    val description: String,
    val titleTr: String,
    val descriptionTr: String,
    // Enemy particulars (if COMBAT or BOSS)
    val enemyNameEn: String = "",
    val enemyNameTr: String = "",
    val enemyHp: Int = 0,
    val enemyMaxHp: Int = 0,
    val enemyAtk: Int = 0,
    // Choice Options (if NARRATIVE, CHEST, SHRINE, MERCHANT)
    val optionA: NodeChoice? = null,
    val optionB: NodeChoice? = null,
    val optionC: NodeChoice? = null,
    val willCost: Int = 0
)

data class NodeChoice(
    val textEn: String,
    val textTr: String,
    val journalEn: String,
    val journalTr: String,
    val hpChange: Int = 0,
    val goldChange: Int = 0,
    val gleamChange: Int = 0,
    val pyreChange: Int = 0,
    val expChange: Int = 0,
    val alignmentShift: Int = 0,
    val willChange: Int = 0,
    val rewardItem: String = "",
    val rewardTitle: String = ""
)

data class EnemyStats(
    val nameEn: String,
    val nameTr: String,
    val hp: Int,
    val atk: Int
)

object AdventureEngine {

    fun generateNodesForFloor(floor: Int, player: PlayerProfile? = null): List<AdventureNode> {
        // Deterministic seed ensures consistent map generation for a given floor
        val floorSeed = floor.toLong() * 41235L + 56789L
        val random = Random(floorSeed)

        // Count of nodes: 15-20 nodes per floor
        val totalNodes = 15 + random.nextInt(6) // 15..20

        // Combat events: 1-7 combat events
        val combatCount = 1 + random.nextInt(7) // 1..7 combat nodes

        val nodes = ArrayList<AdventureNode>()

        // Shatter floor into specialized lists
        val availableIndices = (1 until totalNodes - 1).toList().shuffled(random)
        val selectedCombatIndices = availableIndices.take(combatCount).toSet()

        // Distribute others
        val remainingIndices = availableIndices.drop(combatCount)
        val selectedMerchantIndices = remainingIndices.take(2).toSet()
        val selectedChestIndices = remainingIndices.drop(2).take(3).toSet()
        val selectedShrineIndices = remainingIndices.drop(5).take(3).toSet()

        for (i in 0 until totalNodes) {
            when {
                i == totalNodes - 1 -> {
                    // Ends with a Boss fight!
                    val bossInfo = getBossForFloor(floor, random)
                    nodes.add(
                        AdventureNode(
                            index = i,
                            type = NodeType.BOSS,
                            title = "Floor $floor Overlord: ${bossInfo.nameEn}",
                            description = "You have reached the pinnacle vault of Floor $floor. Guarding the cosmic seal is the Warden of Blight: ${bossInfo.nameEn}. Slay the Overlord to unlock the ascent!",
                            titleTr = "${floor}. Kat Derebeyi: ${bossInfo.nameTr}",
                            descriptionTr = "${floor}. Kat zirve kubbesine ulaştınız. Geçit mührünü koruyan azametli Musibet Gardiyanı ${bossInfo.nameTr} karşınızda kükrüyor. Yükselmek için canavarı alt edin!",
                            enemyNameEn = bossInfo.nameEn,
                            enemyNameTr = bossInfo.nameTr,
                            enemyHp = bossInfo.hp,
                            enemyMaxHp = bossInfo.hp,
                            enemyAtk = bossInfo.atk,
                            willCost = 2 // Boss costs 2 Will to challenge
                        )
                    )
                }
                selectedCombatIndices.contains(i) -> {
                    // Normal Combat node
                    val enemyInfo = getEnemyForFloor(floor, random)
                    nodes.add(
                        AdventureNode(
                            index = i,
                            type = NodeType.COMBAT,
                            title = "Anomalous Encounter: ${enemyInfo.nameEn}",
                            description = "A raw manifestation of the Eternal Blight has materialised in front of you. Defeat it to proceed safely.",
                            titleTr = "Lanetli Saldırı: ${enemyInfo.nameTr}",
                            descriptionTr = "Ebedi Çürümekten fırlayan vahşi bir yaratık yolunuza tıkadı. Güvenle ilerlemek için dövüşe durmalısınız.",
                            enemyNameEn = enemyInfo.nameEn,
                            enemyNameTr = enemyInfo.nameTr,
                            enemyHp = enemyInfo.hp,
                            enemyMaxHp = enemyInfo.hp,
                            enemyAtk = enemyInfo.atk,
                            willCost = 1 // Normal combat costs 1 Will
                        )
                    )
                }
                selectedMerchantIndices.contains(i) -> {
                    nodes.add(generateMerchantNode(i, floor, random))
                }
                selectedChestIndices.contains(i) -> {
                    nodes.add(generateChestNode(i, floor, random))
                }
                selectedShrineIndices.contains(i) -> {
                    nodes.add(generateShrineNode(i, floor, random))
                }
                else -> {
                    nodes.add(generateNarrativeNode(i, floor, random))
                }
            }
        }

        if (player != null) {
            // Check preconditions for secret/hidden content
            val targetIndex = (totalNodes / 2).coerceIn(1..totalNodes - 2)
            
            val itemsList = if (player.itemsEncoded.isEmpty()) emptyList() else player.itemsEncoded.split(",")
            val titlesList = if (player.titlesEncoded.isEmpty()) emptyList() else player.titlesEncoded.split(",")
            
            var secretNode: AdventureNode? = null
            
            // 1. Item precondition (Secret Boss)
            val hasLegendaryItem = itemsList.any { 
                it.contains("Celestial", ignoreCase = true) || 
                it.contains("Sovereign", ignoreCase = true) || 
                it.contains("Vanguard", ignoreCase = true) || 
                it.contains("Archon", ignoreCase = true) ||
                it.contains("Pendant", ignoreCase = true)
            }
            
            // 2. Title / Experience precondition (Secret Narrative Hermit)
            val hasSpecialTitle = titlesList.any { 
                it.contains("Sovereign", ignoreCase = true) || 
                it.contains("Blight Slayer", ignoreCase = true) || 
                it.contains("Warlock", ignoreCase = true) || 
                it.contains("Champion", ignoreCase = true)
            }

            if (hasLegendaryItem && floor >= 10) {
                // Secret Boss: Relic Defender Golem
                val bossHp = 400 + (floor * 12)
                val bossAtk = 25 + (floor * 0.9).toInt()
                secretNode = AdventureNode(
                    index = targetIndex,
                    type = NodeType.BOSS,
                    title = "SECRET: Relic Sentinel Defender",
                    description = "Your heavy legendary artifacts resonate with ancient stone carvings! Out of the temple foundations, a temporal colossus arises to reclaim the sacred relics.",
                    titleTr = "GİZLİ: Kutsal Yadigar Koruyucu Dev",
                    descriptionTr = "Sırt çantanızdaki efsanevi eserler buradaki antik oymalarla derin titreşime girdi! Tapınak temellerinin arasından fırlayan kadim colossus, kutsal eşyaları geri almak için saldırıya geçti.",
                    enemyNameEn = "Relic Sentinel Centurion",
                    enemyNameTr = "Yadigar Muhafızı Koruyucu Santur",
                    enemyHp = bossHp,
                    enemyMaxHp = bossHp,
                    enemyAtk = bossAtk,
                    willCost = 1
                )
            } else if (player.alignment >= 50 && player.level >= 10) {
                // Secret Boss: Celestial Arbiter (Light aligned challenge)
                val bossHp = 350 + (floor * 11)
                val bossAtk = 22 + (floor * 0.8).toInt()
                secretNode = AdventureNode(
                    index = targetIndex,
                    type = NodeType.BOSS,
                    title = "SECRET: Celestial Arbiter Auriel",
                    description = "Sensing your holy alignment and celestial starlight aura, the Judgment Angel Auriel descends to test your combat purity!",
                    titleTr = "GİZLİ: Işığın Kutsal Yargıcı Auriel",
                    descriptionTr = "Tepeden tırnağa yayılan ışık nizamı ve yüksek kutsallık yöneliminiz sebebiyle, Gök kubbeden süzülen Hüküm Meleği Auriel sizi sınamak için yeryüzüne indi!",
                    enemyNameEn = "Celestial Arbiter Auriel",
                    enemyNameTr = "Kutsal Yargıç Melek Auriel",
                    enemyHp = bossHp,
                    enemyMaxHp = bossHp,
                    enemyAtk = bossAtk,
                    willCost = 1
                )
            } else if (player.alignment <= -50 && player.level >= 10) {
                // Secret Boss: Shadow Harbinger of the Abyss (Void aligned challenge)
                val bossHp = 360 + (floor * 11)
                val bossAtk = 24 + (floor * 0.8).toInt()
                secretNode = AdventureNode(
                    index = targetIndex,
                    type = NodeType.BOSS,
                    title = "SECRET: Sovereign Shadow Executor",
                    description = "Your monstrous alignment and affinity to the void has fractured the temporal layer. An executioner of the Abyss has arrived to claim your strength!",
                    titleTr = "GİZLİ: Boşluk Gölgelerinin İnfazcısı",
                    descriptionTr = "Ruhunuzdaki canavarca karanlık yönelim ve boşluk yakınlığı geçici zaman perdesini çatlattı. Derinliklerden gelen Boşluk İnfazcısı canınızı almaya can atıyor!",
                    enemyNameEn = "Abyssal Shadow Executor",
                    enemyNameTr = "Karanlık Boşluk İnfazcısı",
                    enemyHp = bossHp,
                    enemyMaxHp = bossHp,
                    enemyAtk = bossAtk,
                    willCost = 1
                )
            } else if (player.gold >= 350) {
                // Secret Boss: Aurum Mimic Lord (Gold trigger challenge)
                val bossHp = 250 + (floor * 15)
                val bossAtk = 18 + (floor * 0.7).toInt()
                secretNode = AdventureNode(
                    index = targetIndex,
                    type = NodeType.COMBAT,
                    title = "SECRET: Aurum Mimic Loot King",
                    description = "The extreme heavy clinking of your high gold reserve has awakened the legendary Mimic King! Defeat him to double your gold stash!",
                    titleTr = "GİZLİ: Altın İstifçi Taklitçi Kralı",
                    descriptionTr = "Cebinizdeki devasa altın birikiminin şıngırtısı, uykusundaki Taklitçiler Kralını uyandırdı! Altınlarınızı korumak ve ganimetini kapmak için kılıcınızı çekin!",
                    enemyNameEn = "Golden Treasury Mimic Boss",
                    enemyNameTr = "Hazine Taklitçisi Kral Canavarı",
                    enemyHp = bossHp,
                    enemyMaxHp = bossHp,
                    enemyAtk = bossAtk,
                    willCost = 1
                )
            } else if (hasSpecialTitle || player.level >= 20) {
                // Secret Event: Old Hermit Celestial Telescope
                secretNode = AdventureNode(
                    index = targetIndex,
                    type = NodeType.NARRATIVE,
                    title = "SECRET: Celestial Wiseman Hermit",
                    description = "An ancient cloaked hermit steps out of obsidian walls, saluting your grand titles and battle-worn level. He unlocks forbidden space knowledge.",
                    titleTr = "GİZLİ: Kozmik Bilge Keşişen",
                    descriptionTr = "Kadim bir cüppeli keşiş obsidian taş duvarlardan fırlayarak şanlı unvanınızı ve yüksek seviyenizi selamladı. Size yasaklanmış kozmik sırlar ve irade fısıldıyor.",
                    optionA = NodeChoice(
                        textEn = "Study the celestial maps (+150 EXP, +4 Will)",
                        textTr = "Yıldız haritalarını çalış (+150 Deneyim, +4 İrade)",
                        journalEn = "Met the secret hermit and discovered cosmic navigation charts.",
                        journalTr = "Gizli keşişle buluşup kozmik nizam haritalarını çalıştın.",
                        willChange = 4,
                        expChange = 150
                    ),
                    optionB = NodeChoice(
                        textEn = "Sanctify weapons in Hermit's starlight (+Item)",
                        textTr = "Silahlarını bilge keşişin yıldız havuzunda takdis et (+Eşya)",
                        journalEn = "Received a clean high-tier item from the ancient hermit's telescope altar.",
                        journalTr = "Kadim keşişin yıldız sunağından yüksek kademe bir teçhizat kazandın.",
                        rewardItem = "Blight Purifier Plate",
                        expChange = 50
                    ),
                    optionC = NodeChoice(
                        textEn = "Recite heroic epics to gain massive alignment (+15 Align, +30 HP)",
                        textTr = "Kahramanlık destanlarını hisli oku (+15 Yönelim, +30 HP)",
                        journalEn = "Sang sacred ballads to the hermit to tune soul alignment.",
                        journalTr = "Keşişe kutsal ilahiler seslendirerek ruh yönelimini nizam tarafına çektin.",
                        hpChange = 30,
                        alignmentShift = 15
                    ),
                    willCost = 0
                )
            }
            
            if (secretNode != null) {
                nodes[targetIndex] = secretNode
            }
        }

        return nodes
    }

    private fun getBossForFloor(floor: Int, random: Random): EnemyStats {
        val bossList = when {
            floor <= 20 -> listOf(
                EnemyStats("Bone Grinder Gargole", "Kemik Kıran Taşlaşmış Gargoyle", 250, 15),
                EnemyStats("Infested Broodmother", "Çürük Yuva Anası", 280, 14)
            )
            floor <= 50 -> listOf(
                EnemyStats("Exarch Desolator", "Egzark Yıkım Elçisi", 450, 24),
                EnemyStats("Aegis Defiler", "Zırh Kirleten Musibet", 500, 22)
            )
            floor <= 80 -> listOf(
                EnemyStats("Shattered Archon", "Paramparça Kozmik Hükümdar", 750, 36),
                EnemyStats("Abyssal Blood-weaver", "Derinliklerin Kan Dokuyucusu", 800, 34)
            )
            else -> listOf(
                EnemyStats("Eldritch Void Terror", "Karanlık Ezeli Boşluk Felaketi", 1100, 48),
                EnemyStats("Avatar of the Core", "Kule Çekirdeğinin Avatarı", 1300, 52)
            )
        }
        val template = bossList[random.nextInt(bossList.size)]
        
        // Dynamically scale stats correctly based on exact floor
        val scaleHp = template.hp + (floor * 10)
        val scaleAtk = template.atk + (floor * 0.8).toInt()
        return template.copy(hp = scaleHp, atk = scaleAtk)
    }

    private fun getEnemyForFloor(floor: Int, random: Random): EnemyStats {
        val mobs = listOf(
            EnemyStats("Sewer Scuttler", "Leş Akrebi", 70, 8),
            EnemyStats("Blighted Shadow Wolf", "Musibetli Gölge Kurdu", 85, 10),
            EnemyStats("Void Parasite", "Boşluk Asalağı", 60, 12),
            EnemyStats("Corrupted Cultist", "Yozlaşmış Ahit Müridi", 90, 9),
            EnemyStats("Crystalline Spectre", "Parıltılı Hortlak", 80, 11),
            EnemyStats("Rogue Sentry Officer", "Kaçak Nöbetçi Subayı", 100, 8)
        )
        val template = mobs[random.nextInt(mobs.size)]
        val scaleHp = template.hp + (floor * 6)
        val scaleAtk = template.atk + (floor * 0.6).toInt()
        return template.copy(hp = scaleHp, atk = scaleAtk)
    }

    private fun generateChestNode(index: Int, floor: Int, random: Random): AdventureNode {
        val itemReward = getRandomLootItem(floor, random)
        return AdventureNode(
            index = index,
            type = NodeType.CHEST,
            title = "Blighted Relic Cache",
            description = "A heavy iron lockbox wrapped in blighted spores lies on a stone pedestal. It has ancient markings.",
            titleTr = "Lanetli Eski Eser Sandığı",
            descriptionTr = "Taş kürsüde çürük gözeneklerle sarılı ağır demir bir kilitli sandık duruyor. Üstünde antik işaretler kazınmış.",
            optionA = NodeChoice(
                textEn = "Force the lid with brute physical strength (+50 Gold)",
                textTr = "Kaba fiziksel kuvvetle kapağı kır (+50 Altın)",
                journalEn = "On Floor $floor, you forced a relic chest lock, recovering ancient relics.",
                journalTr = "$floor. Katta eski bir sandığı kırarak antik yadigarlar elde ettin.",
                goldChange = 50,
                expChange = 15,
                hpChange = -5 // slight scrap damage
            ),
            optionB = NodeChoice(
                textEn = "Purify spores with Light to reveal precious gear (+Item)",
                textTr = "Işık ile sporları arındır ve teçhizatı çıkar (+Eşya)",
                journalEn = "On Floor $floor, you sanctified a corrupted cache to earn a [$itemReward].",
                journalTr = "$floor. Katta lanetli sandığı arındırıp bir [$itemReward] çıkardın.",
                gleamChange = 20,
                expChange = 25,
                rewardItem = itemReward,
                alignmentShift = 5
            ),
            optionC = NodeChoice(
                textEn = "Shatter the hinges with Void decay (+Pyre)",
                textTr = "Boşluk çürümesiyle menteşeleri sök (+Pyre & -HP)",
                journalEn = "On Floor $floor, you used Abyssal force on a chest, reclaiming raw ash.",
                journalTr = "$floor. Katta bir sandığı Boşluk gücüyle parçalayarak saf küllerini topladın.",
                pyreChange = 30,
                hpChange = -15,
                expChange = 30,
                alignmentShift = -5
            )
        )
    }

    private fun generateShrineNode(index: Int, floor: Int, random: Random): AdventureNode {
        return AdventureNode(
            index = index,
            type = NodeType.SHRINE,
            title = "Sanctum monolith of Will",
            description = "A glowing stone obelisk projects safe cosmic energy. Interacting with it alters your spiritual essence.",
            titleTr = "Kutsal İrade Sütunu",
            descriptionTr = "Işıldayan bir taş dikilitaş etrafına güvenli kozmik enerji yayıyor. Onunla etkileşim kurmak ruhani özünüzü değiştirecektir.",
            optionA = NodeChoice(
                textEn = "Perform cleansing prayer (+30 HP, +2 Will)",
                textTr = "Arınma duasını icra et (+30 HP, +2 İrade)",
                journalEn = "On Floor $floor, you knelt at the monolith to cleanse your broken shell.",
                journalTr = "$floor. Katta dikilitaşın önünde diz çökerek kırık kabuğunu arındırdın.",
                hpChange = 30,
                willChange = 2,
                alignmentShift = 8
            ),
            optionB = NodeChoice(
                textEn = "Infuse blood into the altar to gather Abyss core (+40 Pyre)",
                textTr = "Sunağa kan akıtarak Boşluk özünü topla (+40 Kara Ateş)",
                journalEn = "On Floor $floor, you offered your lifeforce to feed the dark abyss.",
                journalTr = "$floor. Katta karanlık boşluğu beslemek için yaşam gücünü kurban ettin.",
                pyreChange = 40,
                hpChange = -15,
                alignmentShift = -8
            ),
            optionC = NodeChoice(
                textEn = "Meditate to restore core Focus (+4 Will)",
                textTr = "Meditasyon yaparak iradenizi topla (+4 İrade)",
                journalEn = "On Floor $floor, you meditated on a stone altar to recover soul Focus.",
                journalTr = "$floor. Katta taş mihrabın üstünde meditasyon yaparak ruh odağını tazeledin.",
                willChange = 4,
                expChange = 20
            )
        )
    }

    private fun generateMerchantNode(index: Int, floor: Int, random: Random): AdventureNode {
        val uniqueLoot = getRandomLootItem(floor, random)
        val titleAward = "Blight Breaker"
        return AdventureNode(
            index = index,
            type = NodeType.MERCHANT,
            title = "Nomadic Faction Outpost",
            description = "A neutral merchant caravan is bartering in this sector. They offer artifacts, rare gear, and titles.",
            titleTr = "Göçebe Cephe Karakolu",
            descriptionTr = "Tarafsız bir tüccar kervanı bu güvenli sektörde takas yapıyor. Antik eserler, nadir ekipmanlar ve rütbe nişanları teklif ediyorlar.",
            optionA = NodeChoice(
                textEn = "Buy Healing Elixir Potion (-30 Gold, +50 HP)",
                textTr = "Şifa İksiri İlacı Satın Al (-30 Altın, +50 HP)",
                journalEn = "Purchased a healing elixir from Nomadic Merchants.",
                journalTr = "Göçebe Tüccarlardan şifa iksiri satın aldın.",
                goldChange = -30,
                hpChange = 50
            ),
            optionB = NodeChoice(
                textEn = "Purchase unique gear artifact (-80 Gold, +Item)",
                textTr = "Nadir ekipman eserini edin (-80 Altın, +Eşya)",
                journalEn = "On Floor $floor, purchased a rare [$uniqueLoot].",
                journalTr = "$floor. Katta nadir bir [$uniqueLoot] satın aldın.",
                goldChange = -80,
                rewardItem = uniqueLoot
            ),
            optionC = NodeChoice(
                textEn = "Buy prestigious title credential (-120 Gold, +Title)",
                textTr = "Saygın unvan belgesi satın al (-120 Altın, +Unvan)",
                journalEn = "Acquired the Title [$titleAward] from merchant guild papers.",
                journalTr = "Tüccar birliğinden saygın [$titleAward] Unvan belgesi satın aldın.",
                goldChange = -120,
                rewardTitle = titleAward
            )
        )
    }

    private fun generateNarrativeNode(index: Int, floor: Int, random: Random): AdventureNode {
        val plotIndex = random.nextInt(3)
        return when (plotIndex) {
            0 -> AdventureNode(
                index = index,
                type = NodeType.NARRATIVE,
                title = "The Wounded Spires Knight",
                description = "A wounded Sanctum knight lies leaning against the obsidian wall, groaning in dark decay.",
                titleTr = "Yaralı Spires Şövalyesi",
                descriptionTr = "Yaralı bir Sanctum şövalyesi obsidiyen duvara yaslanmış, karanlık çürümenin acısıyla inliyor.",
                optionA = NodeChoice(
                    textEn = "Cast Holy Purify using your life essence (-10 HP, +Gleam)",
                    textTr = "Can özünü kullanarak kutsal arınma uygula (-10 HP, +Gleam)",
                    journalEn = "Healed the wounded knight of Spires, restoring Order.",
                    journalTr = "Yaralı şövalyeyi iyileştirerek ışık nizamını tazeledin.",
                    hpChange = -10,
                    gleamChange = 40,
                    alignmentShift = 6,
                    expChange = 25
                ),
                optionB = NodeChoice(
                    textEn = "End his agony & absorb his armor fragments (+40 Gold)",
                    textTr = "Izdırabına son ver ve zırh parçalarını yağmala (+40 Altın)",
                    journalEn = "You delivered a quick mercy kill to the knight to loot his gear.",
                    journalTr = "Ekipmanı yağmalamak için şövalyenin ızdırabına merhametlice son verdin.",
                    goldChange = 40,
                    alignmentShift = -6,
                    expChange = 20
                ),
                optionC = NodeChoice(
                    textEn = "Ask for advice on checking floor layout (+15 EXP)",
                    textTr = "Kat haritası hakkında tavsiye iste (+15 EXP)",
                    journalEn = "Obtained scout advice from the wounded soldier.",
                    journalTr = "Yaralı askerden kule kat haritası hakkında değerli bilgiler aldın.",
                    expChange = 15
                )
            )
            1 -> AdventureNode(
                index = index,
                type = NodeType.NARRATIVE,
                title = "Ancient Archive Archives",
                description = "Spiraling bookshelves are frozen in calcified crystal. A holographic codex of the Sovereign is humming.",
                titleTr = "Kadim Arşiv Kitaplığı",
                descriptionTr = "Kireçli kristalleşmiş raflarda sarmal kitaplar donakalmış. Holografik bir Hükümdar kutsal el yazması mırıldanıyor.",
                optionA = NodeChoice(
                    textEn = "Attune to Sanctum scripts to earn ancient knowledge (+Gleam)",
                    textTr = "Antik bilgiyi edinmek için kutsal kayıtlara odaklan (+Gleam)",
                    journalEn = "On Floor $floor, read historical Spires lore of the Sovereign.",
                    journalTr = "$floor. Katta Hükümdarın kutsal Beyaz Kule tarihini okudun.",
                    gleamChange = 30,
                    expChange = 30,
                    alignmentShift = 5
                ),
                optionB = NodeChoice(
                    textEn = "Corrupt database to extract Pyre embers (+Pyre, -HP)",
                    textTr = "Ateş közlerini çıkarmak için veritabanını boz (+Pyre, -HP)",
                    journalEn = "Hacked and drained the ancient monolith cores.",
                    journalTr = "Kadim obelisk çekirdeklerini hackleyip Void enerjisini emdin.",
                    pyreChange = 40,
                    hpChange = -10,
                    expChange = 30,
                    alignmentShift = -5
                ),
                optionC = NodeChoice(
                    textEn = "Study survival maps (+30 EXP)",
                    textTr = "Hayatta kalma haritalarını incele (+30 EXP)",
                    journalEn = "Read deep navigation records of the towers.",
                    journalTr = "Kulenin derin seyir seyir defterlerini çalıştın.",
                    expChange = 30
                )
            )
            else -> AdventureNode(
                index = index,
                type = NodeType.NARRATIVE,
                title = "Seeping Pool of Liquid Starlight",
                description = "Heavy silver fluid is dripping from the ceiling into a glowing stone basin. It whispers soft cosmic hums.",
                titleTr = "Işıldayan Yıldız Işığı Havuzu",
                descriptionTr = "Yoğun gümüş renkli bir sıvı tavandan ışıldayan taş bir leğene damlıyor. Yumuşak kozmik tınılar fısıldıyor.",
                optionA = NodeChoice(
                    textEn = "Lubricate blade for Sanctum sharpness (+Gleam, +EXP)",
                    textTr = "Sanctum keskinliği için kılıcını yağla (+Gleam, +EXP)",
                    journalEn = "Anointed your equipment in starlight fluid.",
                    journalTr = "Teçhizatınızı yıldız ışığı sıvısıyla takdis ettin.",
                    gleamChange = 25,
                    expChange = 20,
                    alignmentShift = 3
                ),
                optionB = NodeChoice(
                    textEn = "Drink fluid to unlock Abyss vitality (+25 HP, +Pyre)",
                    textTr = "Boşluk enerjisini açmak için sıvıyı iç (+25 HP, +Pyre)",
                    journalEn = "Gulped raw liquid cosmic gas, expanding your muscles.",
                    journalTr = "Musculus dokularını genişletmek için ham sıvı kozmik gazı içtin.",
                    hpChange = 25,
                    pyreChange = 20,
                    alignmentShift = -3
                ),
                optionC = NodeChoice(
                    textEn = "Dip your hands and wash weary eyes (+15 HP, +15 EXP)",
                    textTr = "Ellerini daldırıp yorgun gözlerini yıka (+15 HP, +15 EXP)",
                    journalEn = "Cleanised blight soot from your face.",
                    journalTr = "Musibetin pasını yüzünüzden temizlediniz.",
                    hpChange = 15,
                    expChange = 15
                )
            )
        }
    }

    fun getRandomLootItem(floor: Int, random: Random): String {
        val items = when {
            floor <= 30 -> listOf("Rustblade Dagger", "Sporeplated Helm", "Scout's Sigil", "Iron Girdle", "Gleam Coin Pendant")
            floor <= 70 -> listOf("Blightslayer Sabret", "Vanguard Aegis Shield", "Sanctified Greatsword", "Voidweave Tunic", "Pyre Signet Ring")
            else -> listOf("Sovereign Pentacle Sceptre", "Cosmic Will Band", "Blight Purifier Plate", "Archon Void Fang", "Celestial Halo Crown")
        }
        return items[random.nextInt(items.size)]
    }

    fun getRandomCombatTitle(floor: Int, random: Random): String {
        val titles = listOf(
            "Warlock of Frost", "Blight Slayer", "Shattered Champion", "Beacon of Grace",
            "Sovereign Vanguard", "Void Ascendant", "Eclipse Pursuer", "Iron Tower Lord"
        )
        return titles[random.nextInt(titles.size)]
    }
}
