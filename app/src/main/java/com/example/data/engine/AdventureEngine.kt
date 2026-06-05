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
        val blueprint = FloorBlueprintSystem.getBlueprintForFloor(floor, player)
        return blueprint.nodes
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
