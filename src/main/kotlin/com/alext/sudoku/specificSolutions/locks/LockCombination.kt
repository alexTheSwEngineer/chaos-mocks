package com.alext.sudoku.specificSolutions.locks

import com.alext.sudoku.GenericSudoku
import com.alext.sudoku.printBoard


val propertyName = "lockDigit"
fun main() {
    val lock = GenericSudoku(availablePropertyValues = mapOf(
            propertyName to setOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    ),
            state = GenericSudoku.emptyBoard(3),
            solveCriteria = {
                it.all { lockDigit -> lockDigit.size == 1 }
            },
            rules = listOf(
                    { state -> AssertList("9", "1", "5").hasJustCorrectValue(1).hasCorrectPlaceAndValue(1).rule(state) },
                    { state -> AssertList("9", "4", "7").hasJustCorrectValue(1).hasCorrectPlaceAndValue(0).rule(state) },
                    { state -> AssertList("5", "3", "9").hasJustCorrectValue(2).hasCorrectPlaceAndValue(0).rule(state) },
                    { state -> AssertList("0", "6", "1").hasJustCorrectValue(0).hasCorrectPlaceAndValue(0).rule(state) },
                    { state -> AssertList("6", "1", "3").hasJustCorrectValue(1).hasCorrectPlaceAndValue(0).rule(state) }
            ),
            allValuesMustAppear = false
    )
    lock.solve()!!.printBoard(headers = false, numeredRows = false)
}

class AssertList(val ruleList: List<String>,
                 val expectedJustCorrectValue: Int? = null,
                 val expectedCorrectValueAndPlace: Int? = null
) {
    constructor(vararg list: String) : this(listOf(*list))

    fun hasCorrectPlaceAndValue(correctPlaceAndValue: Int) = AssertList(ruleList, this.expectedJustCorrectValue, correctPlaceAndValue)
    fun hasJustCorrectValue(correctPlace: Int) = AssertList(ruleList, correctPlace, this.expectedCorrectValueAndPlace)
    public fun rule(state: List<Map<String, String>>): Boolean {
        if (!state.all { it.size == 1 }) {
            return true
        }
        if (expectedJustCorrectValue != null) {
            val justCorrectValue = state.filterIndexed { index, elem -> elem.isNotEmpty() && ruleList.contains(elem[propertyName]) }.count()
            if (justCorrectValue != expectedJustCorrectValue) {
                return false
            }
        }

        if (expectedCorrectValueAndPlace != null) {
            val correctCount = state.filterIndexed { index, elem -> index >= ruleList.size || (elem.isNotEmpty() && ruleList[index] == elem[propertyName]) }.count()
            if (correctCount != expectedCorrectValueAndPlace) {
                return false
            }
        }

        return true
    }
}

