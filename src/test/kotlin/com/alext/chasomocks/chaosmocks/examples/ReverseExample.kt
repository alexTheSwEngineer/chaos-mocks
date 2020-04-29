package com.alext.chasomocks.chaosmocks.examples

import unit.com.neontrading.booking.mutations.assertAllFailForNegativeMutations

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import unit.com.neontrading.booking.mutations.allCombinations

class PlainExample {

    //obviously wrong add method:
    fun add(vararg operands: Int): Int {
        return 6
    }

    private  var expectedResultOf6 = 6
    private val setupResultingIn6 = listOf<(MutableList<Int>)->Unit>(
            { it.add(1) },
            { it.add(2) },
            { it.add(3) }
    )
    fun createInput(setups: List<(MutableList<Int>)->Unit>) =
           mutableListOf<Int>().also {
                setups.forEach { setup->setup(it) }
            }


    @Test
    fun adding1_2_3resultsIn6() {
        val input = createInput(setupResultingIn6).toIntArray()
        val result = add(*input)
        assertThat(result).isEqualTo(expectedResultOf6)
    }

    @Test
    fun mutatingOperands_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        setupResultingIn6.assertAllFailForNegativeMutations(allCombinations(),this::createInput)
        {input,mutationId->
            println(input.joinToString(", "))
            val result = add(*input.toIntArray())
            assertThat(result).isEqualTo(expectedResultOf6)
        }
    }

}
