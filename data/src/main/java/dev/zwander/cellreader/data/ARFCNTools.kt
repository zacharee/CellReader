package dev.zwander.cellreader.data


import android.os.Parcelable
import android.telephony.CellInfo
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.math.absoluteValue

//https://5g-tools.com/4g-lte-earfcn-calculator/
//https://www.sqimway.com/lte_band.php
//https://analog.intgckts.com/lte-carrier-frequency-and-earfcn/
//https://www.etsi.org/deliver/etsi_ts/136100_136199/136101/16.07.00_60/ts_136101v160700p.pdf (5.7.3-1)

//https://www.rfwireless-world.com/Terminology/UMTS-UARFCN-to-frequency-conversion.html
//https://www.sqimway.com/umts_band.php

//https://www.sqimway.com/umts_tdscdma.php

/**
 * TODO: This really needs to be stress-tested and
 * TODO: reviewed by people who know how this works.
 */
object ARFCNTools {
    private val earfcnTable = EARFCNTable
    private val earfcnList = earfcnTable.keys.toList()

    private val uarfcnTable = UARFCNTable
    private val uarfcnList = uarfcnTable.keys.toList()

    private val gsmarfcnTable = GSMARFCNTable
    private val gsmarfcnList = gsmarfcnTable.keys.toList()

    private val nrarfcnRefTable = NRARFCNRefTable
    private val nrarfcnRefList = nrarfcnRefTable.keys.toList()

    private val nrarfcnTable = NRARFCNTable
    private val nrarfcnList = nrarfcnTable.keys.toList()

    private val tdscdmaArfcnTable = TDSCDMAARFCNTable
    private val tdscdmaArfcnList = tdscdmaArfcnTable.keys.toList()

    @JvmStatic
    fun main(args: Array<String>) {
        val earfcn = 65537
        val uarfcn = 4390
        val gsmarfcn = 666
        val nrarfcn = 389000
        val tdscdmaarfcn = 9590

        println(earfcnToInfo(earfcn))
        println(uarfcnToInfo(uarfcn))
        println(gsmArfcnToInfo(gsmarfcn))
        println(nrArfcnToInfo(nrarfcn))
        println(tdscdmaArfcnToInfo(tdscdmaarfcn))
    }

    fun getInfo(arfcn: Int, type: Int): List<ARFCNInfo> {
        return when (type) {
            CellInfo.TYPE_GSM -> gsmArfcnToInfo(arfcn)
            CellInfo.TYPE_WCDMA -> uarfcnToInfo(arfcn)
            CellInfo.TYPE_TDSCDMA -> tdscdmaArfcnToInfo(arfcn)
            CellInfo.TYPE_LTE -> earfcnToInfo(arfcn)
            CellInfo.TYPE_NR -> nrArfcnToInfo(arfcn)
            else -> listOf()
        }
    }

    fun earfcnToInfo(earfcn: Int): List<ARFCNInfo> {
        val containers = earfcnList.filter { it.contains(earfcn) }
            .map { it to earfcnTable[it]!! }

        return calculateEarfcnInfo(earfcn, containers)
    }

    fun uarfcnToInfo(uarfcn: Int): List<ARFCNInfo> {
        val containers = uarfcnList.filter { it.contains(uarfcn) }
            .map { it to uarfcnTable[it]!! }

        return calculateUarfcnInfo(uarfcn, containers)
    }

    fun gsmArfcnToInfo(gsmarfcn: Int): List<ARFCNInfo> {
        val containers = gsmarfcnList.filter { it.contains(gsmarfcn) }
            .map { it to gsmarfcnTable[it]!! }

        return calculateGsmArfcnInfo(gsmarfcn, containers)
    }

    fun nrArfcnToInfo(nrarfcn: Int): List<ARFCNInfo> {
        val refContainer = nrarfcnRefTable[nrarfcnRefList.find { it.contains(nrarfcn) } ?: return emptyList()]!!
        val containers = nrarfcnList.filter { it.first.contains(nrarfcn) || it.second.contains(nrarfcn) }
            .map { it to nrarfcnTable[it]!! }

        return calculateNrArfcnInfo(nrarfcn, refContainer, containers)
    }

