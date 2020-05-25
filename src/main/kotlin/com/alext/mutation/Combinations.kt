package com.alext.mutation

import java.util.*

/**
 * Produces all non empty combinations of indexes for a given list.
 * @return Returns a function that will produce an iterator of all possible non empty combinations of indices for a given lsit
 * */
fun <T> allCombinations(): (List<T>) -> Iterator<Set<Int>> {
    return allCombinationsThatDoNotContain()
}

/**
 * Produces all non empty combinations of list indexes, of size less or equal to the param maxCombinationSize, and greater or equal to the parameter minCombinationSize
 * or by default 0.
 * @param maxCombinationSize the maximum (included) size of combination generated
 * @param minCombinationSize the minimum (included) size of combinations generated, by default 1.
 * @return Returns a function that will produce an iterator of all possible non emoty combinations of indices for a given list such
 * that combinations size is les or equal to maxCombinationSize and biger or equal to minCombinationSize.
 * */
fun <T> allCombinationsOfSize(maxCombinationSize: Int, minCombinationSize: Int = 0): (List<T>) -> Iterator<Set<Int>> {
    return { lst -> ListIndexCombinations(lst.size, maxCombinationSize, minCombinationSize).iterator() }
}

/**
 * Produces all non empty combinations of list indexes, that do not contain neither fo the doNotMutateIndex indexes
 * @param doNotMutateIndex All the indexes that will NOT show up in the combinations
 * @return Returns a function that will produce an iterator of all possible combinations of indices for a given list that
 * do not contain any of the doNotMutateIndexes
 * */
fun <T> allCombinationsThatDoNotContain(vararg doNotMutateIndex: Int): (List<T>) -> Iterator<Set<Int>> {
    val skipIndexSet = doNotMutateIndex.toSet()
    return { lst ->
        ListIndexCombinations(lst.size, indexPredicate = { index -> !skipIndexSet.contains(index) })
                .iterator()
    }
}

/**
 * Produces all combinations of size one of list indexes
 * @return Returns a function that will produce an iterator of all possible combinations of indices for a given list that
 * have size 1
 * */
fun <T> allSingleCombinations(): (List<T>) -> Iterator<Set<Int>> =
        { lst -> ListIndexCombinations(lst.size, maxCombinationSize = 1).iterator() }

/**
 * Produces all combinations of size one of list indexes, except the ones in doNotMutateIndex
 * @param doNotMutateIndex all indexes that will NOT show up as index combinations of size one.
 * @return Returns a function that will produce an iterator of all possible combinations of indices for a given list that
 * have size 1
 * */
fun <T> allSingleCombinationsExcept(vararg doNotMutateIndex: Int): (List<T>) -> Iterator<Set<Int>> {
    val doNotMutateSet = doNotMutateIndex.toHashSet()
    return { lst -> ListIndexCombinations(lst.size, maxCombinationSize = 1, indexPredicate = { !doNotMutateSet.contains(it) }).iterator() }
}

/**
 * Produces all combinations that do not contain the indexes in doNotMutateIndex param.
 * @param doNotMutateIndex all indexes that will NOT show up as index combinations of size one.
 * @return Returns a function that will produce an iterator of all possible non empty combinations of indices for a given list that
 * do not contain any doNotMutateIndex.
 * */
fun <T> allCombinationsExceptContaining(vararg doNotMutateIndex: Int): (List<T>) -> Iterator<Set<Int>> {
    val doNotMutateSet = doNotMutateIndex.toHashSet()
    return { lst -> ListIndexCombinations(lst.size, indexPredicate = { !doNotMutateSet.contains(it) }).iterator() }
}

/**
 * Produces all combinations except the skipCombinations
 * @param skipCombinations all combinations that will NOT show up
 * @return Returns a function that will produce an iterator of all possible non empty combinations of indices for a given list that
 * are not any of the combinations in skipCombinations param
 * */
