package dev.zwander.cellreader.utils


import java.util.*

//https://5g-tools.com/4g-lte-earfcn-calculator/
//https://www.sqimway.com/lte_band.php
//https://analog.intgckts.com/lte-carrier-frequency-and-earfcn/

//https://www.rfwireless-world.com/Terminology/UMTS-UARFCN-to-frequency-conversion.html
//https://www.sqimway.com/umts_band.php

object ARFCNTools {
    val earfcnTable = EARFCNTable
    val earfcnList = earfcnTable.keys.toList()

    val uarfcnTable = UARFCNTable
    val uarfcnList = uarfcnTable.keys.toList()

    @JvmStatic
    fun main(args: Array<String>) {
        val earfcn = 8665
        val uarfcn = 4390

        println(earfcnToInfo(earfcn))
        println(uarfcnToInfo(uarfcn))
    }

    fun earfcnToInfo(earfcn: Int): ARFCNInfo {
        val container = Collections.binarySearch(earfcnList, earfcn..earfcn) { o1, o2 ->
            if (o1.first >= o2.first && o1.last <= o2.last || o2.first >= o1.first && o2.last <= o1.last) {
                0
            } else {
                o1.first.compareTo(o2.first)
            }
        }

        return calculateEarfcnInfo(earfcn, earfcnTable[earfcnList[container]]!!)
    }

    fun uarfcnToInfo(uarfcn: Int): List<ARFCNInfo> {
        val containers = uarfcnList.filter { it.contains(uarfcn) }
            .map { it to uarfcnTable[it]!! }

        return calculateUarfcnInfo(uarfcn, containers)
    }

    private fun calculateEarfcnInfo(earfcn: Int, container: ARFCNContainer): ARFCNInfo {
        val fdl = container.dlLow.toDouble() + 0.1 * (earfcn - container.dlOffset.toDouble())
        // The normal formula is F-ul = F-ul-low + 0.1 * (EARFCN-ul - EARFCN-offset-ul).
        // Since we only have the dl EARFCN-dl, we need to calculate the EARFCN-ul, which is
        // EARFCN-ul = EARFCN-dl + (EARFCN-offset-ul - EARFCN-offset-dl). That simplifies
        // to what's below.
        val ful = if (container.ulLow != -1) {
            container.ulLow.toDouble() + 0.1 * (earfcn - container.dlOffset.toDouble())
        } else {
            -1
        }

        return ARFCNInfo(container.band, fdl, ful)
    }

    private fun calculateUarfcnInfo(uarfcn: Int, containers: List<Pair<IntRange, ARFCNContainer>>): List<ARFCNInfo> {
        return containers.map { (dlRange, info) ->
            ARFCNInfo(
                info.band,
                info.dlOffset.toDouble() + 0.2 * uarfcn,
                info.ulOffset.toDouble() + 0.2 * ((uarfcn - dlRange.first) + info.ulRange.first)
            )
        }
    }
}

data class ARFCNInfo(
    val band: Int,
    val dlFreq: Number,
    val ulFreq: Number,
)

data class ARFCNContainer(
    val dlLow: Number,
    val dlOffset: Number,
    val ulLow: Number,
    val ulOffset: Number,
    val band: Int,
    val ulRange: IntRange = IntRange(0, 0)
)

data class NRARFCNContainer(
    val band: Int
)

data class NRARFCNRefContainer(
    val frRange: IntRange,
    val dfGlobal: Int,
    val fRefOff: Number,
    val nRefOff: Int
)

object EARFCNTable : TreeMap<IntRange, ARFCNContainer>(
    { o1, o2 -> o1.first.compareTo(o2.first) }
) {
    init {
        putAll(
            listOf(
                0..599 to ARFCNContainer(
                    2110, 0,
                    1920, 18000,
                    1
                ),
                600..1199 to ARFCNContainer(
                    1930, 600,
                    1850, 18600,
                    2
                ),
                1200..1949 to ARFCNContainer(
                    1805, 1200,
                    1710, 19200,
                    3
                ),
                1950..2399 to ARFCNContainer(
                    2110, 1950,
                    1710, 19950,
                    4
                ),
                2400..2649 to ARFCNContainer(
                    869, 2400,
                    824, 20400,
                    5
                ),
                2750..3449 to ARFCNContainer(
                    2620, 2750,
                    2500, 20750,
                    7
                ),
                3450..3799 to ARFCNContainer(
                    925, 3450,
                    880, 21450,
                    8
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
                    1695, 132972,
                    70
                ),
                68586..68935 to ARFCNContainer(
                    617, 65586,
                    663, 133122,
                    71
                ),
                68936..68985 to ARFCNContainer(
                    461, 68936,
                    451, 133472,
                    72
                ),
                68986..69035 to ARFCNContainer(
                    460, 68986,
                    450, 133522,
                    73
                ),
                69036..69465 to ARFCNContainer(
                    1475, 69036,
                    1427, 133572,
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

object UARFCNTable : TreeMap<IntRange, ARFCNContainer>(
    { o1, o2 -> o1.first.compareTo(o2.first) }
) {
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
//            9500..9600 to ARFCNContainer(
//                1900, 9500,
//                1900, 9500,
//                33
//            ),
//            10050..10125 to ARFCNContainer(
//                2010, 10050,
//                2010, 10050,
//                34
//            ),
//            9250..9550 to ARFCNContainer(
//                1850, 9250,
//                1850, 9250,
//                35
//            ),
//            9650..9950 to ARFCNContainer(
//                1930, 9650,
//                1930, 9650,
//                36
//            ),
//            9550..9650 to ARFCNContainer(
//                1910, 9550,
//                1910, 9550,
//                37
//            ),
//            12850..13100 to ARFCNContainer(
//                2570, 12850,
//                2570, 12850,
//                38
//            ),
//            9400..9600 to ARFCNContainer(
//                1880, 9400,
//                1880, 9400,
//                39
//            ),
//            11500..12000 to ARFCNContainer(
//                2300, 11500,
//                2300, 11500,
//                40
//            )
        ))
    }
}

object NRARFCNRefTable : TreeMap<IntRange, NRARFCNRefContainer>() {
    init {
        putAll(listOf(
            0..599999 to NRARFCNRefContainer(
                0..3000, 5, 0, 0
            ),
            600000..2016666 to NRARFCNRefContainer(
                3000..24250, 15, 3000, 600000
            ),
            2016667..3279165 to NRARFCNRefContainer(
                24250..100000, 60, 24250.8, 2016667
            )
        ))
    }
}

//object NRARFCNTable : TreeMap<IntRange,