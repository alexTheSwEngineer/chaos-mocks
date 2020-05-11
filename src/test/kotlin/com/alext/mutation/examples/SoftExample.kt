package com.alext.mutation.examples


import com.alext.muitation.allCombinations
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import com.alext.muitation.assertAllForMutations

class MutationExtensionsExampleSoft {

    //obviously wrong add method:
    fun add(vararg operands: Int): Int {
        return 6
    }

    private val operandsResultingIn6= listOf(1, 2, 3)
    private  var expectedResultOf6 = 6
    private val arbitraryMutation = 132

    private val mutations = listOf<(MutableList<Int>)->Unit>(
        { it[0] = it[0] + arbitraryMutation },
        { it[1] = it[1] + arbitraryMutation },
        { it[2] = it[2] + arbitraryMutation }
    )
    fun createMutatedInput(mutations: List<(MutableList<Int>)->Unit>) =
        operandsResultingIn6.toMutableList().also {
            mutations.forEach {mutate->mutate(it) }
        }


    @Test
    fun adding1_2_3resultsIn6() {
        val result = add(*operandsResultingIn6.toIntArray())
        assertThat(result).isEqualTo(expectedResultOf6)
    }

    @Test
    fun mutatingOperands_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        val softAssertions = SoftAssertions()
        mutations.assertAllForMutations(allCombinations(),this::createMutatedInput)
        {mutatedInput,mutationId->

            println("Testing info: ${info(mutationId, mutatedInput)}")

            val result = add(*mutatedInput.toIntArray())

            softAssertions.assertThat(result)
                          .`as`(info(mutationId, mutatedInput))
                          .isNotEqualTo(expectedResultOf6)

        }
        softAssertions.assertAll()
    }

    private fun info(mutationId: Set<Int>, mutatedInput: MutableList<Int>) =
            "Given mutation ${mutationId.joinToString(", ")}: ${mutatedInput.joinToString(",")}"

}
