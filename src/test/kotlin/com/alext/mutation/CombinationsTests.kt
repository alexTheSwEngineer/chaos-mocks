package com.alext.mutation

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test

class CombinatoricTests {

    @Test
    fun combinationsOf4isCorrect() {
        val res = ListIndexCombinations(4).sequence().toSet()
        val softAssertions = SoftAssertions()
        softAssertions.assertThat(res.size).isEqualTo(15)
        softAssertions.assertThat(res).isEqualTo(CombinationsOf4().all)
    }

    @Test
    fun combinationsOf4Between2and3NotContaining3HavingevenSumAreCorrect() {
        val res = createListIndexCombination(indexCombinationsOfSizeBetween2and3NotContaining3).sequence().toSet()
        assertThat(res).isEqualTo(allCombinationsOf4Between2and3NotContaining3HavingevenSum)
    }


    @Test
    fun mutatedCombinationsOf4Between2and3NotContaining3HavingevenSumAreNotCorrect() {
        indexCombinationsOfSizeBetween2and3NotContaining3.assertAllFailForMutations(allCombinationsOfSize(3), this::createListIndexCombination)
        { mutated, mutation ->
            val res = mutated.sequence().toSet()
            assertThat(res).isEqualTo(allCombinationsOf4Between2and3NotContaining3HavingevenSum)
        }
    }

    @Test
    fun allSingleMutations_returnCorrect() {
        val res = mutableSetOf<Set<Int>>()
        allSingleCombinations<Boolean>()(listOfsize(4))
                .forEach { res.add(it) }

        assertThat(res).isEqualTo(setOf(
                setOf(0),
                setOf(1),
                setOf(2),
                setOf(3)
        ))
    }

    @Test
    fun allSingleCombinationsExceptSingleIndexesTest() {
        val res = mutableSetOf<Set<Int>>()
        allSingleCombinationsExcept<Boolean>(3, 4)(listOfsize(6))
                .forEachRemaining { res.add(it) }

        assertThat(res).isEqualTo(setOf(
                setOf(0),
                setOf(1),
                setOf(2),
                setOf(5)
        ))
    }

    @Test
    fun allCombinationsExceptSingleIndexesTest() {
        val res = mutableSetOf<Set<Int>>()
        allCombinationsExceptContaining<Boolean>(3, 4)(listOfsize(4))
                .forEachRemaining { res.add(it) }

        assertThat(res).isEqualTo(CombinationsOf4().all.filter { !it.contains(3) && !it.contains(4) }.toSet())
    }

    @Test
    fun allCombinationsExceptExact() {
        val res = mutableSetOf<Set<Int>>()
        allCombinationsExcept<Boolean>(setOf(3, 4))(listOfsize(4))
                .forEachRemaining { res.add(it) }

        assertThat(res).isEqualTo(CombinationsOf4().all.filter { it != setOf(3, 4) }.toSet())
    }

    @Test
    fun allCombinationsExceptPredicate() {
        val res = mutableSetOf<Set<Int>>()
        allCombinationsExcept<Boolean>({ it == setOf(3, 4) })(listOfsize(4))
                .forEachRemaining { res.add(it) }

        assertThat(res).isEqualTo(CombinationsOf4().all.filter { it != setOf(3, 4) }.toSet())
    }


    @Test
    fun allSingleCombinationsExceptGivenCombinationsIndexesTest() {
        val res = mutableSetOf<Set<Int>>()
        allSingleCombinationsExcept<Boolean>(3, 4)(listOfsize(6))
                .forEachRemaining { res.add(it) }

        assertThat(res).isEqualTo(setOf(
                setOf(0),
                setOf(1),
                setOf(2),
                setOf(5)
        ))
    }

    @Test
    fun allCombinationsWithOneMissingTest() {
        val res = mutableSetOf<Set<Int>>()
        allCombinationsWithOneMissing<Boolean>()(listOfsize(5)).forEachRemaining {
            res.add(it)
        }
        assertThat(res).isEqualTo(setOf(
                setOf(4, 3, 2, 1),
                setOf(4, 3, 2, 0),
                setOf(4, 3, 1, 0),
                setOf(4, 2, 1, 0),
                setOf(3, 2, 1, 0)
        ))
    }


    val allCombinationsOf4Between2and3NotContaining3HavingevenSum = setOf(
            setOf(0, 1),
            setOf(2, 1)
    )

    val indexCombinationsOfSizeBetween2and3NotContaining3 = listOf<(ListIndexCombinations) -> ListIndexCombinations>(
            { it.ofMaxSize(2) },
            { it.ofMinSize(2) },
            { it.forIndexesThatAlso { index -> index != 3 } },
            { it.forCombinationsThatAlso { combination -> combination.sum() % 2 != 0 } }
    )

    fun createListIndexCombination(lst: List<(ListIndexCombinations) -> ListIndexCombinations>): ListIndexCombinations {
        var acc = ListIndexCombinations(4)
        lst.forEach { acc = it(acc) }
        return acc
    }

    fun listOfsize(n: Int): MutableList<Boolean> {
        return mutableListOf<Boolean>().also { lst ->
            (0 until n).forEach { lst.add(false) }
        }
    }
}

class CombinationsOf4 {
    val combinationsOf4WithLength1 = setOf(
            setOf(0),
            setOf(1),
            setOf(2),
            setOf(3)
    )

    val combinationsOf4WithLength2 = setOf(
            setOf(0, 1),
            setOf(0, 2),
            setOf(0, 3),
            setOf(1, 2),
            setOf(1, 3),
            setOf(2, 3)
    )

    val combinationsOf4WithLength3 = setOf(
            setOf(0, 1, 2),
            setOf(0, 1, 3),
            setOf(0, 2, 3),
            setOf(1, 2, 3)
    )

    val combinationsOf4WithLength4 = setOf(
            setOf(0, 1, 2, 3)
    )

    val all = setOf(
            *combinationsOf4WithLength1.toList().toTypedArray(),
            *combinationsOf4WithLength2.toList().toTypedArray(),
            *combinationsOf4WithLength3.toList().toTypedArray(),
            *combinationsOf4WithLength4.toList().toTypedArray()
    )
}