    fun tdscdmaArfcnToInfo(tdscdmaArfcn: Int): List<ARFCNInfo> {
        val containers = tdscdmaArfcnList.filter { it.contains(tdscdmaArfcn) }
            .map { it to tdscdmaArfcnTable[it]!! }

        return calculateTdscdmaArfcnInfo(tdscdmaArfcn, containers)
    }

    private fun calculateEarfcnInfo(earfcn: Int, containers: List<Pair<IntRange, ARFCNContainer>>): List<ARFCNInfo> {
        return containers.map { (_, info) ->
            ARFCNInfo(
                info.band.toString(),
                info.dlLow.toDouble() + 0.1 * (earfcn - info.dlOffset.toDouble()),
                // The normal formula is F-ul = F-ul-low + 0.1 * (EARFCN-ul - EARFCN-offset-ul).
                // Since we only have the dl EARFCN-dl, we need to calculate the EARFCN-ul, which is
                // EARFCN-ul = EARFCN-dl + (EARFCN-offset-ul - EARFCN-offset-dl). That simplifies
                // to what's below.
                if (info.ulLow != -1) {
                    info.ulLow.toDouble() + 0.1 * (earfcn - info.dlOffset.toDouble())
                } else {
                    -1
                }
            )
        }
    }

    private fun calculateUarfcnInfo(uarfcn: Int, containers: List<Pair<IntRange, ARFCNContainer>>): List<ARFCNInfo> {
        return containers.map { (dlRange, info) ->
            ARFCNInfo(
                info.band.toString(),
                info.dlOffset.toDouble() + 0.2 * uarfcn,
                info.ulOffset.toDouble() + 0.2 * ((uarfcn - dlRange.first) + info.ulRange.first)
            )
        }
    }

    private fun calculateGsmArfcnInfo(gsmarfcn: Int, containers: List<Pair<IntRange, GSMARFCNContainer>>): List<ARFCNInfo> {
        return containers.map { (_, info) ->
            val ful = info.fulEq(gsmarfcn)
            val fdl = info.fdlEq(ful)

            ARFCNInfo(
                info.band,
                fdl, ful
            )
        }
    }

    private fun calculateNrArfcnInfo(nrarfcn: Int, refContainer: NRARFCNRefContainer, containers: List<Pair<Pair<IntRange, IntRange>, ARFCNContainer>>): List<ARFCNInfo> {
        return containers.map { (ranges, info) ->
            val (dlRange, ulRange) = ranges
            val firstDl = dlRange.first
            val firstUl = ulRange.first

            val fdl = if (firstDl != -1) {
                refContainer.fRefOff.toDouble() + refContainer.dfGlobal.toDouble() * (nrarfcn - refContainer.nRefOff.toDouble())
            } else {
                -1
            }
            val ful = if (firstUl != -1) {
                val realDl = if (firstDl == -1) firstUl else firstDl

                refContainer.fRefOff.toDouble() + refContainer.dfGlobal.toDouble() * ((realDl - firstUl).absoluteValue + nrarfcn - refContainer.nRefOff.toDouble())
            } else {
                -1
            }

            ARFCNInfo(
                info.band.toString(),
                fdl,
                ful
            )
        }
    }

    private fun calculateTdscdmaArfcnInfo(tdscdmaArfcn: Int, containers: List<Pair<IntRange, TDSCDMAARFCNContainer>>): List<ARFCNInfo> {
        return containers.map { (_, info) ->
            ARFCNInfo(
                info.band,
                tdscdmaArfcn / 5.0,
                tdscdmaArfcn / 5.0
            )
        }
    }
}

@Parcelize
data class ARFCNInfo(
    val band: String,
    val dlFreq: Number,
    val ulFreq: Number
) : Parcelable

