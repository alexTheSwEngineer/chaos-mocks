package com.alext.sudoku

import java.io.PrintStream
import java.lang.Math.*
import java.lang.String.format

class GenericSudoku(
        val availablePropertyValues: Map<String, Set<String>>,
        val state: List<MutableMap<String, String>>,
        val rules: List<(List<Map<String, String>>) -> Boolean>,
        val solveCriteria: (List<Map<String, String>>) -> Boolean,
        val allValuesMustAppear: Boolean = true,
        val duplicatesAreAllowed: Boolean = false,
        val debugInterceptor: (GenericSudoku, String) -> Any = { _, _ -> },
        val maxDeterministicAtempts: Int = Int.MAX_VALUE,
        val maxBruteForceSearchDepth: Int = state.size * state.size + 2,
        val posibleMovesDebugger: (List<Map<String, String>>, List<MutableMap<String, Set<String>>>, Triple<Int, String, String>?) -> Any = { _, _, _ -> }
) {
    constructor(src: GenericSudoku) : this(
            src.availablePropertyValues,
            copy(src.state),
            src.rules,
            src.solveCriteria,
            src.allValuesMustAppear,
            src.duplicatesAreAllowed,
            src.debugInterceptor,
            src.maxDeterministicAtempts,
            src.maxBruteForceSearchDepth,
            src.posibleMovesDebugger
    )


    fun solve(currentDepth: Int = 0): GenericSudoku? {
        while (true) {
            val posibleMoves = calculatePossibleProperties()
            val certainMove = findCertainMove(posibleMoves)
            posibleMovesDebugger(state, posibleMoves, certainMove)
            if (certainMove == null) {
                break
            }
            state[certainMove.first][certainMove.second] = certainMove.third
            debugInterceptor(this, "After Deterministic Choice")
        }

        if (solveCriteria(state)) {
            return this
        }

        val posibleValues = calculatePossibleProperties()
        for (personIndex in 0 until state.size) {
            val valuesForPerson = posibleValues[personIndex]
            for ((property, posibleValuesForProperty) in valuesForPerson) {
                if (state[personIndex].containsKey(property)) {
                    continue
                }

                for (posibleValue in posibleValuesForProperty) {
                    val newSudoku = GenericSudoku(this)
                    newSudoku.state[personIndex][property] = posibleValue
                    debugInterceptor(newSudoku, "After brute-force choice")
                    val posibleSolution = newSudoku.solve(currentDepth + 1)
                    if (posibleSolution != null) {
                        return posibleSolution
                    }
                }
            }
        }

        return null
    }

    private fun findCertainMove(posibleValues: List<MutableMap<String, Set<String>>>): Triple<Int, String, String>? {
        var hits = findFiledsWithOnePossibleEntry(posibleValues)
        if (hits.isNotEmpty()) {
            return hits.first()
        }

        if (allValuesMustAppear) {
            hits = findValuesWithOnePossiblePosition(posibleValues)
            if (hits.isNotEmpty()) {
                return hits.first()
            }
        }

        return null
    }

    private fun findValuesWithOnePossiblePosition(posibleValues: List<MutableMap<String, Set<String>>>): List<Triple<Int, String, String>> {
        val res = mutableListOf<Triple<Int, String, String>>()
        availablePropertyValues.forEach { propertyName, propertyValues ->
            for (propertyValue in propertyValues) {
                var ocurances = 0
                val posiblePlace = mutableListOf<Triple<Int, String, String>>()
                for (personIndex in (0 until state.size)) {
                    val person = state[personIndex]
                    if (person[propertyName] == propertyValue) {
                        ocurances++
                    }

                    if (!person.containsKey(propertyName) && posibleValues[personIndex][propertyName]!!.contains(propertyValue)) {
                        posiblePlace.add(Triple(personIndex, propertyName, propertyValue))
                    }
                }
                if (ocurances == 0 && posiblePlace.size == 1) {
                    res.addAll(posiblePlace)
                }
            }
        }
        return res
    }

    private fun findFiledsWithOnePossibleEntry(posibleValues: List<MutableMap<String, Set<String>>>): List<Triple<Int, String, String>> {
        val res = mutableListOf<Triple<Int, String, String>>()
        for (personIndex in (0 until state.size)) {
            val person = state[personIndex]
            for ((property, values) in posibleValues[personIndex]) {
                if (values.size == 1 && !person.containsKey(property)) {
                    res.add(Triple(personIndex, property, values.single()))
                }
            }
        }
        return res
    }

    fun calculatePossibleProperties(): List<MutableMap<String, Set<String>>> {
        val res = mutableListOf<MutableMap<String, Set<String>>>()
        for (personIndex in 0 until state.size) {
            val person = state[personIndex]
            val remainingPropertyValues = mutableMapOf<String, Set<String>>()
            for ((property, values) in availablePropertyValues) {
                val availableValues = mutableSetOf<String>()
                remainingPropertyValues[property] = availableValues

                if (person.containsKey(property)) {
                    //move is played, and hence is the only possible move,
                    // no need for calculation, just move on to nex property
                    availableValues.add(person[property]!!)
                    continue
                } else {
                    availableValues.addAll(values)
                }

                if (!duplicatesAreAllowed) {
                    availableValues.removeIf { value -> isOtherPersonHavingThisValue(personIndex, property, value) }
                }

                availableValues.removeIf { value -> !isMoveValid(personIndex, person, property, value) }
            }
            res.add(remainingPropertyValues)
        }
        return res
    }

    private fun isMoveValid(personIndex: Int, person: MutableMap<String, String>, property: String, value: String): Boolean {
        val oldValue = person[property]
        person[property] = value
        var failingRuleIndex = -1
        for (ruleIndex in 0 until rules.size) {
            if (!rules[ruleIndex](state)) {
                failingRuleIndex = ruleIndex
                break
            }
        }
        if (oldValue == null) {
            person.remove(property)
        } else {
            person[property] = oldValue
        }
        if (failingRuleIndex >= 0) {
            println("property of $personIndex  $property $value breaks rule with index $failingRuleIndex ")
            return false
        }
        return true
    }

    private fun isOtherPersonHavingThisValue(personIndex: Int, property: String, value: String): Boolean {
        for (otherPersonIndex in 0 until state.size) {
            if (otherPersonIndex == personIndex) {
                continue
            }

            val otherPerson = state[otherPersonIndex]
            if (otherPerson[property] == value) {
                println("property of $personIndex  $property $value exists at other person $otherPersonIndex ")
                return true
            }
        }
        return false
    }

    companion object {
        public fun emptyBoard(size: Int): List<MutableMap<String, String>> {
            return (0 until size).map { mutableMapOf<String, String>() }
        }

        public fun printPosibleMoves(state: List<Map<String, String>>, moves: List<MutableMap<String, Set<String>>>,chosenMove: Triple<Int, String, String>?): Unit{
            println()
            moves.forEachIndexed{ index,personalMoves->
                print("$index. ")
                personalMoves.forEach{ k,v->
                    print("$k:  {${v.joinToString(", ")}} ")
                }
                println()
            }
        }

        private fun copy(other: List<MutableMap<String, String>>): List<MutableMap<String, String>> {
            val res = mutableListOf<MutableMap<String, String>>()
            other.forEach {
                val values = mutableMapOf<String, String>()
                res.add(values)
                it.forEach { key, value ->
                    values[key] = value
                }
            }
            return res
        }
    }
}