fun <T> allCombinationsExcept(vararg skipCombinations:Set<Int>):(List<T>)-> Iterator<Set<Int>> {
    val skipCombinationsSet = skipCombinations.toHashSet()
    return { lst -> ListIndexCombinations(lst.size, combinationPredicate = { !skipCombinationsSet.contains(it) }).iterator() }
}


/**
 * Produces all combinations except the ones that satisfy the predicate sent as param.
 * @param predicate a predicate that returns true if a combination should NOT be generated
 * @return Returns a function that will produce an iterator of all possible non empty combinations of indices for a given list that
 * do not match the predicate given as parameter
 * */
fun <T> allCombinationsExcept(predicate:(Set<Int>)->Boolean):(List<T>)-> Iterator<Set<Int>> {
    return { lst -> ListIndexCombinations(lst.size, combinationPredicate = { !predicate(it) }).iterator() }
}

/**
* Produces all combinations of indexes where only one is missing for a given list
* @return Returns a function that will produce an iterator of all possible non empty combinations of indices for a given list that
* have size list.size-1
* */
fun <T> allCombinationsWithOneMissing(): (List<T>) -> Iterator<Set<Int>> =
        { lst -> ListIndexCombinations(lst.size, maxCombinationSize = lst.size - 1, minCombinationSize = lst.size - 1).iterator() }


/**
 * Data class for describing non empty combinations of list indexes (akka sequential integers starting form 0).
 * @property listSize The size of the list
 * @property maxCombinationSize The bigest combination size (inclusive)
 * @property minCombinationSize The smalles combination size (inclusive), 1 by default
 * @property indexPredicate A predicate to match chosen indexes. Only combination in which all indexes that match will be generated
 * @property combinationPredicate A predicate to match chosen combinations. Only combinations that match the combination predicate will be matched
 *
 * */
data class ListIndexCombinations(
        val listSize: Int,
        val maxCombinationSize: Int = listSize,
        val minCombinationSize: Int = 0,
        val indexPredicate: (Int) -> Boolean = { true },
        val combinationPredicate: (Set<Int>) -> Boolean = { true }
) {
    fun ofMaxSize(maxSize: Int) = this.copy(maxCombinationSize = maxSize)
    fun ofMinSize(minSize: Int) = this.copy(minCombinationSize = minSize)
    fun forIndexesThatAlso(newPredicate: (Int) -> Boolean) = this.copy(indexPredicate = { this.indexPredicate(it) && newPredicate(it) })
    fun forCombinationsThatAlso(newPredicate: (Set<Int>) -> Boolean) = this.copy(combinationPredicate = { this.combinationPredicate(it) && newPredicate(it) })
    fun iterator() = sequence().iterator()
    fun sequence() = indexCombinations(
            maxListIndex = listSize - 1,
            maxCombinationSize = maxCombinationSize,
            minCombinationSize = minCombinationSize,
            indexPredicate = indexPredicate,
            combinationPredicate = combinationPredicate
    )
}

private fun indexCombinations(maxListIndex: Int, maxCombinationSize: Int, minCombinationSize: Int, currentCombination: Stack<Int> = Stack(), indexPredicate: (Int) -> Boolean = { true }, combinationPredicate: (Set<Int>) -> Boolean = { true }): Sequence<Set<Int>> {
    return sequence {
        var currentListIndex = maxListIndex
        if (currentCombination.size >= maxCombinationSize) {
            return@sequence
        }
        while (currentListIndex >= 0) {
            currentCombination.push(currentListIndex)
            val shouldUseIndex = indexPredicate(currentListIndex)
            val combinationAsSet = currentCombination.toSet()
            if (minCombinationSize <= currentCombination.size && combinationPredicate(combinationAsSet) && shouldUseIndex) {
                yield(combinationAsSet)
            }

            if(shouldUseIndex){
                indexCombinations(currentListIndex - 1, maxCombinationSize, minCombinationSize, currentCombination, indexPredicate, combinationPredicate).forEach { subComb ->
                    yield(subComb)
                }
            }

            currentCombination.pop()
            currentListIndex -= 1
        }
    }
}