data class ARFCNContainer(
    val dlLow: Number = 0,
    val dlOffset: Number = 0,
    val ulLow: Number = 0,
    val ulOffset: Number = 0,
    val band: Int,
    val ulRange: IntRange = IntRange(0, 0)
)

data class GSMARFCNContainer(
    val fulEq: (Number) -> Number,
    val fdlEq: (Number) -> Number,
    val band: String
)

data class TDSCDMAARFCNContainer(
    val fLow: Number,
    val fHigh: Number,
    val band: String
)

data class NRARFCNRefContainer(
    val frRange: IntRange,
    val dfGlobal: Number,
    val fRefOff: Number,
    val nRefOff: Int
)

object EARFCNTable : TreeMap<IntRange, ARFCNContainer>(IntRangeComparator) {
    init {
        putAll(
            listOf(
                0..599 to ARFCNContainer(
                    2110, 0,
                    1920, 18000,
                    1,
                ),
                600..1199 to ARFCNContainer(
                    1930, 600,
                    1850, 18600,
                    2,
                ),
                1200..1949 to ARFCNContainer(
                    1805, 1200,
                    1710, 19200,
                    3,
                ),
                1950..2399 to ARFCNContainer(
                    2110, 1950,
                    1710, 19950,
                    4,
                ),
                2400..2649 to ARFCNContainer(
                    869, 2400,
                    824, 20400,
                    5,
                ),
                2750..3449 to ARFCNContainer(
                    2620, 2750,
                    2500, 20750,
                    7,
                ),
                3450..3799 to ARFCNContainer(
                    925, 3450,
                    880, 21450,
                    8,
                ),
                3800..4149 to ARFCNContainer(
                    1844.9, 3800,
                    1749.9, 21800,
                    9
                ),
                4150..4749 to ARFCNContainer(
                    2110, 4150,
                    1710, 22150,
                    10
                ),
                4750..4949 to ARFCNContainer(
                    1475.9, 4750,
                    1427.9, 22750,
                    11
                ),
                5010..5179 to ARFCNContainer(
                    729, 5010,
                    699, 23010,
                    12
                ),
                5180..5279 to ARFCNContainer(
                    746, 5180,
                    777, 23180,
                    13
                ),
                5280..5379 to ARFCNContainer(
                    758, 5280,
                    788, 23280,
                    14
                ),
                5730..5849 to ARFCNContainer(
                    734, 5730,
                    704, 23730,
                    17
                ),
                5850..5999 to ARFCNContainer(
                    860, 5850,
                    815, 23850,
                    18
                ),
                6000..6149 to ARFCNContainer(
                    875, 6000,
                    830, 24000,
                    19
                ),
                6150..6449 to ARFCNContainer(
                    791, 6150,
                    832, 24150,
                    20
                ),
                6450..6599 to ARFCNContainer(
                    1495.9, 6450,
                    1447.9, 23850,
                    21
                ),
                6600..7399 to ARFCNContainer(
                    3510, 6600,
                    3410, 24600,
                    22
                ),
                7700..8039 to ARFCNContainer(
                    1525, 7700,
                    1626.5, 25700,
                    24
                ),
                8040..8689 to ARFCNContainer(
                    1930, 8040,
                    1850, 26040,
                    25
                ),
                8690..9039 to ARFCNContainer(
                    859, 8690,
                    814, 26690,
                    26
                ),
                9040..9209 to ARFCNContainer(
                    852, 9040,
                    807, 27040,
                    27
                ),
                9210..9659 to ARFCNContainer(
                    758, 9210,
                    703, 27210,
                    28
                ),
                9660..9769 to ARFCNContainer(
                    717, 9660,
                    -1, -1,
                    29
                ),
                9770..9869 to ARFCNContainer(
                    2350, 9770,
                    2305, 27660,
                    30
                ),
                9870..9919 to ARFCNContainer(
                    462.5, 9870,
                    452.5, 27760,
                    31
                ),
                9920..10359 to ARFCNContainer(
                    1452, 9920,
                    -1, -1,
                    32
                ),
                36000..36199 to ARFCNContainer(
                    1900, 3600,
                    1900, 3600,
                    33
                ),
                36200..36349 to ARFCNContainer(
                    2010, 36200,
                    2010, 36200,
                    34
                ),
                36350..36949 to ARFCNContainer(
                    1850, 36350,
                    1850, 36350,
                    35
                ),
                36950..37549 to ARFCNContainer(
                    1930, 36950,
                    1930, 36950,
                    36
                ),
                37550..37749 to ARFCNContainer(
                    1910, 37550,
                    1910, 37550,
                    37
                ),
                37750..38249 to ARFCNContainer(
                    2570, 37750,
                    2570, 37750,
                    38
                ),
                38250..38649 to ARFCNContainer(
                    1880, 38250,
                    1880, 38250,
                    39
                ),
                38650..39649 to ARFCNContainer(
                    2300, 38650,
                    2300, 38650,
                    40
                ),
                39650..41589 to ARFCNContainer(
                    2496, 39650,
                    2496, 39650,
                    41
                ),
                41590..43589 to ARFCNContainer(
                    3400, 41590,
                    3400, 41590,
                    42
                ),
                43590..45589 to ARFCNContainer(
                    3600, 43590,
                    3600, 43590,
                    43
                ),
                45590..46589 to ARFCNContainer(
                    703, 45590,
                    703, 45590,
                    44
                ),
                46590..46789 to ARFCNContainer(
                    1447, 46590,
                    1447, 46590,
                    45
                ),
                46790..54539 to ARFCNContainer(
                    5150, 46790,
                    5150, 46790,
                    46
                ),
                54540..55239 to ARFCNContainer(
                    5855, 54540,
                    5855, 54540,
                    47
                ),
                55240..56739 to ARFCNContainer(
                    3550, 55240,
                    3550, 55240,
                    48
                ),
                56740..58239 to ARFCNContainer(
                    3550, 56740,
                    3550, 56740,
                    49
                ),
                58240..59089 to ARFCNContainer(
                    1432, 58240,
                    1432, 58240,
                    50
                ),
                59090..59139 to ARFCNContainer(
                    1427, 59090,
                    1427, 59090,
                    51
                ),
                59140..60139 to ARFCNContainer(
                    3300, 59140,
                    3300, 59140,
                    52
                ),
                60140..60254 to ARFCNContainer(
                    2483.5, 60140,
                    2483.5, 60140,
                    53
                ),
                65536..66435 to ARFCNContainer(
                    2110, 65536,
                    1920, 131072,
                    65
                ),
                66436..67335 to ARFCNContainer(
                    2110, 66436,
                    1710, 131972,
                    66
                ),
                67336..67535 to ARFCNContainer(
                    738, 67336,
                    -1, -1,
                    67
                ),
                67536..67835 to ARFCNContainer(
                    753, 67536,
                    698, 132672,
                    68
                ),
                67836..68335 to ARFCNContainer(
                    2570, 67836,
                    -1, -1,
                    69
                ),
                68336..68585 to ARFCNContainer(
                    1995, 68336,
                    1695, 68336 + 65536,
                    70
                ),
                68586..68935 to ARFCNContainer(
                    617, 68586,
                    663, 68586 + 65536,
                    71
                ),
                68936..68985 to ARFCNContainer(
                    461, 68936,
                    451, 68936 + 65536,
                    72
                ),
                68986..69035 to ARFCNContainer(
                    460, 68986,
                    450, 68986 + 65536,
                    73
                ),
                69036..69465 to ARFCNContainer(
                    1475, 69036,
                    1427, 69036 + 65536,
                    74
                ),
                69466..70315 to ARFCNContainer(
                    1432, 69466,
                    -1, -1,
                    75
                ),
                70316..70365 to ARFCNContainer(
                    1427, 70316,
                    -1, -1,
                    76
                ),
                70336..70545 to ARFCNContainer(
                    728, 70366,
                    698, 134002,
                    85
                ),
                70546..70595 to ARFCNContainer(
                    420, 70546,
                    410, 134182,
                    87
                ),
                70596..70645 to ARFCNContainer(
                    422, 70596,
                    412, 134232,
                    88
                )
            )
        )
    }
}