public fun GenericSudoku.printBoard(out: PrintStream = System.out, nullString: String = "null", delimiter: String = "   ", numeredRows: Boolean = true, headers: Boolean = true) {
    println()
    var maxWordLenght = this.state.flatMap {
        it.values.map { it.length }
    }.max() ?: 0

    maxWordLenght = max(maxWordLenght, this.availablePropertyValues.keys.map { it.length }.max() ?: 0)
    maxWordLenght = max(maxWordLenght, nullString.length)
    maxWordLenght += 1

    val numberSize = log10(this.state.size.toDouble()).let(::round) + 1
    if (headers) {
        if (numeredRows) {
            out.print(format("%1$-" + numberSize + "s", " "))
            out.print(delimiter)
        }
        this.availablePropertyValues.forEach { key, _ ->
            out.print(format("%1$-" + maxWordLenght + "s", key))
        }
        out.println()
    }


//            if (maxValue)

    this.state.forEachIndexed { index, row ->
        if (numeredRows) {
            out.print(format("%1$-" + numberSize + "s", "${index + 1}"))
            out.print(delimiter)
        }

        this.availablePropertyValues.keys.forEach { key ->
            out.print(format("%1$-" + maxWordLenght + "s", "${row[key] ?: nullString}"))
        }
        out.println()
    }
}
