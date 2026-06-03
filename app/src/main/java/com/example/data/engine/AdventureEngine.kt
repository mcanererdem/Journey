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
    val rewardTitle: String = "",
    val skipToBoss: Boolean = false,
    val skipToNextFloor: Boolean = false
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

        val nodes = ArrayList<AdventureNode>()

        // Specific procedural ratios for inner nodes (indices 1 to totalNodes - 2)
        val innerCount = totalNodes - 2
        var combatCount = (innerCount * 0.40).toInt().coerceAtLeast(3) // ~40% combat
        var merchantCount = (innerCount * 0.10).toInt().coerceAtLeast(1) // ~10% merchant
        var chestCount = (innerCount * 0.10).toInt().coerceAtLeast(1) // ~10% chests
        var shrineCount = (innerCount * 0.10).toInt().coerceAtLeast(1) // ~10% shrines
        var narrativeCount = innerCount - combatCount - merchantCount - chestCount - shrineCount

        if (narrativeCount < 2) {
            narrativeCount = 2
            combatCount = (innerCount - merchantCount - chestCount - shrineCount - narrativeCount).coerceAtLeast(1)
        }

        // Construct pool of node types according to exact ratios
        val typesPool = ArrayList<NodeType>()
        repeat(combatCount) { typesPool.add(NodeType.COMBAT) }
        repeat(merchantCount) { typesPool.add(NodeType.MERCHANT) }
        repeat(chestCount) { typesPool.add(NodeType.CHEST) }
        repeat(shrineCount) { typesPool.add(NodeType.SHRINE) }
        repeat(narrativeCount) { typesPool.add(NodeType.NARRATIVE) }

        val shuffledTypes = typesPool.shuffled(random)

        for (i in 0 until totalNodes) {
            when {
                i == 0 -> {
                    // Entry portal node: high immersion, safe greeting
                    nodes.add(
                        AdventureNode(
                            index = i,
                            type = NodeType.NARRATIVE,
                            title = "Floor $floor Nexus Vestibule",
                            description = "Your heavy boots echo inside the cold gateway of Floor $floor. The atmospheric decay whispers of ancient hidden secrets and shortcut portals.",
                            titleTr = "${floor}. Kat Karşılama Geçidi",
                            descriptionTr = "${floor}. Katın soğuk ana geçidinde adımlarınız yankılanıyor. Atmosferdeki kozmik dalgalanma antik sırlar, gizli tüneller ve boyut yırtığı kısayollarını fısıldıyor.",
                            optionA = NodeChoice(
                                textEn = "Focus mind to scan layout (+1 Will, +10 EXP)",
                                textTr = "Kule planını taramak için odaklan (+1 İrade, +10 EXP)",
                                journalEn = "Entered Floor $floor entryway, preparing spatial path plans.",
                                journalTr = "$floor. Kat kapısına giriş yaptınız, güzergahı zihninizde planladınız.",
                                willChange = 1,
                                expChange = 10
                            ),
                            optionB = NodeChoice(
                                textEn = "Sanctify physical energy (+15 HP)",
                                textTr = "Bedensel enerjiyi canlandır (+15 HP)",
                                journalEn = "Rested for a brief moment in the entryway, channeling safe light.",
                                journalTr = "Kat girişinde kısa bir an soluklanarak saf ışığı canınıza yüklediniz.",
                                hpChange = 15
                            ),
                            optionC = NodeChoice(
                                textEn = "Forge ahead confidently",
                                textTr = "Kendinden emin şekilde ileri atıl",
                                journalEn = "Stepped into the wild chambers of Floor $floor.",
                                journalTr = "$floor. Katın tehlikeli odalarına doğru kararlıca ilerlediniz."
                            ),
                            willCost = 0
                        )
                    )
                }
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
                else -> {
                    val nodeType = shuffledTypes[i - 1]
                    when (nodeType) {
                        NodeType.COMBAT -> {
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
                                    willCost = 1
                                )
                            )
                        }
                        NodeType.MERCHANT -> {
                            nodes.add(generateMerchantNode(i, floor, random))
                        }
                        NodeType.CHEST -> {
                            nodes.add(generateChestNode(i, floor, random))
                        }
                        NodeType.SHRINE -> {
                            nodes.add(generateShrineNode(i, floor, random))
                        }
                        else -> {
                            // Narrative. 25% chance of rolling a secret time rift shortcut node!
                            if (random.nextInt(100) < 25) {
                                nodes.add(generateShortcutNode(i, floor, random))
                            } else {
                                nodes.add(generateNarrativeNode(i, floor, random))
                            }
                        }
                    }
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
            description = "A neutral merchant caravan is bartering in this sector. They offer healing potions, rare artifacts, and dimension-bending seals.",
            titleTr = "Göçebe Cephe Karakolu",
            descriptionTr = "Tarafsız bir tüccar kervanı bu güvenli sektörde takas yapıyor. Şifa iksirleri, nadir ekipmanlar ve boyut bükücü mühürler teklif ediyorlar.",
            optionA = NodeChoice(
                textEn = "Buy Healing Elixir Potion (-30 Gold, +55 HP)",
                textTr = "Şifa İksiri İlacı Satın Al (-30 Altın, +55 HP)",
                journalEn = "Purchased a healing elixir from Nomadic Merchants.",
                journalTr = "Göçebe Tüccarlardan şifa iksiri satın aldın.",
                goldChange = -30,
                hpChange = 55
            ),
            optionB = NodeChoice(
                textEn = "Buy Overlord Chamber Key (-90 Gold, Teleport to Boss! 🌀)",
                textTr = "Derebeyi Oda Anahtarı Al (-90 Altın, Derebeye Işınlan! 🌀)",
                journalEn = "Bribed nomadic guild to obtain the Floor Overlord Chamber Key shortcut.",
                journalTr = "Göçebe birliğine rüşvet vererek Kat Derebeyi Odası Anahtarı kısayolunu aldınız.",
                goldChange = -90,
                skipToBoss = true
            ),
            optionC = NodeChoice(
                textEn = "Purchase rare gear artifact (-80 Gold, +Item)",
                textTr = "Nadir ekipman eserini edin (-80 Altın, +Eşya)",
                journalEn = "On Floor $floor, purchased a rare [$uniqueLoot].",
                journalTr = "$floor. Katta nadir bir [$uniqueLoot] satın aldın.",
                goldChange = -80,
                rewardItem = uniqueLoot
            )
        )
    }

    private fun generateShortcutNode(index: Int, floor: Int, random: Random): AdventureNode {
        return AdventureNode(
            index = index,
            type = NodeType.NARRATIVE,
            title = "🌀 SECRET: Quantum Temporal Crack",
            description = "A shimmering chronological fissure hover in the air. Time flows weirdly here. You can try to bend reality to skip forward!",
            titleTr = "🌀 GİZLİ: Zaman-Mekan Boyut Çatlağı",
            descriptionTr = "Tavanda göz alıcı boyutsal bir zaman çatlağı titreşiyor. Zaman burada bükülmüş durumda. Kulenin zirvelerine kestirmeden gitmek için bu yarığı kullanabilirsiniz!",
            optionA = NodeChoice(
                textEn = "Squeeze into Rift (Skip this Floor entirely! -1 Will, -10 HP)",
                textTr = "Boyut Yarığına Süzül (Bu Katı Komple Atla! -1 İrade, -10 HP)",
                journalEn = "Used the secret quantum temporal rift to skip Floor $floor entirely!",
                journalTr = "Gizli boyut çatlağını kullanarak $floor. Katı komple atladınız!",
                hpChange = -10,
                willChange = -1,
                skipToNextFloor = true
            ),
            optionB = NodeChoice(
                textEn = "Overload Rift Grid (Teleport straight to Floor Boss! -35 Gold)",
                textTr = "Yarığı Aşırı Yükle (Doğrudan Bölüm Sonu Canavarına Işınlan! -35 Altın)",
                journalEn = "Charged the spatial crack to teleport directly to Floor $floor Overlord's throne.",
                journalTr = "Boyut yarığını yükleyip doğrudan $floor. Kat Derebeyinin taht odasına ışınlandınız.",
                goldChange = -35,
                skipToBoss = true
            ),
            optionC = NodeChoice(
                textEn = "Absorb radiation heat (+20 Pyre, +20 Gleam, +15 EXP)",
                textTr = "Yarığın enerjisini emip güçlen (+20 Pyre, +20 Gleam, +15 EXP)",
                journalEn = "Absorbed raw spatial energy from the rift bounds.",
                journalTr = "Yarıktan sızan ham kozmik ışınları emerek ruhunu güçlendirdin.",
                pyreChange = 20,
                gleamChange = 20,
                expChange = 15
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