object UARFCNTable : TreeMap<IntRange, ARFCNContainer>(IntRangeComparator) {
    init {
        putAll(listOf(
            10562..10838 to ARFCNContainer(
                2110, 0,
                1920, 0,
                1,
                9612..9888
            ),
            9662..9938 to ARFCNContainer(
                1930, 0,
                1850, 0,
                2,
                9262..9538
            ),
            1162..1513 to ARFCNContainer(
                1805, 1575,
                1710, 1525,
                3,
                937..1288
            ),
            1537..1738 to ARFCNContainer(
                2110, 1805,
                1710, 1450,
                4,
                1312..1513
            ),
            4357..4458 to ARFCNContainer(
                869, 0,
                824, 0,
                5,
                4132..4233
            ),
            4387..4413 to ARFCNContainer(
                875, 0,
                830, 0,
                6,
                4162..4188
            ),
            2237..2563 to ARFCNContainer(
                2620, 2175,
                2500, 2100,
                7,
                2012..2338
            ),
            2937..3088 to ARFCNContainer(
                925, 340,
                880, 340,
                8,
                2712..2863
            ),
            9237..9387 to ARFCNContainer(
                1845, 0,
                1750, 0,
                9,
                8762..8912
            ),
            3112..3388 to ARFCNContainer(
                2110, 1490,
                1710, 1135,
                10,
                2887..3163
            ),
            3712..3787 to ARFCNContainer(
                1476, 736,
                1428, 733,
                11,
                3487..3562
            ),
            3842..3903 to ARFCNContainer(
                729, -37,
                699,  -22,
                12,
                3617..3678
            ),
            4017..4043 to ARFCNContainer(
                746, -55,
                777, 21,
                13,
                3792..3818
            ),
            4117..4143 to ARFCNContainer(
                758, -63,
                788, 12,
                14,
                3892..3918
            ),
            712..763 to ARFCNContainer(
                875, 735,
                830, 770,
                19,
                312..363
            ),
            4512..4638 to ARFCNContainer(
                791, -109,
                832, -23,
                20,
                4287..4413
            ),
            862..912 to ARFCNContainer(
                1496, 1326,
                1448, 1358,
                21,
                462..512
            ),
            4662..5038 to ARFCNContainer(
                3510, 2580,
                3410, 2525,
                22,
                4437..4813
            ),
            5112..5413 to ARFCNContainer(
                1930, 910,
                1850, 875,
                25,
                4887..5188
            ),
            5762..5913 to ARFCNContainer(
                859, -291,
                814, -291,
                26,
                5537..5688
            ),
            6617..6813 to ARFCNContainer(
                1452, 131,
                -1, -1,
                32
            ),
        ))
    }
}

