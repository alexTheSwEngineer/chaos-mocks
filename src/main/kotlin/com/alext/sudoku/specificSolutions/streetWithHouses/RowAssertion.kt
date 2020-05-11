package com.alext.sudoku.specificSolutions.streetWithHouses


open class RowAssertion(
        val candidates: List<(List<Map<String, String>>, Map<String, String>) -> Boolean> = mutableListOf(),
        val predicates: List<(List<Map<String, String>>, Map<String, String>) -> Boolean> = mutableListOf(),
        val isSinglePersonExpected: Boolean = false
) {


    fun that(key: String, value: String): RowAssertion {
        return return RowAssertion(
                this.candidates + listOf{ _, person ->  person.containsKey(key) },
                this.predicates + listOf{ _, person ->  person[key] == value },
                this.isSinglePersonExpected
        )
    }

    fun that(key: String): RowAssertion {
        return return RowAssertion(
                this.candidates + listOf{ _, person -> person.containsKey(key) },
                this.predicates,
                this.isSinglePersonExpected
        )
    }


    fun that(pair: Pair<String, String>): RowAssertion {
        return that(pair.first, pair.second)
    }


    fun     with(pair: Pair<String, String>): RowAssertion {
        return that(pair.first, pair.second)
    }

    fun with(key: String): RowAssertion {
        return that(key)
    }

    fun are(other: RowAssertion): RowAssertion {
        return RowAssertion(
                this.candidates + other.predicates + other.candidates,
                this.predicates + other.predicates,
                false
        )
    }

    fun are(key: String, value: String): RowAssertion {
        return RowAssertion(
                this.candidates +this.predicates + listOf{ _,person->person.containsKey(key)} ,
                this.predicates+ listOf{ _,person->person[key] == value},
                false
        )
    }

    fun are(pair: Pair<String, String>): RowAssertion {
        return are(pair.first,pair.second)
    }

    fun `is`(other: RowAssertion): RowAssertion {
        return RowAssertion(
                this.candidates + this.predicates+other.candidates,
                this.predicates + other.predicates,
                true
        )
    }

    fun `is`(key: String, value: String): RowAssertion {
        return RowAssertion(
                this.candidates + this.predicates + listOf{_,state -> state.containsKey(key)},
                this.predicates + listOf{_,state -> state[key] == value},
                true
        )
    }

    fun `is`(pair: Pair<String, String>): RowAssertion {
        return `is`(pair.first,pair.second)
    }


    fun toRule(): (List<Map<String, String>>) -> Boolean {
        return { state ->
            val peopleThatMustMatch = state.filter { person -> candidates.all { it(state, person) } }
            if (peopleThatMustMatch.isEmpty()) {
                true
            } else {
                val peopleThatMustMatchIndeedMatch = peopleThatMustMatch.all { person -> predicates.all { it(state, person) } }
                if (isSinglePersonExpected) {
                    peopleThatMustMatchIndeedMatch && peopleThatMustMatch.size == 1
                } else {
                    peopleThatMustMatchIndeedMatch
                }
            }
        }
    }
}