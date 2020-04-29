package com.alext.chasomocks.chaosmocks.examples


import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FalsePositiveExampleTest {

    //obviously wrong add method:
    fun add(vararg operands: Int): Int {
        return 6
    }

    private val operandsResultingIn6 = listOf(1, 2, 3)
    private val expectedResult6 = 6

    @Test
    fun adding1_2_3resultsIn6() {
        val result = add(*operandsResultingIn6.toIntArray())
        assertThat(result).isEqualTo(expectedResult6)
    }

}