object TDSCDMAARFCNTable : TreeMap<IntRange, TDSCDMAARFCNContainer>(IntRangeComparator) {
    init {
        putAll(listOf(
            9500..9600 to TDSCDMAARFCNContainer(
                1900, 1920, "A (low)"
            ),
            10050..10125 to TDSCDMAARFCNContainer(
                2010, 2025, "A (high)"
            ),
            9250..9550 to TDSCDMAARFCNContainer(
                1850, 1910, "B (low)"
            ),
            9650..9950 to TDSCDMAARFCNContainer(
                1930, 1990, "B (high)"
            ),
            9550..9650 to TDSCDMAARFCNContainer(
                1910, 1930, "C"
            ),
            12850..13100 to TDSCDMAARFCNContainer(
                2570, 2620, "D"
            ),
            9400..9600 to TDSCDMAARFCNContainer(
                1880, 1920, "F"
            ),
            11500..12000 to TDSCDMAARFCNContainer(
                2300, 2400, "E"
            )
        ))
    }
}

object NRARFCNRefTable : TreeMap<IntRange, NRARFCNRefContainer>(IntRangeComparator) {
    init {
        putAll(listOf(
            0..599999 to NRARFCNRefContainer(
                0..3000, 0.005, 0, 0
            ),
            600000..2016666 to NRARFCNRefContainer(
                3000..24250, 0.015, 3000, 600000
            ),
            2016667..3279165 to NRARFCNRefContainer(
                24250..100000, 0.060, 24250.8, 2016667
            )
        ))
    }
}

