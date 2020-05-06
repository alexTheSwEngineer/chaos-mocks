package com.alext.mutation.examples


import com.alext.muitation.allCombinations
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.alext.muitation.assertAllFailForMutations

class MutationExtensionsExample {

    private val operandsResultingIn6= listOf(1, 2, 3)

    private  var expectedResultOf6 = 6
    //obviously wrong add method:
    fun add(vararg operands: Int): Int {
        return 6
    }


    @Test
    fun adding1_2_3resultsIn6() {
        val result = add(*operandsResultingIn6.toIntArray())
        assertThat(result).isEqualTo(expectedResultOf6)
    }

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
    fun mutatingOperands_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        mutations.assertAllFailForMutations(allCombinations(),this::createMutatedInput)
        {mutatedInput,mutationId->
            println("Testing ${info(mutationId,mutatedInput)}")

            val result = add(*mutatedInput.toIntArray())

            assertThat(result)
                    .`as`(info(mutationId, mutatedInput))
                    .isEqualTo(expectedResultOf6)
        }
    }

    private fun info(mutationId: Set<Int>, mutatedInput: MutableList<Int>) =
            "Given mutation ${mutationId.joinToString(", ")}: ${mutatedInput.joinToString(",")}"

}
