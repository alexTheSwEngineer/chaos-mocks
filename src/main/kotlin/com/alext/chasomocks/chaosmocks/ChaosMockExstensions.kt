package unit.com.neontrading.booking.mutations


import java.util.*
inline fun <reified SetupType : Any, reified MockType> List<SetupType>.assertAllFailForNegativeMutations(mutator: (List<SetupType>) -> Iterator<Set<Int>>, mockFactory: (List<SetupType>) -> MockType, assertion: (MockType, Set<Int>) -> Unit) {
    this.assertForMutations(mutator, mockFactory, true,true, assertion)
}

inline fun <reified SetupType : Any, reified InputType> List<SetupType>.assertAllFailForMutations(mutator: (List<SetupType>) -> Iterator<Set<Int>>, mockFactory: (List<SetupType>) -> InputType, assertion: (InputType, Set<Int>) -> Unit) {
    this.assertForMutations(mutator, mockFactory, true,false, assertion)
}

inline fun <reified SetupType : Any, reified MockType> List<SetupType>.assertAllForNegativeMutations(mutator: (List<SetupType>) -> Iterator<Set<Int>>, mockFactory: (List<SetupType>) -> MockType, assertion: (MockType, Set<Int>) -> Unit) {
    this.assertForMutations(mutator, mockFactory, false,true, assertion)
}

inline fun <reified SetupType : Any, reified MockType> List<SetupType>.assertAllForMutations(mutator: (List<SetupType>) -> Iterator<Set<Int>>, mockFactory: (List<SetupType>) -> MockType, assertion: (MockType, Set<Int>) -> Unit) {
    this.assertForMutations(mutator, mockFactory, false,false, assertion)
}
/**
 * Applies mutations to a list of setup commands and then, for each mutation it creates a system under test by invoking the factory in order to execute the assertion.
 * A mutation is a set of indexes. When invoking the systemUnderTestFactory, settings with indexes in the mutation set will be ommited from the list with which (the systemUnderTestFactory) it is invoked
 * @param this Any list of settings that can be used to create a system ununder test
 * @param mutator A generator of mutation index sets. Each mutation consists of indexes that are going to be omited from the setup list.
 * @param assertion Is going to be called for each mutation with the mutation, and systemUnderTest as arguments.
 * @param assertionsShouldFail A flag indicating if the assertions are expected to pass, or fail. If the same assertions are used in non mutated tests, it is expected for them (the assertions) to fail
 * in mutation tests. If there are mutation specific assertions that are not expected to raise an error, this flag should be set to false.
 * */
inline fun <reified SetupType : Any, reified MockType> List<SetupType>.assertForMutations(mutator: (List<SetupType>) -> Iterator<Set<Int>>, systemUnderTestFactory: (List<SetupType>) -> MockType, assertionsShouldFail: Boolean = false, skipSetups: Boolean = false, assertion: (MockType, Set<Int>) -> Unit) {
    val mutationIterator = mutator(this)
    while (mutationIterator.hasNext()) {
        val mutation = mutationIterator.next()
        val mutatedMock = this.filterIndexed { index, _ ->
            skipSetups.xor(mutation.contains(index))
        }.let { systemUnderTestFactory(it) }

        try {
            assertion(mutatedMock, mutation)
        } catch (e: AssertionError) {
            if (assertionsShouldFail) {
                //If called with the usual assertions
                continue
            } else {
                //If called with mutation specific reversed assertions
                throw e
            }
        }

        if (assertionsShouldFail) {
            //If called with non mutation specific assertions, reaching this line means some assertion passed
            throw AssertionError("Mutation skip{${mutation.joinToString(",")}} did not raise assertionError")
        }
    }
}


fun <T> allSingleMutations(): (List<T>) -> Iterator<Set<Int>> {
    return allSingleMutationsExcept()
}

fun <T> allSingleMutationsExcept(vararg doNotMutateIndex: Int): (List<T>) -> Iterator<Set<Int>> {
    val doNotMutateIndexSet = doNotMutateIndex.toSet()
    return { lst ->
        sequence {
            (0 until lst.size).filter {
                !doNotMutateIndexSet.contains(it)
            }.forEach {
                yield(setOf(it))
            }
        }.iterator()
    }
}

fun <T> allCombinations(): (List<T>) -> Iterator<Set<Int>> {
    return allCombinationsThatDoNotContain()
}

fun <T> allCombinationsOfSize(maxCombinationSize:Int,minCombinationSize: Int = 0): (List<T>) -> Iterator<Set<Int>> {
    return  { lst -> indexCombinations(lst.size - 1,maxCombinationSize,minCombinationSize).iterator() }
}

fun <T> allCombinationsThatDoNotContain(vararg doNotMutateIndex: Int): (List<T>) -> Iterator<Set<Int>> {
    val skipIndexSet = doNotMutateIndex.toSet()
    return { lst ->
        val combinationsAcc = indexCombinations(lst.size - 1,lst.size,0) { index ->
            !skipIndexSet.contains(index)
        }
        combinationsAcc.iterator()
    }
}

private fun indexCombinations(listSize: Int, maxCombinationSize: Int,minCombinationSize: Int, currentCombination: Stack<Int> = Stack(), predicate: (Int) -> Boolean = { true }): Sequence<Set<Int>> {
    return sequence {
        var index = listSize
        if(currentCombination.size >= maxCombinationSize){
            return@sequence
        }
        while (index >= 0) {
            val shouldUseThisIndex = predicate(index)
            if (shouldUseThisIndex) {
                currentCombination.push(index)
                if(minCombinationSize<=currentCombination.size){
                    yield(currentCombination.toSet())
                }
            }
            indexCombinations(index - 1, maxCombinationSize,minCombinationSize, currentCombination, predicate).forEach { subComb ->
                yield(subComb)
            }
            if (shouldUseThisIndex) {
                currentCombination.pop()
            }
            index -= 1
        }
    }
}

fun main(){
//    val set
    allCombinationsOfSize<Int>(6,2).invoke(listOf(1,2,3,4,5,6)).forEachRemaining{
        println(it.joinToString(", "))
    }
}