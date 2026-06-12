package com.mcanererdem.journey.data.engine

import com.mcanererdem.journey.data.model.PlayerProfile
import kotlin.random.Random
import org.json.JSONObject
import org.json.JSONArray

/**
 * Clean, scalable structural model representing an entire tower floor.
 * Decouples narrative, custom sectors, side quests, and special drops.
 */
data class FloorBlueprint(
    val floor: Int,
    val titleEn: String,
    val titleTr: String,
    val descriptionEn: String,
    val descriptionTr: String,
    val introScenario: FloorScenario,
    val nodes: List<AdventureNode>
)

object FloorBlueprintSystem {

    /**
     * Entrypoint to fetch the structural blueprint for any floor.
     * Handcrafts Floors 1-3, dynamically computes Floor 4-100 procedural tracks.
     */
    fun getBlueprintForFloor(floor: Int, player: PlayerProfile? = null): FloorBlueprint {
        val jsonBlueprint = loadBlueprintFromJson(floor)
        if (jsonBlueprint != null) {
            // Handcrafted nodes are already linear in the JSON, no need to expand them to columns.
            // We just ensure their index matches their position in the list.
            val linearNodes = jsonBlueprint.nodes.mapIndexed { index, node ->
                node.copy(index = index, depth = index, column = 0)
            }
            return jsonBlueprint.copy(nodes = linearNodes)
        }
        return generateProceduralBlueprint(floor, player)
    }

