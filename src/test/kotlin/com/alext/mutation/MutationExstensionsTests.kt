package com.alext.mutation

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class MutationExstensionsTests {

    @Test
    fun given2mutations_assertIsCalled2Times() {
        val callHistory = mutableListOf<Pair<String, Set<Int>>>()
        val testCase = create(listOf(), callHistory)
        testCase.mutations.assertAllFailForMutations(testCase.combinator, testCase.inputCreator, testCase.assertion)
        assertThat(callHistory).isEqualTo(listOf(
                firstMutation.joinToString(", ") to firstMutation,
                secondMutation.joinToString(", ") to secondMutation

        ))
    }

    @Test(expected = AssertionError::class)
    fun givenSuccesfullAssert_assertAllFailForMutationsTHrowsAssertionError() {
        val callHistory = mutableListOf<Pair<String, Set<Int>>>()
        val testCase = create(listOf(), callHistory)
                .copy(assertion = { _, _ -> })
        testCase.mutations.assertAllFailForMutations(testCase.combinator, testCase.inputCreator, testCase.assertion)
    }

    @Test(expected = AssertionError::class)
    fun givenFailedAssert_assertAllForMutationsTHrowsAssertionError() {
        val callHistory = mutableListOf<Pair<String, Set<Int>>>()
        val testCase = create(listOf(), callHistory)
                .copy(assertion = { _, _ -> assertThat(true).isFalse() })
        testCase.mutations.assertAllForMutations(testCase.combinator, testCase.inputCreator, testCase.assertion)
    }

    @Test //A bit of dogfooding. This is a prime example of when not to use this library. The added complexity by far exceeds the benefits
    fun givenMutatedScenarioThatExpectsFailsFor2Mutations01and23_happyFlowAssertionsFail() {
        var callHistory = mutableListOf<Pair<String, Set<Int>>>()
        mutations.assertAllFailForMutations(allCombinationsExcept { it.contains(2) || it.contains(3) },
                { create(it, callHistory) })
        { mutated, mutation ->

            callHistory.clear()
            mutated.mutations.assertAllFailForMutations(mutated.combinator, mutated.inputCreator, mutated.assertion)
            assertThat(callHistory).isEqualTo(listOf(
                    firstMutation.joinToString(", ") to firstMutation,
                    secondMutation.joinToString(", ") to secondMutation

            ))
        }

    }

    val firstMutation = setOf(0, 1)
    val secondMutation = setOf(2, 3)
    val mutationThatExpectsFailsFor2Mutations01and23 = MutationExstensionsTestCase(
            mutations = listOf(0, 1, 2, 3, 4),
            combinator = { _ -> listOf(firstMutation, secondMutation).iterator() },
            inputCreator = { mutation -> mutation.joinToString(", ") },
            assertionShouldFail = false,
            assertion = { _, _ -> assertThat(false).isTrue() }
    )
    val mutations: List<(MutationExstensionsTestCase) -> MutationExstensionsTestCase> = listOf<(MutationExstensionsTestCase) -> MutationExstensionsTestCase>(
            { it.copy(combinator = { _ -> listOf(firstMutation).iterator() }) },
            { it.copy(inputCreator = { mutation -> mutation.joinToString("X") }) },
            { it.copy(assertionShouldFail = true) },
            { it.copy(assertion = { _, _ -> assertThat(false).isFalse() }) }

    )

    fun create(mutations: List<(MutationExstensionsTestCase) -> MutationExstensionsTestCase>, callHistory: MutableList<Pair<String, Set<Int>>>): MutationExstensionsTestCase {
        var acc = mutationThatExpectsFailsFor2Mutations01and23
        mutations.forEach {
            acc = it(acc)
        }
        return acc.copy(assertion = { result, mutation ->
            callHistory.add(Pair(result, mutation))
            acc.assertion(result, mutation)
        })
    }
}

data class MutationExstensionsTestCase(
        val mutations: List<Int>,
        val combinator: (List<Int>) -> Iterator<Set<Int>>,
        val inputCreator: (List<Int>) -> String,
        val assertionShouldFail: Boolean,
        val assertion: (String, Set<Int>) -> Unit
)
