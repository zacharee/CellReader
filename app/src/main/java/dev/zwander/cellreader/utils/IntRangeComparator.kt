package dev.zwander.cellreader.utils

object IntRangeComparator : Comparator<IntRange> {
    override fun compare(o1: IntRange, o2: IntRange): Int {
        return o1.first.compareTo(o2.first).run {
            if (this == 0) {
                o1.last.compareTo(o2.last)
            } else {
                this
            }
        }
    }
}

object IntRangePairComparator : Comparator<Pair<IntRange, IntRange>> {
    override fun compare(o1: Pair<IntRange, IntRange>, o2: Pair<IntRange, IntRange>): Int {
        val firstResult = IntRangeComparator.compare(o1.first, o2.first)

        return if (firstResult == 0) {
            IntRangeComparator.compare(o1.second, o2.second)
        } else {
            firstResult
        }
    }
}