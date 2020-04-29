package com.alext.chasomocks.chaosmocks.examples


import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ManuallExampleThatDetectsFalsePositives {

    //obviously wrong add method:
    fun add(vararg operands: Int): Int {
        return 6
    }

    private val operandsResultingIn6 = listOf(1, 2, 3)
    private val expectedResult6 = 6
    private val arbitraryMutation = 123

    @Test
    fun adding1_2_3resultsIn6() {
        val result = add(*operandsResultingIn6.toIntArray())
        assertThat(result).isEqualTo(expectedResult6)
    }

    @Test
    fun mutatingOperand0_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        val mutatedList = operandsResultingIn6.toMutableList()
        mutatedList[0] = mutatedList[0] + arbitraryMutation

        val result = add(*mutatedList.toIntArray())
        assertThat(result).isNotEqualTo(expectedResult6)
    }

    @Test
    fun mutatingOperand1_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        val mutatedList = operandsResultingIn6.toMutableList()
        mutatedList[1] = mutatedList[1] + arbitraryMutation

        val result = add(*mutatedList.toIntArray())
        assertThat(result).isNotEqualTo(expectedResult6)
    }

    @Test
    fun mutatingOperand2_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        val mutatedList = operandsResultingIn6.toMutableList()
        mutatedList[2] = mutatedList[2] + arbitraryMutation

        val result = add(*mutatedList.toIntArray())
        assertThat(result).isNotEqualTo(expectedResult6)
    }

    @Test
    fun mutatingOperands0and1_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        val mutatedList = operandsResultingIn6.toMutableList()
        mutatedList[0] = mutatedList[0] + arbitraryMutation
        mutatedList[1] = mutatedList[1] + arbitraryMutation

        val result = add(*mutatedList.toIntArray())
        assertThat(result).isNotEqualTo(expectedResult6)
    }

    @Test
    fun mutatingOperand0and2_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        val mutatedList = operandsResultingIn6.toMutableList()
        mutatedList[0] = mutatedList[0] + arbitraryMutation
        mutatedList[2] = mutatedList[2] + arbitraryMutation

        val result = add(*mutatedList.toIntArray())
        assertThat(result).isNotEqualTo(expectedResult6)
    }

    @Test
    fun mutatingOperand1and2_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        val mutatedList = operandsResultingIn6.toMutableList()
        mutatedList[1] = mutatedList[1] + arbitraryMutation
        mutatedList[2] = mutatedList[2] + arbitraryMutation

        val result = add(*mutatedList.toIntArray())
        assertThat(result).isNotEqualTo(expectedResult6)
    }

}