object NRARFCNTable : TreeMap<Pair<IntRange, IntRange>, ARFCNContainer>(IntRangePairComparator) {
    init {
        putAll(
            listOf(
                (422000..434000 to 384000..396000) to ARFCNContainer(band = 1),
                (386000..398000 to 370000..382000) to ARFCNContainer(band = 2),
                (361000..376000 to 342000..357000) to ARFCNContainer(band = 3),
                (173800..178800 to 164800..169800) to ARFCNContainer(band = 5),
                (524000..538000 to 500000..514000) to ARFCNContainer(band = 7),
                (185000..192000 to 176000..183000) to ARFCNContainer(band = 8),
                (145800..149200 to 139800..143200) to ARFCNContainer(band = 12),
                (149200..151200 to 155400..157400) to ARFCNContainer(band = 13),
                (151600..153600 to 157600..159600) to ARFCNContainer(band = 14),
                (172000..173500 to 163000..166000) to ARFCNContainer(band = 18),
                (158200..164200 to 166400..172400) to ARFCNContainer(band = 20),
                (305000..311800 to 325300..332100) to ARFCNContainer(band = 24),
                (386000..399000 to 370000..383000) to ARFCNContainer(band = 25),
                (171800..178800 to 162800..169800) to ARFCNContainer(band = 26),
                (151600..160600 to 140600..149600) to ARFCNContainer(band = 28),
                (143400..145600 to -1..-1) to ARFCNContainer(band = 29),
                (470000..472000 to 461000..463000) to ARFCNContainer(band = 30),
                (402000..405000 to 402000..405000) to ARFCNContainer(band = 34),
                (514000..524000 to 514000..524000) to ARFCNContainer(band = 38),
                (376000..384000 to 376000..384000) to ARFCNContainer(band = 39),
                (460000..480000 to 460000..480000) to ARFCNContainer(band = 40),
                (499200..537999 to 499200..537999) to ARFCNContainer(band = 41),
                (743333..795000 to 743333..795000) to ARFCNContainer(band = 46),
                (636667..646666 to 636667..646666) to ARFCNContainer(band = 48),
                (286400..303400 to 286400..303400) to ARFCNContainer(band = 50),
                (285400..286400 to 285400..286400) to ARFCNContainer(band = 51),
                (496700..499000 to 496700..499000) to ARFCNContainer(band = 53),
                (422000..440000 to 384000..402000) to ARFCNContainer(band = 65),
                (422000..440000 to 342000..356000) to ARFCNContainer(band = 66),
                (147600..151600 to -1..-1) to ARFCNContainer(band = 67),
                (399000..404000 to 339000..342000) to ARFCNContainer(band = 70),
                (123400..130400 to 132600..139600) to ARFCNContainer(band = 71),
                (295000..303600 to 285400..294000) to ARFCNContainer(band = 74),
                (286400..303400 to -1..-1) to ARFCNContainer(band = 75),
                (285400..286400 to -1..-1) to ARFCNContainer(band = 76),
                (620000..680000 to 620000..680000) to ARFCNContainer(band = 77),
                (620000..653333 to 620000..653333) to ARFCNContainer(band = 78),
                (693334..733333 to 693334..733333) to ARFCNContainer(band = 79),
                (-1..-1 to 342000..357000) to ARFCNContainer(band = 80),
                (-1..-1 to 176000..183000) to ARFCNContainer(band = 81),
                (-1..-1 to 166400..172400) to ARFCNContainer(band = 82),
                (-1..-1 to 140600..149600) to ARFCNContainer(band = 83),
                (-1..-1 to 384000..396000) to ARFCNContainer(band = 84),
                (145600..149200 to 139600..143200) to ARFCNContainer(band = 85),
                (-1..-1 to 342000..356000) to ARFCNContainer(band = 86),
                (-1..-1 to 164800..169800) to ARFCNContainer(band = 89),
                (499200..538000 to 499200..538000) to ARFCNContainer(band = 90),
                (285400..286400 to 166400..172400) to ARFCNContainer(band = 91),
                (286400..303400 to 166400..172400) to ARFCNContainer(band = 92),
                (285400..286400 to 176000..183000) to ARFCNContainer(band = 93),
                (286400..303400 to 176000..183000) to ARFCNContainer(band = 94),
                (-1..-1 to 402000..405000) to ARFCNContainer(band = 95),
                (795000..875000 to 795000..875000) to ARFCNContainer(band = 96),
                (-1..-1 to 460000..480000) to ARFCNContainer(band = 97),
                (-1..-1 to 376000..384000) to ARFCNContainer(band = 98),
                (-1..-1 to 325300..332100) to ARFCNContainer(band = 99),
                (2054166..2104165 to 2054166..2104165) to ARFCNContainer(band = 257),
                (2016667..2070832 to 2016667..2070832) to ARFCNContainer(band = 258),
                (2270832..2337499 to 2270832..2337499) to ARFCNContainer(band = 259),
                (2229166..2279165 to 2229166..2279165) to ARFCNContainer(band = 260),
                (2070833..2084999 to 2070833..2084999) to ARFCNContainer(band = 261),
                (2399166..2415832 to 2399166..2415832) to ARFCNContainer(band = 262)
            )
        )
    }
}