    // ==========================================
    // PROCEDURAL TRACK FOR FLOORS 4 TO 100
    // ==========================================
    private fun generateProceduralBlueprint(floor: Int, player: PlayerProfile?): FloorBlueprint {
        val floorSeed = floor.toLong() * 41235L + 56789L
        val random = Random(floorSeed)

        val titleEn: String
        val titleTr: String
        val descEn: String
        val descTr: String

        // Establish 10 unique procedural theme brackets (every 10 floors)
        val bracketIndex = ((floor - 1) / 10).coerceIn(0, 9)
        when (bracketIndex) {
            0 -> {
                titleEn = "The Blighted Foothills - F$floor"
                titleTr = "Musibetli Yamaçlar - K$floor"
                descEn = "The tower's base is choked by thick, pulsating roots and the stench of decay. Shadows skitter just out of sight."
                descTr = "Kulenin temeli kalın, zonklayan kökler ve çürüme kokusuyla boğulmuş durumda. Gölgeler gözden ırak yerlerde süzülüyor."
            }
            1 -> {
                titleEn = "The Sunken Necropolis - F$floor"
                titleTr = "Batık Nekropol - K$floor"
                descEn = "Damp stone corridors filled with the whispers of the forgotten. The air is cold and smells of wet earth and ancient bone."
                descTr = "Unutulmuşların fısıltılarıyla dolu nemli taş koridorlar. Hava soğuk, ıslak toprak ve kadim kemik kokuyor."
            }
            2 -> {
                titleEn = "The Scorched Spire - F$floor"
                titleTr = "Yanık Kule - K$floor"
                descEn = "Molten veins flow through the walls. The heat is oppressive, and the air is thick with volcanic ash and sulfur."
                descTr = "Duvarlardan erimiş damarlar akıyor. Baskıcı bir sıcaklık var ve hava volkanik kül ve kükürtle ağırlaşmış."
            }
            3 -> {
                titleEn = "The Whispering Gardens - F$floor"
                titleTr = "Fısıldayan Bahçeler - K$floor"
                descEn = "Lush, alien flora glows with a sickly light. Beautiful but deadly, the plants here hunger for more than just sunlight."
                descTr = "Gür, yabancı flora hastalıklı bir ışıkla parlıyor. Güzel ama ölümcül; buradaki bitkiler güneş ışığından fazlasına aç."
            }
            4 -> {
                titleEn = "The Crystal Desolation - F$floor"
                titleTr = "Kristal Issızlık - K$floor"
                descEn = "Jagged crystalline structures refract light into blinding patterns. It's a silent, beautiful, and frozen wasteland."
                descTr = "Pürüzlü kristal yapılar ışığı kör edici desenlere kırıyor. Sessiz, güzel ve donmuş bir ıssızlık."
            }
            5 -> {
                titleEn = "The Abyssal Archive - F$floor"
                titleTr = "Derinlik Arşivi - K$floor"
                descEn = "Infinite shelves of forbidden knowledge stretch into the darkness. The gravity here feels heavy with the weight of secrets."
                descTr = "Yasak bilgilerin sonsuz rafları karanlığa uzanıyor. Buradaki yerçekimi, sırların ağırlığıyla ağır hissediliyor."
            }
            6 -> {
                titleEn = "The Celestial Forge - F$floor"
                titleTr = "Semavi Demirhane - K$floor"
                descEn = "The air rings with the sound of phantom hammers. Golden sparks fly as cosmic energy is hammered into reality."
                descTr = "Hava, hayalet çekiçlerin sesiyle çınlıyor. Kozmik enerji gerçekliğe dövülürken altın kıvılcımlar uçuşuyor."
            }
            7 -> {
                titleEn = "The Void Sanctum - F$floor"
                titleTr = "Boşluk Tapınağı - K$floor"
                descEn = "Reality is thin here. Stars are visible through cracks in the floor. A place of deep meditation and terrifying power."
                descTr = "Burada gerçeklik oldukça ince. Yerdeki çatlaklardan yıldızlar görünüyor. Derin meditasyon ve dehşet verici bir güç yeri."
            }
            8 -> {
                titleEn = "The Mirror Dimension - F$floor"
                titleTr = "Ayna Boyutu - K$floor"
                descEn = "Everything is reflected and distorted. You see versions of yourself that never were, whispering of what could be."
                descTr = "Her şey yansıtılıyor ve çarpıtılıyor. Hiç var olmamış versiyonlarınızı görüyorsunuz, neler olabileceğini fısıldıyorlar."
            }
            else -> {
                titleEn = "The Sovereign Peak - F$floor"
                titleTr = "Hükümdar Zirvesi - K$floor"
                descEn = "The very top of the world. The Core is close now. The wind screams with the voices of all who failed to reach this height."
                descTr = "Dünyanın en tepesi. Çekirdek artık yakın. Rüzgar, bu yüksekliğe ulaşamayanların sesleriyle çığlık atıyor."
            }
        }

        // Construct standard scenario matching historical patterns
        val scenario = buildNormalScenario(floor, bracketIndex)

        // Construct nodes List dynamically based on deterministic distribution
        val totalDepths = 20
        val nodes = ArrayList<AdventureNode>()

        val innerCount = 18 // 18 depths * 1 column
        var combatCount = (innerCount * 0.40).toInt().coerceAtLeast(6)
        var merchantCount = (innerCount * 0.10).toInt().coerceAtLeast(2)
        var chestCount = (innerCount * 0.10).toInt().coerceAtLeast(2)
        var shrineCount = (innerCount * 0.10).toInt().coerceAtLeast(1)
        var narrativeCount = innerCount - combatCount - merchantCount - chestCount - shrineCount

        if (narrativeCount < 4) {
            narrativeCount = 4
            combatCount = (innerCount - merchantCount - chestCount - shrineCount - narrativeCount).coerceAtLeast(4)
        }

        val pool = ArrayList<NodeType>()
        repeat(combatCount) { pool.add(NodeType.COMBAT) }
        repeat(merchantCount) { pool.add(NodeType.MERCHANT) }
        repeat(chestCount) { pool.add(NodeType.CHEST) }
        repeat(shrineCount) { pool.add(NodeType.SHRINE) }
        repeat(narrativeCount) { pool.add(NodeType.NARRATIVE) }
        val shuffledPool = pool.shuffled(random).toMutableList()

        // Depth 0
        nodes.add(
            AdventureNode(
                index = 0,
                type = NodeType.NARRATIVE,
                title = "Floor $floor Nexus Vestibule",
                description = "Your heavy boots echo inside the cold gateway of Floor $floor.",
                titleTr = "${floor}. Kat Karşılama Geçidi",
                descriptionTr = "${floor}. Katın soğuk ana geçidinde adımlarınız yankılanıyor.",
                depth = 0,
                column = 0,
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

        // Depths 1 to 18
        for (d in 1..18) {
            val type = if (d == 10) NodeType.CAMP else shuffledPool.removeAt(0)
            nodes.add(generateProceduralNode(floor, d, type, random, d, 0))
        }

        // Depth 19 (Boss)
        val bossInfo = getBossForFloor(floor, random)
        nodes.add(
            AdventureNode(
                index = 19,
                type = NodeType.BOSS,
                title = "Floor $floor Overlord: ${bossInfo.nameEn}",
                description = "Guarding the cosmic seal is the Warden of Blight: ${bossInfo.nameEn}.",
                titleTr = "${floor}. Kat Derebeyi: ${bossInfo.nameTr}",
                descriptionTr = "Geçit mührünü koruyan azametli Musibet Gardiyanı ${bossInfo.nameTr} karşınızda kükrüyor.",
                depth = 19,
                column = 0,
                enemyNameEn = bossInfo.nameEn,
                enemyNameTr = bossInfo.nameTr,
                enemyHp = bossInfo.hp,
                enemyMaxHp = bossInfo.hp,
                enemyAtk = bossInfo.atk,
                willCost = 2
            )
        )

        // Apply secret content triggers exactly as done historically
        injectProceduralSecretContent(floor, nodes, player, random)

        return FloorBlueprint(floor, titleEn, titleTr, descEn, descTr, scenario, nodes)
    }

    private fun generateProceduralNode(floor: Int, index: Int, type: NodeType, random: Random, depth: Int, column: Int): AdventureNode {
        val isHard = column == 1
        return when (type) {
            NodeType.COMBAT -> {
                val stats = getEnemyForFloor(floor, random)
                val hp = if (isHard) (stats.hp * 1.3f).toInt() else stats.hp
                val atk = if (isHard) (stats.atk * 1.3f).toInt() else stats.atk
                AdventureNode(
                    index = index,
                    type = NodeType.COMBAT,
                    title = if (isHard) "Elite skirmish sector" else "Skirmish sector",
                    titleTr = if (isHard) "Seçkin Çatışma Sektörü" else "Çatışma Sektörü",
                    description = if (isHard) "A dangerous elite threat stands in your way. High risk, high reward!" else "An aggressive threat steps in from the shadows to drain your lifeforce.",
                    descriptionTr = if (isHard) "Yolunuzu kesen son derece tehlikeli seçkin bir düşman! Yüksek risk, yüksek ödül!" else "Gölgelerden sıyrılan agresif bir tehdit can enerjini sömürmek için saldırıyor.",
                    depth = depth,
                    column = column,
                    enemyNameEn = if (isHard) "Elite ${stats.nameEn}" else stats.nameEn,
                    enemyNameTr = if (isHard) "Seçkin ${stats.nameTr}" else stats.nameTr,
                    enemyHp = hp,
                    enemyMaxHp = hp,
                    enemyAtk = atk,
                    willCost = 1
                )
            }
            NodeType.CHEST -> {
                val multiplier = if (isHard) 1.5f else 1.0f
                AdventureNode(
                    index = index,
                    type = NodeType.CHEST,
                    title = if (isHard) "Elite Blighted Relic Urn" else "Blighted Relic Urn",
                    titleTr = if (isHard) "Seçkin Musibetli Kalıntı Lahiti" else "Musibetli Kalıntı Lahiti",
                    description = if (isHard) "A heavily warded chest. Greater rewards, but higher costs." else "A sealed decorative chest humming with celestial or dark currents.",
                    descriptionTr = if (isHard) "Ağır mühürlerle korunan kadim bir sandık. Daha büyük ödüller, fakat yüksek bedeller." else "Semavi veya karanlık dalgalarla mırıldayan kilitli kutsal bir mahfaza.",
                    depth = depth,
                    column = column,
                    optionA = NodeChoice(
                        textEn = "Unlock using light keys (+${(20 * multiplier).toInt()} Aether, -${(10 * multiplier).toInt()} HP)",
                        textTr = "Işık gücüyle aç (+${(20 * multiplier).toInt()} Aether, -${(10 * multiplier).toInt()} HP)",
                        journalEn = "Purified a sealed relic chest.",
                        journalTr = "Sıkışmış mühürlü sandığı can enerjisiyle temizleyip açtınız.",
                        hpChange = -(10 * multiplier).toInt(),
                        aetherChange = (20 * multiplier).toInt()
                    ),
                    optionB = NodeChoice(
                        textEn = "Break open roughly (+${(30 * multiplier).toInt()} Gold, -${(15 * multiplier).toInt()} HP)",
                        textTr = "Zorla parçala (+${(30 * multiplier).toInt()} Altın, -${(15 * multiplier).toInt()} HP)",
                        journalEn = "Smashed lock mechanisms for direct currency scrap loot.",
                        journalTr = "Zula kutusunu sertçe parçalayarak altını torbaya aktardınız.",
                        hpChange = -(15 * multiplier).toInt(),
                        goldChange = (30 * multiplier).toInt()
                    ),
                    willCost = 1
                )
            }
            NodeType.SHRINE -> {
                val multiplier = if (isHard) 1.5f else 1.0f
                AdventureNode(
                    index = index,
                    type = NodeType.SHRINE,
                    title = if (isHard) "Overcharged Obelisk" else "Aether Resonance obelisk",
                    titleTr = if (isHard) "Aşırı Yüklü Güç Dikilitaşı" else "Eter Rezonans Sütunu",
                    description = "A stone block radiating warm protective energy currents.",
                    descriptionTr = "Sıcak koruyucu enerji dalgaları saçan devasa yükselmiş dikilitaş.",
                    depth = depth,
                    column = column,
                    optionA = NodeChoice(
                        textEn = "Touch and channel focus (+${(30 * multiplier).toInt()} HP)",
                        textTr = "Dokun ve odaklan (+${(30 * multiplier).toInt()} HP)",
                        journalEn = "Restored cells at the Resonance Obelisk.",
                        journalTr = "Rezonans dikilitaşının can enerjisiyle dokuları yenilediniz.",
                        hpChange = (30 * multiplier).toInt()
                    ),
                    optionB = NodeChoice(
                        textEn = "Sacrifice willpower to harness Aether (-${(2 * multiplier).toInt()} Will, +${(40 * multiplier).toInt()} Aether)",
                        textTr = "Işıltı çekmek için iradeni feda et (-${(2 * multiplier).toInt()} İrade, +${(40 * multiplier).toInt()} Aether)",
                        journalEn = "Sacrificed willpower to channel spatial Aether.",
                        journalTr = "Eter rezonansını zorlamak için iradenizden feragat ettiniz.",
                        willChange = -(2 * multiplier).toInt(),
                        aetherChange = (40 * multiplier).toInt()
                    ),
                    willCost = 1
                )
            }
            NodeType.MERCHANT -> {
                val multiplier = if (isHard) 1.5f else 1.0f
                AdventureNode(
                    index = index,
                    type = NodeType.MERCHANT,
                    title = if (isHard) "Elite Dark Peddler" else "Wandering merchant",
                    titleTr = if (isHard) "Seçkin Gezgin Tüccar" else "Gezgin Tüccar",
                    description = "A mysterious vendor offering rare dimensional wares.",
                    descriptionTr = "Boyutsal teçhizatlar satan esrarengiz bir tüccar.",
                    depth = depth,
                    column = column,
                    optionA = NodeChoice(
                        textEn = "Buy health elixir (-${(25 * multiplier).toInt()} Gold, +30 HP)",
                        textTr = "Can iksiri satın al (-${(25 * multiplier).toInt()} Altın, +30 HP)",
                        journalEn = "Bought health elixir from wandering merchant.",
                        journalTr = "Gezgin satıcıdan hayat iksiri satın alıp yaralarınızı sardınız.",
                        goldChange = -(25 * multiplier).toInt(),
                        hpChange = 30
                    ),
                    optionB = NodeChoice(
                        textEn = "Trade spare metals for Aether (+${(15 * multiplier).toInt()} Aether, -${(40 * multiplier).toInt()} Gold)",
                        textTr = "Metal parçalarını Aether ile takas et (+${(15 * multiplier).toInt()} Aether, -${(40 * multiplier).toInt()} Altın)",
                        journalEn = "Traded gold coin reserves for cosmic Aether fragments.",
                        journalTr = "Gezgin tüccardan altın karşılığı Aether parçacıkları edindiniz.",
                        goldChange = -(40 * multiplier).toInt(),
                        aetherChange = (15 * multiplier).toInt()
                    ),
                    optionC = NodeChoice(
                        textEn = "Decline trade",
                        textTr = "Ticareti reddet",
                        journalEn = "Passed by the merchant without trading.",
                        journalTr = "Gezgin satıcının tekliflerini pas geçip ilerlemeyi seçtiniz."
                    ),
                    willCost = 1
                )
            }
            NodeType.CAMP -> {
                AdventureNode(
                    index = index,
                    type = NodeType.CAMP,
                    title = "Whispering Campfire",
                    description = "A warm, safe corner where the tower's corruption seems to fade. You can rest here to recover your strength.",
                    titleTr = "Fısıldayan Kamp Ateşi",
                    descriptionTr = "Kulenin yozlaşmasının azaldığı, sıcak ve güvenli bir köşe. Burada dinlenerek güç toplayabilirsiniz.",
                    depth = depth,
                    column = column,
                    optionA = NodeChoice(
                        textEn = "Rest and Meditate (+25 HP, +2 Will)",
                        textTr = "Dinlen ve Meditasyon Yap (+25 HP, +2 İrade)",
                        journalEn = "On Floor $floor, you rested at a campfire, recovering vital energy.",
                        journalTr = "$floor. Katta bir kamp ateşinde dinlenerek hayati enerjini topladın.",
                        hpChange = 25,
                        willChange = 2
                    ),
                    optionB = NodeChoice(
                        textEn = "Deep Sleep (+50 HP, -10 Gold)",
                        textTr = "Derin Uyku (+50 HP, -10 Altın)",
                        journalEn = "You spent some gold for a safer, deeper rest at the camp.",
                        journalTr = "Kampta daha güvenli ve derin bir dinlenme için biraz altın harcadın.",
                        hpChange = 50,
                        goldChange = -10
                    ),
                    optionC = NodeChoice(
                        textEn = "Tinker with Gear (+15 EXP, +10 Aether)",
                        textTr = "Teçhizatı Onar (+15 EXP, +10 Aether)",
                        journalEn = "Instead of resting, you spent time maintaining your gear.",
                        journalTr = "Dinlenmek yerine vaktini teçhizatının bakımına ayırdın.",
                        expChange = 15,
                        aetherChange = 10
                    ),
                    willCost = 0
                )
            }
            NodeType.NARRATIVE -> {
                AdventureNode(
                    index = index,
                    type = NodeType.NARRATIVE,
                    title = "Uncharted Ruins",
                    titleTr = "Keşfedilmemiş Harabeler",
                    description = "Eerie runes glow on a cracked pillar. A quiet voice promises power in exchange for your faith.",
                    descriptionTr = "Çatlamış bir sütunda tekinsiz rünler parlıyor. Sessiz bir ses inancına karşılık güç vaat ediyor.",
                    depth = depth,
                    column = column,
                    optionA = NodeChoice(
                        textEn = "Reject the voice (+10 Momentum, +20 EXP)",
                        textTr = "Sesi reddet (+10 Momentum, +20 EXP)",
                        journalEn = "Purified a whispering chaotic wall.",
                        journalTr = "Fısıldayan kaotik bir duvarı arındırıp Ak Kule yoluna yaklaştınız.",
                        alignmentShift = 10,
                        expChange = 20
                    ),
                    optionB = NodeChoice(
                        textEn = "Embrace the shadows (+15 Aether, -10 Momentum)",
                        textTr = "Gölgeleri kucakla (+15 Aether, -10 Momentum)",
                        journalEn = "Whispered secrets to the dark wall, feeding the void soul.",
                        journalTr = "Karanlık duvara sırlar fısıldayarak boşluk ruhunu beslediniz.",
                        alignmentShift = -10,
                        aetherChange = 15
                    ),
                    willCost = 0
                )
            }
            NodeType.BOSS -> {
                val stats = getEnemyForFloor(floor, random)
                AdventureNode(
                    index = index,
                    type = NodeType.BOSS,
                    title = "Apex Boss Sanctum",
                    titleTr = "Kozmik Zirve Mihrabı",
                    description = "The floor keeper blocks the portal to the higher floor. Prepare yourself!",
                    descriptionTr = "Kat muhafızı portalı kapatıyor. Kendini hazırla!",
                    depth = depth,
                    column = column,
                    enemyNameEn = stats.nameEn,
                    enemyNameTr = stats.nameTr,
                    enemyHp = stats.hp * 2,
                    enemyMaxHp = stats.hp * 2,
                    enemyAtk = (stats.atk * 1.5).toInt(),
                    willCost = 2
                )
            }
            else -> {
                // Default fallback for EVENT, SECRET etc.
                AdventureNode(
                    index = index,
                    type = NodeType.NARRATIVE,
                    title = "Uncharted Ruins",
                    titleTr = "Keşfedilmemiş Harabeler",
                    description = "Eerie runes glow on a cracked pillar. A quiet voice promises power in exchange for your faith.",
                    descriptionTr = "Çatlamış bir sütunda tekinsiz rünler parlıyor. Sessiz bir ses inancına karşılık güç vaat ediyor.",
                    depth = depth,
                    column = column,
                    optionA = NodeChoice(
                        textEn = "Reject the voice (+10 Momentum, +20 EXP)",
                        textTr = "Sesi reddet (+10 Momentum, +20 EXP)",
                        journalEn = "Purified a whispering chaotic wall.",
                        journalTr = "Fısıldayan kaotik bir duvarı arındırıp Ak Kule yoluna yaklaştınız.",
                        alignmentShift = 10,
                        expChange = 20
                    ),
                    optionB = NodeChoice(
                        textEn = "Embrace the shadows (+15 Aether, -10 Momentum)",
                        textTr = "Gölgeleri kucakla (+15 Aether, -10 Momentum)",
                        journalEn = "Whispered secrets to the dark wall, feeding the void soul.",
                        journalTr = "Karanlık duvara sırlar fısıldayarak boşluk ruhunu beslediniz.",
                        alignmentShift = -10,
                        aetherChange = 15
                    ),
                    willCost = 0
                )
            }
        }
    }

    private fun injectProceduralSecretContent(floor: Int, nodes: ArrayList<AdventureNode>, player: PlayerProfile?, random: Random) {
        if (player == null) return
        val targetIndex = (nodes.size * 0.6).toInt()
        val originalNode = nodes.getOrNull(targetIndex) ?: return
        val originalDepth = originalNode.depth
        val originalColumn = originalNode.column

        // 1. Secret Mimic Node
        if (player.gold > 200 && random.nextInt(100) < 30) {
            nodes[targetIndex] = AdventureNode(
                index = targetIndex,
                type = NodeType.COMBAT,
                title = "⚡ SECRET: Gilded Mimic ⚡",
                titleTr = "⚡ GİZLİ: Altın Dişli Taklitçi Chest ⚡",
                description = "Your sheer accumulation of gold has drawn a gluttonous creature disguised as a majestic chest. Kill it to seize massive loot!",
                descriptionTr = "Kesenizdeki yüksek altın miktarı, sandık kılığındaki obur bir açgözlü paraziti cezbetti! Onu öldürerek yüklü miktarda ganimet toplayın!",
                depth = originalDepth,
                column = originalColumn,
                enemyNameEn = "Gilded Vault Mimic",
                enemyNameTr = "Altın Kasalı Taklitçi canavar",
                enemyHp = 100 + (floor * 8),
                enemyMaxHp = 100 + (floor * 8),
                enemyAtk = 10 + (floor * 0.6).toInt(),
                willCost = 1
            )
        }
        // 2. Secret Shadow Event
        else if (player.momentum < 20 && random.nextInt(100) < 40) {
            nodes[targetIndex] = AdventureNode(
                index = targetIndex,
                type = NodeType.NARRATIVE,
                title = "⚡ SECRET: Hermit of Decay ⚡",
                titleTr = "⚡ GİZLİ: Musibetin Çürük Hermiti ⚡",
                description = "Recognizing your dark eclipse alignment, an outcast Void Hermit approaches holding forbidden scrolls.",
                descriptionTr = "Ruhundaki karanlık parıltıyı fark eden dışlanmış bir Boşluk Hermiti, yasaklanmış rün yazmaları tutarak sana doğru yanaşıyor.",
                depth = originalDepth,
                column = originalColumn,
                optionA = NodeChoice(
                    textEn = "Receive shadow knowledge (+45 EXP, +20 Aether, -5 alignment)",
                    textTr = "Kara ilmi sahiplen (+45 EXP, +20 Aether, -5 faksiyon)",
                    journalEn = "Studied under the Dark Void Hermit in secret.",
                    journalTr = "Gizli fısıltılar koridorunda Hermit'ten yasaklı boşluk ilmini edindiniz.",
                    expChange = 45,
                    aetherChange = 20,
                    alignmentShift = -5
                ),
                optionB = NodeChoice(
                    textEn = "Slay him to claim sanity reward (+20 HP, +40 Gold)",
                    textTr = "Hermit'i keserek akıl sağlığını koru (+20 HP, +40 Altın)",
                    journalEn = "Slew the void outcast Hermit to keep clean order.",
                    journalTr = "Düşkün Hermit'i infaz edip sığınağın düzenini ve ödül altınlarını korudunuz.",
                    hpChange = 20,
                    goldChange = 40
                ),
                willCost = 0
            )
        }
    }

    private fun getBossForFloor(floor: Int, random: Random): EnemyStats {
        val bracketIndex = ((floor - 1) / 10).coerceIn(0, 9)
        val bossList = when (bracketIndex) {
            0 -> listOf(
                EnemyStats("Root-Grown Horror", "Kökten Türeme Dehşet", 250, 15),
                EnemyStats("Blighted Broodmother", "Musibetli Yuva Anası", 280, 14)
            )
            1 -> listOf(
                EnemyStats("Drowned Archduke", "Boğulmuş Arşidük", 350, 20),
                EnemyStats("Crypt Warden", "Mezar Muhafızı", 380, 18)
            )
            2 -> listOf(
                EnemyStats("Magma Core Golem", "Magma Çekirdek Golemi", 450, 25),
                EnemyStats("Cinder Wyvern", "Kül Ejderi", 480, 24)
            )
            3 -> listOf(
                EnemyStats("Ancient Shambler", "Kadim Sendeleyen", 550, 30),
                EnemyStats("Floral Empress", "Çiçeksi İmparatoriçe", 580, 28)
            )
            4 -> listOf(
                EnemyStats("Shard-Heart Colossus", "Kristal Kalpli Kolos", 650, 35),
                EnemyStats("Glass Weaver", "Cam Dokuyucu", 680, 33)
            )
            5 -> listOf(
                EnemyStats("Grand Archivist", "Büyük Arşivci", 750, 40),
                EnemyStats("Tome-Bound Wraith", "Kitaba Bağlı Hortlak", 780, 38)
            )
            6 -> listOf(
                EnemyStats("Solaris Smith", "Solaris Demirci", 850, 45),
                EnemyStats("Celestial Guardian", "Semavi Gardiyan", 880, 43)
            )
            7 -> listOf(
                EnemyStats("Void Reaver", "Boşluk Yağmacısı", 950, 50),
                EnemyStats("Shadow Stalker", "Gölge Takipçisi", 980, 48)
            )
            8 -> listOf(
                EnemyStats("Mirror Doppelganger", "Ayna İkizi", 1050, 55),
                EnemyStats("Reflective Terror", "Yansıtıcı Dehşet", 1080, 53)
            )
            else -> listOf(
                EnemyStats("Avatar of the Core", "Çekirdeğin Avatarı", 1200, 65),
                EnemyStats("The Eternal Sovereign", "Ebedi Hükümdar", 1500, 70)
            )
        }
        val template = bossList[random.nextInt(bossList.size)]
        val scaleHp = template.hp + (floor * 10)
        val scaleAtk = template.atk + (floor * 0.8).toInt()
        return template.copy(hp = scaleHp, atk = scaleAtk)
    }

    private fun getEnemyForFloor(floor: Int, random: Random): EnemyStats {
        val bracketIndex = ((floor - 1) / 10).coerceIn(0, 9)
        val mobs = when (bracketIndex) {
            0 -> listOf(
                EnemyStats("Infested Rat", "Musibetli Fare", 70, 8),
                EnemyStats("Blighted Sprout", "Yozlaşmış Filiz", 75, 9),
                EnemyStats("Thorny Vine-Lurker", "Dikenli Sarmaşık Pusuya Yatan", 80, 10)
            )
            1 -> listOf(
                EnemyStats("Damp Skeleton", "Nemli İskelet", 90, 11),
                EnemyStats("Oozing Slime", "Sızan Balçık", 85, 12),
                EnemyStats("Grave Beetle", "Mezar Böceği", 95, 10)
            )
            2 -> listOf(
                EnemyStats("Ash Crawler", "Kül Sürüngeni", 110, 15),
                EnemyStats("Lava Imp", "Lav Zebanisi", 100, 16),
                EnemyStats("Obsidian Shard", "Obsidyen Parçası", 120, 14)
            )
            3 -> listOf(
                EnemyStats("Spore Cloud", "Spor Bulutu", 130, 18),
                EnemyStats("Petal-Blade Stalker", "Taç Yaprak Bıçaklı Avcı", 140, 20),
                EnemyStats("Tangled Roots", "Düğümlü Kökler", 150, 17)
            )
            4 -> listOf(
                EnemyStats("Crystalline Spider", "Kristal Örümcek", 160, 22),
                EnemyStats("Prism Golem", "Prizma Golemi", 180, 20),
                EnemyStats("Light-Bound Spectre", "Işığa Bağlı Hortlak", 170, 24)
            )
            5 -> listOf(
                EnemyStats("Ink-Stained Monk", "Mürekkep Lekeli Keşiş", 200, 26),
                EnemyStats("Paper-Cut Horror", "Kağıt Kesikli Dehşet", 190, 28),
                EnemyStats("Scroll Guardian", "Parşömen Muhafızı", 210, 25)
            )
            6 -> listOf(
                EnemyStats("Gilded Sentinel", "Altın Kaplama Gözcü", 230, 30),
                EnemyStats("Solar Flare Spirit", "Güneş Patlaması Ruhu", 220, 32),
                EnemyStats("Hammer-Hands", "Çekiç Elliler", 250, 28)
            )
            7 -> listOf(
                EnemyStats("Void Parasite", "Boşluk Asalağı", 260, 35),
                EnemyStats("Rift Creeper", "Yarık Sürüngeni", 270, 38),
                EnemyStats("Dark Matter Ooze", "Karanlık Madde Balçığı", 280, 33)
            )
            8 -> listOf(
                EnemyStats("Silver Echo", "Gümüş Yankı", 300, 40),
                EnemyStats("Fractured Image", "Kırık Görüntü", 310, 42),
                EnemyStats("Mirror Shard Gargoyle", "Ayna Parçası Gargoyle", 320, 38)
            )
            else -> listOf(
                EnemyStats("Celestial Zealot", "Semavi Bağnaz", 350, 45),
                EnemyStats("Abyssal Harbinger", "Derinlik Habercisi", 360, 48),
                EnemyStats("Sovereign Guard", "Hükümdar Muhafızı", 380, 42)
            )
        }
        val template = mobs[random.nextInt(mobs.size)]
        val scaleHp = template.hp + (floor * 6)
        val scaleAtk = template.atk + (floor * 0.6).toInt()
        return template.copy(hp = scaleHp, atk = scaleAtk)
    }

    private fun buildNormalScenario(floor: Int, bracketIndex: Int): FloorScenario {
        val scenarioKey = when {
            floor == 100 -> "floor_100"
            floor % 25 == 0 -> "exarch_council"
            floor % 10 == 0 -> "arbiter_threshold"
            else -> "bracket_${bracketIndex + 1}"
        }

        // Return a mock FloorScenario representation. LocalizationManager handles the standard
        // JSON file parsing of scenarios.
        return FloorScenario(
            floor = floor,
            titleEn = "Procedural Floor $floor",
            titleTr = "Prosedürel Kat $floor",
            descriptionEn = "Welcome to Floor $floor scenario.",
            descriptionTr = "Kat $floor hikayesine hoş geldiniz.",
            optionA = GameOption("Opt A", "Sec A", 10, journalEn = "Selected Opt A on Floor $floor", journalTr = "Katta Sec A seçildi"),
            optionB = GameOption("Opt B", "Sec B", -10, journalEn = "Selected Opt B on Floor $floor", journalTr = "Katta Sec B seçildi"),
            optionC = GameOption("Opt C", "Sec C", 0, journalEn = "Selected Opt C on Floor $floor", journalTr = "Katta Sec C seçildi")
        )
    }

    private fun loadBlueprintFromJson(floorNum: Int): FloorBlueprint? {
        try {
            val floorObj = LocalizationManager.loadFloorBlueprint(floorNum) ?: return null
            // Title and Description keys
            val titleKey = floorObj.optString("titleKey", "")
            val titleEn = if (titleKey.isNotEmpty()) LocalizationManager.getString("EN", titleKey) else floorObj.optString("titleEn", "")
            val titleTr = if (titleKey.isNotEmpty()) LocalizationManager.getString("TR", titleKey) else floorObj.optString("titleTr", "")
            
            val descKey = floorObj.optString("descriptionKey", "")
            val descriptionEn = if (descKey.isNotEmpty()) LocalizationManager.getString("EN", descKey) else floorObj.optString("descriptionEn", "")
            val descriptionTr = if (descKey.isNotEmpty()) LocalizationManager.getString("TR", descKey) else floorObj.optString("descriptionTr", "")

            // Intro Scenario keys
            val introScenarioObj = floorObj.optJSONObject("introScenario") ?: return null
            val scenarioTitleKey = introScenarioObj.optString("titleKey", "")
            val scenarioTitleEn = if (scenarioTitleKey.isNotEmpty()) LocalizationManager.getString("EN", scenarioTitleKey) else introScenarioObj.optString("titleEn", "")
            val scenarioTitleTr = if (scenarioTitleKey.isNotEmpty()) LocalizationManager.getString("TR", scenarioTitleKey) else introScenarioObj.optString("titleTr", "")
            
            val scenarioDescKey = introScenarioObj.optString("descriptionKey", "")
            val scenarioDescEn = if (scenarioDescKey.isNotEmpty()) LocalizationManager.getString("EN", scenarioDescKey) else introScenarioObj.optString("descriptionEn", "")
            val scenarioDescTr = if (scenarioDescKey.isNotEmpty()) LocalizationManager.getString("TR", scenarioDescKey) else introScenarioObj.optString("descriptionTr", "")

            val optionAObj = introScenarioObj.optJSONObject("optionA") ?: return null
            val optionBObj = introScenarioObj.optJSONObject("optionB") ?: return null
            val optionCObj = introScenarioObj.optJSONObject("optionC") ?: return null

            val optA = parseGameOption(optionAObj)
            val optB = parseGameOption(optionBObj)
            val optC = parseGameOption(optionCObj)

            val introScenario = FloorScenario(
                floor = floorNum,
                titleEn = scenarioTitleEn,
                titleTr = scenarioTitleTr,
                descriptionEn = scenarioDescEn,
                descriptionTr = scenarioDescTr,
                optionA = optA,
                optionB = optB,
                optionC = optC
            )

            // Nodes
            val nodesArr = floorObj.optJSONArray("nodes") ?: return null
            val nodesList = ArrayList<AdventureNode>()
            for (j in 0 until nodesArr.length()) {
                val nodeObj = nodesArr.getJSONObject(j)
                val idx = nodeObj.optInt("index", 0)
                val typeStr = nodeObj.optString("type", "NARRATIVE")
                val type = try { NodeType.valueOf(typeStr) } catch(e: Exception) { NodeType.NARRATIVE }
                
                val nodeTitleKey = nodeObj.optString("titleKey", "")
                val title = if (nodeTitleKey.isNotEmpty()) LocalizationManager.getString("EN", nodeTitleKey) else nodeObj.optString("titleEn", nodeObj.optString("title", ""))
                val titleTr = if (nodeTitleKey.isNotEmpty()) LocalizationManager.getString("TR", nodeTitleKey) else nodeObj.optString("titleTr", nodeObj.optString("title", ""))
                
                val nodeDescKey = nodeObj.optString("descriptionKey", "")
                val description = if (nodeDescKey.isNotEmpty()) LocalizationManager.getString("EN", nodeDescKey) else nodeObj.optString("descriptionEn", nodeObj.optString("description", ""))
                val descriptionTr = if (nodeDescKey.isNotEmpty()) LocalizationManager.getString("TR", nodeDescKey) else nodeObj.optString("descriptionTr", nodeObj.optString("description", ""))

                val depth = nodeObj.optInt("depth", idx)
                val column = nodeObj.optInt("column", 0)

                var enemyNameEn = nodeObj.optString("enemyNameEn", "")
                var enemyNameTr = nodeObj.optString("enemyNameTr", "")
                var enemyHp = nodeObj.optInt("enemyHp", 0)
                var enemyMaxHp = nodeObj.optInt("enemyMaxHp", 0)
                var enemyAtk = nodeObj.optInt("enemyAtk", 0)

                val enemyId = nodeObj.optString("enemyId", "")
                if (enemyId.isNotEmpty()) {
                    val globalEnemies = LocalizationManager.loadGlobalEnemies()
                    val enemyTmpl = globalEnemies?.optJSONObject(enemyId)
                    if (enemyTmpl != null) {
                        val nameKey = enemyTmpl.optString("nameKey", "")
                        enemyNameEn = if (nameKey.isNotEmpty()) LocalizationManager.getString("EN", nameKey) else enemyTmpl.optString("nameEn", enemyNameEn)
                        enemyNameTr = if (nameKey.isNotEmpty()) LocalizationManager.getString("TR", nameKey) else enemyTmpl.optString("nameTr", enemyNameTr)
                        enemyHp = enemyTmpl.optInt("hp", enemyHp)
                        enemyMaxHp = enemyTmpl.optInt("maxHp", enemyMaxHp)
                        enemyAtk = enemyTmpl.optInt("atk", enemyAtk)
                    }
                }

                val nodeOptAObj = nodeObj.optJSONObject("optionA")
                val nodeOptBObj = nodeObj.optJSONObject("optionB")
                val nodeOptCObj = nodeObj.optJSONObject("optionC")

                val nodeOptA = if (nodeOptAObj != null) parseNodeChoice(nodeOptAObj) else null
                val nodeOptB = if (nodeOptBObj != null) parseNodeChoice(nodeOptBObj) else null
                val nodeOptC = if (nodeOptCObj != null) parseNodeChoice(nodeOptCObj) else null

                val willCost = nodeObj.optInt("willCost", 0)

                nodesList.add(
                    AdventureNode(
                        index = idx,
                        type = type,
                        title = title,
                        description = description,
                        titleTr = titleTr,
                        descriptionTr = descriptionTr,
                        depth = depth,
                        column = column,
                        enemyNameEn = enemyNameEn,
                        enemyNameTr = enemyNameTr,
                        enemyHp = enemyHp,
                        enemyMaxHp = enemyMaxHp,
                        enemyAtk = enemyAtk,
                        optionA = nodeOptA,
                        optionB = nodeOptB,
                        optionC = nodeOptC,
                        willCost = willCost
                    )
                )
            }

            return FloorBlueprint(
                floor = floorNum,
                titleEn = titleEn,
                titleTr = titleTr,
                descriptionEn = descriptionEn,
                descriptionTr = descriptionTr,
                introScenario = introScenario,
                nodes = nodesList
            )
        } catch (e: Exception) {
            android.util.Log.e("FloorBlueprintSystem", "Error parsing floor JSON blueprint $floorNum", e)
        }
        return null
    }

    private fun parseGameOption(obj: JSONObject): GameOption {
        val textKey = obj.optString("textKey", "")
        val journalKey = obj.optString("journalKey", "")
        return GameOption(
            textEn = if (textKey.isNotEmpty()) LocalizationManager.getString("EN", textKey) else obj.optString("textEn", ""),
            textTr = if (textKey.isNotEmpty()) LocalizationManager.getString("TR", textKey) else obj.optString("textTr", ""),
            alignmentShift = obj.optInt("alignmentShift", 0),
            goldChange = obj.optInt("goldChange", 0),
            aetherChange = obj.optInt("aetherChange", 0),
            hpChange = obj.optInt("hpChange", 0),
            journalEn = if (journalKey.isNotEmpty()) LocalizationManager.getString("EN", journalKey) else obj.optString("journalEn", ""),
            journalTr = if (journalKey.isNotEmpty()) LocalizationManager.getString("TR", journalKey) else obj.optString("journalTr", "")
        )
    }

    private fun parseNodeChoice(obj: JSONObject): NodeChoice {
        val textKey = obj.optString("textKey", "")
        val journalKey = obj.optString("journalKey", "")
        return NodeChoice(
            textEn = if (textKey.isNotEmpty()) LocalizationManager.getString("EN", textKey) else obj.optString("textEn", ""),
            textTr = if (textKey.isNotEmpty()) LocalizationManager.getString("TR", textKey) else obj.optString("textTr", ""),
            journalEn = if (journalKey.isNotEmpty()) LocalizationManager.getString("EN", journalKey) else obj.optString("journalEn", ""),
            journalTr = if (journalKey.isNotEmpty()) LocalizationManager.getString("TR", journalKey) else obj.optString("journalTr", ""),
            hpChange = obj.optInt("hpChange", 0),
            goldChange = obj.optInt("goldChange", 0),
            aetherChange = obj.optInt("aetherChange", 0),
            expChange = obj.optInt("expChange", 0),
            alignmentShift = obj.optInt("alignmentShift", 0),
            willChange = obj.optInt("willChange", 0),
            rewardItem = obj.optString("rewardItem", ""),
            rewardTitle = obj.optString("rewardTitle", ""),
            requiredStoryFlag = obj.optString("requiredStoryFlag", ""),
            addStoryFlag = obj.optString("addStoryFlag", ""),
            skipToBoss = obj.optBoolean("skipToBoss", false),
            skipToNextFloor = obj.optBoolean("skipToNextFloor", false)
        )
    }
}
