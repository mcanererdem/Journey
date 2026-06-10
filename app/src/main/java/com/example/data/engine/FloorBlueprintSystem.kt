package com.example.data.engine

import com.example.data.model.PlayerProfile
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
        if (floor in 1..3) {
            val jsonBlueprint = loadBlueprintFromJson(floor)
            if (jsonBlueprint != null) {
                if (jsonBlueprint.nodes.size == 20) {
                    val expandedNodes = ArrayList<AdventureNode>()
                    val originalNodes = jsonBlueprint.nodes
                    for (d in 0..19) {
                        if (d == 0) {
                            expandedNodes.add(originalNodes[0].copy(index = 0, depth = 0, column = 0))
                        } else if (d == 19) {
                            expandedNodes.add(originalNodes[19].copy(index = 37, depth = 19, column = 0))
                        } else {
                            val originalNode = originalNodes[d]
                            // Left Column (column 0)
                            expandedNodes.add(originalNode.copy(index = 2 * d - 1, depth = d, column = 0))
                            // Right Column (column 1) - scale rewards and difficulty
                            val statsScaledNode = if (originalNode.type == NodeType.COMBAT) {
                                originalNode.copy(
                                    index = 2 * d,
                                    depth = d,
                                    column = 1,
                                    title = "Elite " + originalNode.title,
                                    titleTr = "Seçkin " + originalNode.titleTr,
                                    enemyHp = (originalNode.enemyHp * 1.3f).toInt(),
                                    enemyMaxHp = (originalNode.enemyMaxHp * 1.3f).toInt(),
                                    enemyAtk = (originalNode.enemyAtk * 1.3f).toInt(),
                                    enemyNameEn = "Elite " + originalNode.enemyNameEn,
                                    enemyNameTr = "Seçkin " + originalNode.enemyNameTr
                                )
                            } else {
                                val optA = originalNode.optionA?.copy(
                                    goldChange = (originalNode.optionA.goldChange * 1.5).toInt(),
                                    aetherChange = (originalNode.optionA.aetherChange * 1.5).toInt(),
                                    hpChange = (originalNode.optionA.hpChange * 1.5).toInt(),
                                    willChange = (originalNode.optionA.willChange * 1.5).toInt()
                                )
                                val optB = originalNode.optionB?.copy(
                                    goldChange = (originalNode.optionB.goldChange * 1.5).toInt(),
                                    aetherChange = (originalNode.optionB.aetherChange * 1.5).toInt(),
                                    hpChange = (originalNode.optionB.hpChange * 1.5).toInt(),
                                    willChange = (originalNode.optionB.willChange * 1.5).toInt()
                                )
                                val optC = originalNode.optionC?.copy(
                                    goldChange = (originalNode.optionC.goldChange * 1.5).toInt(),
                                    aetherChange = (originalNode.optionC.aetherChange * 1.5).toInt(),
                                    hpChange = (originalNode.optionC.hpChange * 1.5).toInt(),
                                    willChange = (originalNode.optionC.willChange * 1.5).toInt()
                                )
                                originalNode.copy(
                                    index = 2 * d,
                                    depth = d,
                                    column = 1,
                                    title = originalNode.title + " (Covenant Route)",
                                    titleTr = originalNode.titleTr + " (Ahit Rotası)",
                                    optionA = optA,
                                    optionB = optB,
                                    optionC = optC
                                )
                            }
                            expandedNodes.add(statsScaledNode)
                        }
                    }
                    return jsonBlueprint.copy(nodes = expandedNodes)
                }
                return jsonBlueprint
            }
        }
        return when (floor) {
            1 -> getFloor1Blueprint()
            2 -> getFloor2Blueprint()
            3 -> getFloor3Blueprint()
            else -> generateProceduralBlueprint(floor, player)
        }
    }

    // ==========================================
    // HANDCRAFTED FLOOR 1: THE BLIGHTED FOOTHILLS
    // ==========================================
    private fun getFloor1Blueprint(): FloorBlueprint {
        val floor = 1
        val titleEn = "The Blighted Foothills"
        val titleTr = "Musibetli Etekler"
        val descEn = "Your climb begins at the root. The atmosphere is damp, thick with blight spores and the skittering of infected creatures."
        val descTr = "Kule tırmanışın en dipten başlıyor. Nemli, musibet sporlarıyla ağırlaşmış havayı soluyor ve yozlaşmış yaratıkların tıkırtılarını duyuyorsun."

        val scenario = FloorScenario(
            floor = floor,
            titleEn = "Gateway to Ascendance",
            titleTr = "Yükseliş Geçidi",
            descriptionEn = "You stand before the massive iron seal of Floor 1. Cold mist swirls as the wind from the tower corridors whispers secrets.",
            descriptionTr = "1. Katın devasa demir mührü önündesin. Koridorlardan esen soğuk rüzgarlar zihnine sırlar fısıldarken pus girdap gibi dönüyor.",
            optionA = GameOption(
                textEn = "Pledge your blade to the Spires (+Aether, +Order)",
                textTr = "Kılıcını Ak Kuleler'e ada (+Aether, +Düzen)",
                alignmentShift = 10,
                aetherChange = 30,
                journalEn = "Pledged alignment to the Sanctum at the base gateway.",
                journalTr = "Giriş geçidinde Ak Kuleler idealine sadakat yemini ettiniz."
            ),
            optionB = GameOption(
                textEn = "Absorb the mold to harden your willpower (+Aether, +Void)",
                textTr = "İradeni çelikleştirmek için küfü içine çek (+Aether, +Boşluk)",
                alignmentShift = -10,
                aetherChange = 30,
                journalEn = "Absorbed decay energy to strengthen the dark soul.",
                journalTr = "Karanlık ruhu beslemek için odağı yozlaşma enerjisine çevirdiniz."
            ),
            optionC = GameOption(
                textEn = "Loot fallen scouts for equipment tokens (+Gold)",
                textTr = "Ekipman almak için düşmüş gözcüleri yağmala (+Altın)",
                alignmentShift = 0,
                goldChange = 60,
                journalEn = "Prioritized survival equipment tokens over spiritual alignments.",
                journalTr = "Maddi varlığı ve hayatta kalma teçhizatını ruhani yönelime tercih ettiniz."
            )
        )

        val nodes = listOf(
            AdventureNode(
                index = 0,
                type = NodeType.NARRATIVE,
                title = "Entrance Bastion",
                titleTr = "Giriş Kalesi",
                description = "Golden light leaks from broken wall cracks. You feel the pulse of the Core.",
                descriptionTr = "Yarık duvar çıtlamalarından altın bir ışık sızıyor. Çekirdeğin nabzını hisset.",
                optionA = NodeChoice(
                    textEn = "Recite a short purification mantra (+10 EXP)",
                    textTr = "Küçük bir arınma mantrası oku (+10 EXP)",
                    journalEn = "Began the climb on Floor 1 with focus and cleansing rituals.",
                    journalTr = "$floor. Kat tırmanışına zihni arındırarak adım attınız.",
                    expChange = 10
                ),
                optionB = NodeChoice(
                    textEn = "Smash the decorative statues for gold scraps (+20 Gold)",
                    textTr = "Altın parçaları için dekoratif heykelleri kır (+20 Altın)",
                    journalEn = "Smashed temple icons for quick currency values.",
                    journalTr = "Sunağın süslemelerini kırarak harcanabilir altın hurdası topladınız.",
                    goldChange = 20
                ),
                willCost = 0
            ),
            AdventureNode(
                index = 1,
                type = NodeType.COMBAT,
                title = "The Skittering Rat",
                titleTr = "Tıkırdayan Lağım Faresi",
                description = "A mutant rodent swathed in purple rot blocks the hallway.",
                descriptionTr = "Mor çürüklere bürünmüş dev bir mutasyonlu kemirgen koridoru kapatıyor.",
                enemyNameEn = "Infested Rat Scout",
                enemyNameTr = "Yozlaşmış Lağım Faresi Gözcüsü",
                enemyHp = 45,
                enemyMaxHp = 45,
                enemyAtk = 4,
                willCost = 1
            ),
            AdventureNode(
                index = 2,
                type = NodeType.CHEST,
                title = "Fallen Soldier's Cache",
                titleTr = "Düşmüş Askerin Zulası",
                description = "An old iron box covered in blight moss.",
                descriptionTr = "Musibet yosunlarıyla kaplanmış eski bir demir kutu.",
                optionA = NodeChoice(
                    textEn = "Open carefully (-1 Will, +Adventurer's Ring)",
                    textTr = "Dikkatlice aç (-1 İrade, +Maceracı Yüzüğü)",
                    journalEn = "Discovered an Adventurer's Ring in Flatlands cache on Floor 1.",
                    journalTr = "1. Katta düşmüş askerin kutusundan bir Maceracı Yüzüğü çıkardınız.",
                    willChange = -1,
                    rewardItem = "Adventurer's Ring"
                ),
                optionB = NodeChoice(
                    textEn = "Pry it open using gold picks (-15 Gold, +Scout Dagger)",
                    textTr = "Altın maymuncukla zorla (-15 Altın, +Gözcü Hançeri)",
                    journalEn = "Acquired a Scout Dagger by picking locks.",
                    journalTr = "Maymuncuk marifetiyle demir zula kutusundan Gözcü Hançeri aldınız.",
                    goldChange = -15,
                    rewardItem = "Scout Dagger"
                ),
                willCost = 1
            ),
            AdventureNode(
                index = 3,
                type = NodeType.SHRINE,
                title = "Altar of the Wild Hearth",
                titleTr = "Yabani Ocak Sunağı",
                description = "Warm warmth emanates from a brick fireplace glowing with holy flame.",
                descriptionTr = "Kutsal alevle parıldayan tuğla ocaktan sıcak bir ürperti yayılıyor.",
                optionA = NodeChoice(
                    textEn = "Pray for vitality (+25 HP)",
                    textTr = "Canlılık için dua et (+25 HP)",
                    journalEn = "Healed deep wounds at the Wild Hearth on Floor 1.",
                    journalTr = "Yabani Ocak alevinde yaralarınızı sarıp can kazandınız.",
                    hpChange = 25
                ),
                optionB = NodeChoice(
                    textEn = "Sacrifice blood to receive power (-10 HP, +15 Aether)",
                    textTr = "Güç elde etmek için kan ada (-10 HP, +15 Aether)",
                    journalEn = "Shed blood for the Sanctum's warm light on Floor 1.",
                    journalTr = "Lütuf ve ışık parıltısı için sunağa canından kan adadınız.",
                    hpChange = -10,
                    aetherChange = 15
                ),
                willCost = 1
            ),
            AdventureNode(
                index = 4,
                type = NodeType.BOSS,
                title = "Crown of Rat's Nest",
                titleTr = "Fare Yuvasının Tacı",
                description = "Golgoth, the Plague Rat King, sits bloated upon a pile of garbage and rusted scepters. He shrieks in hunger!",
                descriptionTr = "Devasa boyuttaki Veba Faresi Kralı Golgoth, paslı asalar ve çöp yığınından yapılmış tahtında açlıkla tıslıyor!",
                enemyNameEn = "Plague Rat King Golgoth",
                enemyNameTr = "Lağım Faresi Kralı Golgoth",
                enemyHp = 130,
                enemyMaxHp = 130,
                enemyAtk = 7,
                willCost = 2
            )
        )

        return FloorBlueprint(floor, titleEn, titleTr, descEn, descTr, scenario, nodes)
    }

    // ==========================================
    // HANDCRAFTED FLOOR 2: THE SHIMMERING CRYSTALS
    // ==========================================
    private fun getFloor2Blueprint(): FloorBlueprint {
        val floor = 2
        val titleEn = "The Crystal Spires"
        val titleTr = "Kristal Sütunlar"
        val descEn = "Silent stalactites of glowing crystal hum in deep darkness. Liquid ether leaks from crevices, gathering into glowing puddles."
        val descTr = "Karanlığın bağrında parıldayan kristal sarkıtlar sessizce mırıldanıyor. Çatlaklardan sızan sıvı ether parıltılı göletler oluşturuyor."

        val scenario = FloorScenario(
            floor = floor,
            titleEn = "The Shimmering Mirror",
            titleTr = "Işıldayan Ayna",
            descriptionEn = "You enter a vast cavern decorated with floating crystals. An ancient mirror pool reflects your inner reflection.",
            descriptionTr = "Havada süzülen dev kristallerle bezeli devasa bir mağaraya giriyorsun. Kadim ayna havuzu içsel benliğini yansıtıyor.",
            optionA = GameOption(
                textEn = "Focus the pure beam of light into your heart (+Aether, +Order)",
                textTr = "Ruhunu saf ışık huzmesiyle doldur (+Aether, +Düzen)",
                alignmentShift = 10,
                aetherChange = 40,
                journalEn = "Aligned with the crystal light on Floor 2.",
                journalTr = "2. Katta yansıtıcı kristal havuzunun saf enerjisiyle temizlendiniz."
            ),
            optionB = GameOption(
                textEn = "Crush a crystal to feed the dark void (+Aether, +Void)",
                textTr = "Karanlık boşluğu beslemek için bir kristal kır (+Aether, +Boşluk)",
                alignmentShift = -10,
                aetherChange = 40,
                journalEn = "Shattered crystals to draw shadow particles on Floor 2.",
                journalTr = "Sarkıt kristallerini kırıp açığa çıkan gölge parçacıklarını emdiniz."
            ),
            optionC = GameOption(
                textEn = "Mine glowing quartz cluster for trade value (+Gold)",
                textTr = "Ticari amaçla parıldayan kuvars madeni kaz (+Altın)",
                alignmentShift = 0,
                goldChange = 80,
                journalEn = "Harvested rare crystal quartz for wealth.",
                journalTr = "Ruhani işleri bir kenara bırakıp kıymetli kuvars damarlarını kazdınız."
            )
        )

        val nodes = listOf(
            AdventureNode(
                index = 0,
                type = NodeType.NARRATIVE,
                title = "Glittering Corridor",
                titleTr = "Işıltılı Koridor",
                description = "Each step you take triggers musical notes from the quartz floor.",
                descriptionTr = "Attığın her adım kuvars kaplı zeminden müzikal tınılar yükseltiyor.",
                optionA = NodeChoice(
                    textEn = "Listen to the harmony (+2 Will, +15 EXP)",
                    textTr = "Kozmik armoniye kulak ver (+2 İrade, +15 EXP)",
                    journalEn = "Sank into alignment harmony on Floor 2, recovering inner willpower.",
                    journalTr = "2. Katta kristallerin melodisini dinleyerek irade ve deneyim kazandınız.",
                    willChange = 2,
                    expChange = 15
                ),
                optionB = NodeChoice(
                    textEn = "Ignore and tread quickly (+10 EXP)",
                    textTr = "Görmezden gelip hızla geç (+10 EXP)",
                    journalEn = "Walked silently through the sonic quartz cave.",
                    journalTr = "Uğultulu kuvars mağarasından vakit kaybetmeden hızlıca geçtiniz.",
                    expChange = 10
                ),
                willCost = 0
            ),
            AdventureNode(
                index = 1,
                type = NodeType.COMBAT,
                title = "Sharp Crystalline Spid",
                titleTr = "Yarık Kristal Örümceği",
                description = "An arachnid body forged of sharp sapphire legs climbs down to strike.",
                descriptionTr = "Safir pençelerle bezeli sivri uçlu kristal örümcek pusu kurmuş bekliyor.",
                enemyNameEn = "Crystalline Shard Weaver",
                enemyNameTr = "Kristal Ağı Dokuyucusu",
                enemyHp = 70,
                enemyMaxHp = 70,
                enemyAtk = 9,
                willCost = 1
            ),
            AdventureNode(
                index = 2,
                type = NodeType.MERCHANT,
                title = "Nomadic Carver",
                titleTr = "Göçebe Oymacı",
                description = "A silent rogue wearing dense blindfolds carves crystal amulets.",
                descriptionTr = "Kalın göz bağları takmış sessiz bir göçmen parıltılı muskalar yontuyor.",
                optionA = NodeChoice(
                    textEn = "Buy Quartz Amulet (-50 Gold, +Quartz Guardian Signet)",
                    textTr = "Kuvars Muska al (-50 Altın, +Quartz Guardian Signet)",
                    journalEn = "Bought a Quartz Guardian Signet from carver rogue.",
                    journalTr = "Göçebe oymacıdan koruyucu Kuvars Mührü satın aldınız.",
                    goldChange = -50,
                    rewardItem = "Quartz Guardian Signet"
                ),
                optionB = NodeChoice(
                    textEn = "Exchange alignment for Aether potion (+15 Aether, -5 alignment)",
                    textTr = "Hizalamayı Aether iksirine dönüştür (+15 Aether, -5 faksiyon)",
                    journalEn = "Sacrificed minor integrity for Aether essence shards on Floor 2.",
                    journalTr = "Işık banyosu yapıp lütuf kazanmak için saflıktan taviz verdiniz.",
                    alignmentShift = -5,
                    aetherChange = 15
                ),
                optionC = NodeChoice(
                    textEn = "Wave farewell",
                    textTr = "Veda et ve ayrıl",
                    journalEn = "Declined nomadic barter offers.",
                    journalTr = "Tüccarın tekliflerini es geçip yola devam ettiniz."
                ),
                willCost = 1
            ),
            AdventureNode(
                index = 3,
                type = NodeType.SHRINE,
                title = "Crystalline Heart-Vein",
                titleTr = "Kristal Kalp Damarı",
                description = "A giant pulsing pink crystal node glows with warmth.",
                descriptionTr = "Devasa boyutlardaki, nabız gibi atan pembe kristal kütlesi ısı saçıyor.",
                optionA = NodeChoice(
                    textEn = "Bathe in the glowing ether to close wounds (+35 HP)",
                    textTr = "Yaraları iyileştirmek için eterde banyo yap (+35 HP)",
                    journalEn = "Absorbed raw pink crystal ether on Floor 2 to heal wounds.",
                    journalTr = "Can damarı sızan eterin parıltısıyla derin yaraları kapattınız.",
                    hpChange = 35
                ),
                optionB = NodeChoice(
                    textEn = "Siphon the crystal heart core for EXP (+50 EXP, -20 HP)",
                    textTr = "EXP için özü em (-20 HP, +50 EXP)",
                    journalEn = "Overheated the crystal vein, suffering recoil damage but amassing large knowledge.",
                    journalTr = "Ruhani bir şok pahasına damarın merkezini sömürerek yüksek deneyim emdiniz.",
                    hpChange = -20,
                    expChange = 50
                ),
                willCost = 1
            ),
            AdventureNode(
                index = 4,
                type = NodeType.BOSS,
                title = "The Spires Sentinel",
                titleTr = "Sütunların Nöbetçisi",
                description = "Clarith, the Shimmering Crystal Guardian, is a huge elemental golem made of levitating quartz boulders and blinding light beams.",
                descriptionTr = "Işıldayan Kristal Muhafız Clarith, havada süzülen kuvars blokları ve yakıcı ışınlardan yapılmış dev bir tılsımlı golem!",
                enemyNameEn = "Crystal Guardian Clarith",
                enemyNameTr = "Kristal Muhafız Clarith",
                enemyHp = 190,
                enemyMaxHp = 190,
                enemyAtk = 11,
                willCost = 2
            )
        )

        return FloorBlueprint(floor, titleEn, titleTr, descEn, descTr, scenario, nodes)
    }

    // ==========================================
    // HANDCRAFTED FLOOR 3: THE SHADOWED CATACOMBS
    // ==========================================
    private fun getFloor3Blueprint(): FloorBlueprint {
        val floor = 3
        val titleEn = "The Shadowed Catacombs"
        val titleTr = "Gölgeli Dehlizler"
        val descEn = "Cold, damp crypts of historical kings lie in ruin. Purple spectres and iron tombs decorate the shadowy catacombs."
        val descTr = "Tarihi kralların soğuk ve rutubetli mezarları harabe halinde uzanıyor. Mor hayaletler ve demir lahitler gölgeli dehlizleri süjelerle dolduruyor."

        val scenario = FloorScenario(
            floor = floor,
            titleEn = "The Hollow Crypt",
            titleTr = "Boş Lahit",
            descriptionEn = "You stumble into an ancient sanctuary holding the ashes of lost crusaders. Shadow figures dance of bygone memories, offering paths.",
            descriptionTr = "Kayıp kutsal şövalyelerin küllerini barındıran kadim bir mabede giriyorsun. Gölgeler kayıp anıları fisgıldayarak sana yol gösteriyor.",
            optionA = GameOption(
                textEn = "Recite the Sanctum's Prayer of Sealing (+Aether, +Order)",
                textTr = "Ak Sığınağın Mühür Dua'sını oku (+Aether, +Düzen)",
                alignmentShift = 10,
                aetherChange = 50,
                journalEn = "recited sealing prayers on Floor 3.",
                journalTr = "Dehlizlerin karanlığında mühür duaları fısıldayarak şövalye küllerini huzura erdirdiniz."
            ),
            optionB = GameOption(
                textEn = "Embrace the void spectres whispers (+Aether, +Void)",
                textTr = "Boşluk hayaletlerinin fısıltılarını kucakla (+Aether, +Boşluk)",
                alignmentShift = -10,
                aetherChange = 50,
                journalEn = "Sought power from void whispers on Floor 3.",
                journalTr = "Duvarlardan sızan mor hayalet fısıltılarına odaklanıp Ahit yolunu seçtiniz."
            ),
            optionC = GameOption(
                textEn = "Melt down antique iron coffin handles for bullion (+Gold)",
                textTr = "Külçe elde etmek için antika lahit halkalarını erit (+Altın)",
                alignmentShift = 0,
                goldChange = 100,
                journalEn = "Prioritized valuable scrap plunder over divine elements.",
                journalTr = "Eski lahit kollarını eritmek üzere sırt çantana doldurdun, altın kazandın."
            )
        )

        val nodes = listOf(
            AdventureNode(
                index = 0,
                type = NodeType.NARRATIVE,
                title = "Mausoleum Steps",
                titleTr = "Anıt Mezar Merdivenleri",
                description = "Heavy stone slabs block path, but your blade can pry open side cathedrals.",
                descriptionTr = "Ağır taş bloklar kapıyı kapatıyor ancak kılıcınla yan şapelleri zorlayabilirsin.",
                optionA = NodeChoice(
                    textEn = "Sacrifice blood to the iron lock mechanism (-15 HP, +2 Will)",
                    textTr = "Demir kilit çarkına kan canı feda et (-15 HP, +2 İrade)",
                    journalEn = "Opened the cathedrals with raw physical force.",
                    journalTr = "3. Katta anıt mezarı açmak için kan gücüyle döküm mekanizmasını çevirdiniz.",
                    hpChange = -15,
                    willChange = 2
                ),
                optionB = NodeChoice(
                    textEn = "Pry the rocks slowly (-1 Will, +15 EXP)",
                    textTr = "Kayaları yavaşça kanırt (-1 İrade, +15 EXP)",
                    journalEn = "Pried heavy catacomb seals manually.",
                    journalTr = "Güvenli yolla vakit kaybederek ağır blokları kas gücüyle araladınız.",
                    willChange = -1,
                    expChange = 15
                ),
                willCost = 0
            ),
            AdventureNode(
                index = 1,
                type = NodeType.COMBAT,
                title = "Undead Knight",
                titleTr = "Ölümsüz Muhafız Şövalye",
                description = "A skeletal legionnaire clanking rusty chainmail and sword challenges you.",
                descriptionTr = "Paslı zincir zırhlar içinde eski krallığı koruyan iskelet lejyoner yolunu kapatıyor.",
                enemyNameEn = "Skeletal Legionnaire Guard",
                enemyNameTr = "İskelet Muhafız Asker",
                enemyHp = 95,
                enemyMaxHp = 95,
                enemyAtk = 13,
                willCost = 1
            ),
            AdventureNode(
                index = 2,
                type = NodeType.CHEST,
                title = "Ancestor's Sarcophagus",
                titleTr = "Ata lahiti",
                description = "A glowing sarcophagus wrapped in heavy chains inscribed with runic binding.",
                descriptionTr = "Rün kaplı kalın zincirlerle sarılı parıldayan mermer lahit.",
                optionA = NodeChoice(
                    textEn = "Sunder the bindings (+Ancient Steel Wardplate)",
                    textTr = "Zincir bağlarını parçala (+Ancient Steel Wardplate)",
                    journalEn = "Looted Ancient Steel Wardplate from crypt sarcophagus on Floor 3.",
                    journalTr = "Rün zincirlerini kırarak ata lahitinden Antik Çelik Plaka Zırh çıkardınız.",
                    rewardItem = "Ancient Steel Wardplate"
                ),
                optionB = NodeChoice(
                    textEn = "Purify the tomb with Aether (-20 Aether, +Vesper's Cloak)",
                    textTr = "Mezarı Semavi Işıkla kutsayıp aç (-20 Aether, +Vesper's Cloak)",
                    journalEn = "Sanctified tomb to claim the Vesper's Cloak on Floor 3.",
                    journalTr = "Eski mezarı Işıkla takdis ederek paha biçilmez Gece Örtüsü aldınız.",
                    aetherChange = -20,
                    rewardItem = "Vesper's Cloak"
                ),
                willCost = 1
            ),
            AdventureNode(
                index = 3,
                type = NodeType.COMBAT,
                title = "Swirling Shadow Spect",
                titleTr = "Girdap Gölge Hortlağı",
                description = "Cold air condenses as a weeping shade swoops down from the catacomb cavern roof.",
                descriptionTr = "Soğuk hava aniden donuyor ve ağlayan karanlık bir hortlak kubbeden süzülerek saldırıyor.",
                enemyNameEn = "Abyssal Weeping Wraith",
                enemyNameTr = "Derinliğin Feryat Eden Hortlağı",
                enemyHp = 105,
                enemyMaxHp = 105,
                enemyAtk = 14,
                willCost = 1
            ),
            AdventureNode(
                index = 4,
                type = NodeType.BOSS,
                title = "Oracle of Shadow Crypt",
                titleTr = "Gölge Mahzen Kahini",
                description = "Malakar, the Necromancer Oracle, levitates atop a vortex of bone ash and void lightning. He aims to harvest your soul!",
                descriptionTr = "Kemik küllerinden girdap ve yıldırım yığını üzerinde süzülen Necromancer Kahini Malakar ruhunu feda etmek istiyor!",
                enemyNameEn = "Necromancer Oracle Malakar",
                enemyNameTr = "Gölge Kahini Malakar",
                enemyHp = 250,
                enemyMaxHp = 250,
                enemyAtk = 16,
                willCost = 2
            )
        )

        return FloorBlueprint(floor, titleEn, titleTr, descEn, descTr, scenario, nodes)
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

        // Establish 7 unique procedural theme categories
        val themeIndex = (floor % 7)
        when (themeIndex) {
            0 -> {
                titleEn = "The Blighted Vaults - F$floor"
                titleTr = "Musibetli Mahzenler - K$floor"
                descEn = "Thick purple roots of the Eternal Blight wrap around crumbling stone vaults. Skeletons lie frozen in eternal prayer."
                descTr = "Ebedi Çürüme'nin kalın mor kökleri ufalanan taş tonozları sarıyor. İskeletler ebedi duada donakalmış halde yatıyor."
            }
            1 -> {
                titleEn = "The Shimmering Mirror - F$floor"
                titleTr = "Işıldayan Ayna - K$floor"
                descEn = "A cosmic liquid pool reflects starlight from the peaks of the tower structure."
                descTr = "Kozmik sıvı havuzu, kule yapısının en tepe noktalarından süzülen yıldız ışıklarını yansıtıyor."
            }
            2 -> {
                titleEn = "The Ruined Shrine of Sealing - F$floor"
                titleTr = "Yıkık Mühürleme Tapınağı - K$floor"
                descEn = "A floating broken sanctuary collapsing slowly under severe void pressure conditions."
                descTr = "Şiddetli boşluk basıncı altında yavaşça çöken, havada süzülen yitik ve yıkık bir tapınak."
            }
            3 -> {
                titleEn = "The Spires Caravan - F$floor"
                titleTr = "Kuleler Kervanı - K$floor"
                descEn = "A neutral hub where nomadic traders barter specialized artifacts and whisper dark intel."
                descTr = "Göçebe tüccarların kıymetli eserler takas ettiği ve gizemli istihbaratlar fısıldadığı tarafsız durak."
            }
            4 -> {
                titleEn = "The Weeping Void Outpost - F$floor"
                titleTr = "Ağlayan Boşluk Karakolu - K$floor"
                descEn = "A scattered outpost of the Eclipse army, devastated by purifiers hunting void outcasts."
                descTr = "Boşluk sürgünlerini avlayan arındırıcılar tarafından yerle bir edilmiş yitik Ahit sığınağı."
            }
            5 -> {
                titleEn = "The Whispering Fount - F$floor"
                titleTr = "Fısıldayan Çeşme - K$floor"
                descEn = "Natural mineral rivers bubbling in sulfur fumes where echo memories whisper."
                descTr = "Kükürt buharları arasında fıkırdayan ve yankı anıları fısıldayan doğal şifalı mineral kaynakları."
            }
            else -> {
                titleEn = "The Iron Threshold - F$floor"
                titleTr = "Demir Eşik - K$floor"
                descEn = "Thick steel blast doors blocking the path forward, requiring tribute sacrifices to pass."
                descTr = "Yolu tıkayan ağır zırhlı kapılar; tüneli geçebilmek için haraç veya hizalamalı kurbanlar talep ediyor."
            }
        }

        // Construct standard scenario matching historical patterns
        val scenario = buildNormalScenario(floor, themeIndex)

        // Construct nodes List dynamically based on deterministic distribution
        val totalDepths = 20
        val nodes = ArrayList<AdventureNode>()

        val innerCount = 36 // 18 depths * 2 columns
        var combatCount = (innerCount * 0.40).toInt().coerceAtLeast(8)
        var merchantCount = (innerCount * 0.10).toInt().coerceAtLeast(4)
        var chestCount = (innerCount * 0.10).toInt().coerceAtLeast(4)
        var shrineCount = (innerCount * 0.10).toInt().coerceAtLeast(2)
        var narrativeCount = innerCount - combatCount - merchantCount - chestCount - shrineCount

        if (narrativeCount < 6) {
            narrativeCount = 6
            combatCount = (innerCount - merchantCount - chestCount - shrineCount - narrativeCount).coerceAtLeast(6)
        }

        val pool = ArrayList<NodeType>()
        repeat(combatCount) { pool.add(NodeType.COMBAT) }
        repeat(merchantCount) { pool.add(NodeType.MERCHANT) }
        repeat(chestCount) { pool.add(NodeType.CHEST) }
        repeat(shrineCount) { pool.add(NodeType.SHRINE) }
        repeat(narrativeCount) { pool.add(NodeType.NARRATIVE) }
        val shuffledPool = pool.shuffled(random)

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
            val type0 = shuffledPool[(d - 1) * 2]
            val type1 = shuffledPool[(d - 1) * 2 + 1]
            
            // Left Column (Column 0)
            nodes.add(generateProceduralNode(floor, 2 * d - 1, type0, random, d, 0))
            // Right Column (Column 1)
            nodes.add(generateProceduralNode(floor, 2 * d, type1, random, d, 1))
        }

        // Depth 19 (Boss)
        val bossInfo = getBossForFloor(floor, random)
        nodes.add(
            AdventureNode(
                index = 37,
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

    private fun buildNormalScenario(floor: Int, themeIndex: Int): FloorScenario {
        val (scenarioKey, formatFloorArg) = when {
            floor == 100 -> Pair("floor_100", false)
            floor % 25 == 0 -> Pair("exarch_council", true)
            floor % 10 == 0 -> Pair("arbiter_threshold", true)
            else -> Pair("theme_${floor % 7}", true)
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
            // Title and Description
            val titleEn = floorObj.optString("titleEn", "")
            val titleTr = floorObj.optString("titleTr", "")
            val descriptionEn = floorObj.optString("descriptionEn", "")
            val descriptionTr = floorObj.optString("descriptionTr", "")

            // Intro Scenario
            val introScenarioObj = floorObj.optJSONObject("introScenario") ?: return null
            val scenarioTitleEn = introScenarioObj.optString("titleEn", "")
            val scenarioTitleTr = introScenarioObj.optString("titleTr", "")
            val scenarioDescEn = introScenarioObj.optString("descriptionEn", "")
            val scenarioDescTr = introScenarioObj.optString("descriptionTr", "")

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
                val title = nodeObj.optString("title", "")
                val titleTr = nodeObj.optString("titleTr", "")
                val description = nodeObj.optString("description", "")
                val descriptionTr = nodeObj.optString("descriptionTr", "")

                val depth = nodeObj.optInt("depth", idx)
                val column = nodeObj.optInt("column", 0)

                val enemyNameEn = nodeObj.optString("enemyNameEn", "")
                val enemyNameTr = nodeObj.optString("enemyNameTr", "")
                val enemyHp = nodeObj.optInt("enemyHp", 0)
                val enemyMaxHp = nodeObj.optInt("enemyMaxHp", 0)
                val enemyAtk = nodeObj.optInt("enemyAtk", 0)

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
        return GameOption(
            textEn = obj.optString("textEn", ""),
            textTr = obj.optString("textTr", ""),
            alignmentShift = obj.optInt("alignmentShift", 0),
            goldChange = obj.optInt("goldChange", 0),
            aetherChange = obj.optInt("aetherChange", 0),
            hpChange = obj.optInt("hpChange", 0),
            journalEn = obj.optString("journalEn", ""),
            journalTr = obj.optString("journalTr", "")
        )
    }

    private fun parseNodeChoice(obj: JSONObject): NodeChoice {
        return NodeChoice(
            textEn = obj.optString("textEn", ""),
            textTr = obj.optString("textTr", ""),
            journalEn = obj.optString("journalEn", ""),
            journalTr = obj.optString("journalTr", ""),
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