object GSMARFCNTable : TreeMap<IntRange, GSMARFCNContainer>(IntRangeComparator) {
    init {
        putAll(listOf(
            259..293 to GSMARFCNContainer(
                { 450.6 + 0.2 * (it.toDouble() - 259) },
                { it.toDouble() + 10 },
                "GSM 450"
            ),
            306..340 to GSMARFCNContainer(
                { 479+0.2*(it.toDouble()-306) },
                { it.toDouble() + 10 },
                "GSM 480"
            ),
            128..251 to GSMARFCNContainer(
                { 824.2+0.2*(it.toDouble()-128) },
                { it.toDouble() + 45 },
                "GSM 850"
            ),
            1..124 to GSMARFCNContainer(
                { 890 + 0.2 * it.toDouble() },
                { it.toDouble() + 45 },
                "P-GSM"
            ),
            975..1023 to GSMARFCNContainer(
                { 890+0.2*(it.toDouble()-1024) },
                { it.toDouble() + 45 },
                "E-GSM"
            ),
            0..124 to GSMARFCNContainer(
                { 890+0.2*(it.toDouble()) },
                { it.toDouble() + 45 },
                "E-GSM"
            ),
            955..1023 to GSMARFCNContainer(
                { 890+0.2*(it.toDouble()-1024) },
                { it.toDouble() + 45 },
                "GSM-R"
            ),
            512..885 to GSMARFCNContainer(
                { 1710.2+0.2*(it.toDouble()-512) },
                { it.toDouble() + 95 },
                "DCS 1800"
            ),
            512..810 to GSMARFCNContainer(
                { 1850.2 + 0.2*(it.toDouble()-512) },
                { it.toDouble() + 80 },
                "PCS 1900"
            ),
            0..124 to GSMARFCNContainer(
                { 890 + 0.2 * it.toDouble() },
                { it.toDouble() + 80 },
                "R-GSM"
            ),
            955..1023 to GSMARFCNContainer(
                { 890 + 0.2 * (it.toDouble() - 1024) },
                { it.toDouble() + 45 },
                "R-GSM"
            )
        ))
    }
}