package com.alext.chasomocks.chaosmocks

import com.nhaarman.mockito_kotlin.KStubbing
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.SoftAssertions
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import java.util.*


/**
 * Creates a mock and applies all of the stubbings to it.
 * @return A mock with all stubbings applied to it
 * */
inline fun <reified T : Any> List<(KStubbing<T>) -> Unit>.create(defaultAnswer: Answer<Any> = Mockito.RETURNS_SMART_NULLS): T{
    return applyMockCommands(this,defaultAnswer)
}

/**
 * Executes the assertion for each possible mock created by skiping just one stubbing from the list.
 * For a list of stubbings with size 3 it will execute the assertions for the following kmocks:
 * MOCK  with stubbings 1 & 2 chaosIndex = 0
 * MOCK with stubbings 0 & 2  chaosIndex = 1
 * MOCK with stubbings 1 & 2  chaosIndex = 2
 * @param defaultAnswer the default answer for non mocked fields
 * @param assertion the assertion lambda with parameter: softassertion is a soft assertion initialized before every call of forEachChaotickMock, the mock itself, and the index of the current chaotic execution (for debbuging purposes)
 * */
inline fun <reified T : Any> List<(KStubbing<T>) -> Unit>.forEachChaoticMock(defaultAnswer: Answer<Any> = Mockito.RETURNS_SMART_NULLS,assertion:(SoftAssertions, T, Int)->Unit){
    this.forEachChaoticMock((0 until this.size).toList(),defaultAnswer,assertion)
}


/**
 * Executes the assertion for each mock created by skiping just one stubbing from this list which index is in chaosIndexes.
 * For a list of stubbings with size 3 and chaosIndexes list [1,2] it will execute the assertions for the following mocks
 * MOCK  with stubbings 0 & 2  chaosIndex=1
 * MOCK with stubbings  0 & 1   chaosIndex=2
 * @param chaosIndexes the list with indexes to be skipped from the stubing list in order for "chaotic" incomplete mock to be chreated
 * @param defaultAnswer the default answer for non mocked fields
 * @param assertion the assertion lambda with parameter: softassertion is a soft assertion initialized before every call of forEachChaotickMock, the mock itself, and the index of the current chaotic execution (for debbuging purposes)
 * */
inline fun <reified T : Any> List<(KStubbing<T>) -> Unit>.forEachChaoticMock(chaosIndexes:List<Int>,defaultAnswer: Answer<Any> = Mockito.RETURNS_SMART_NULLS, assertion:(SoftAssertions, T, Int)->Unit){
    val nonNUllSoftAssertions = SoftAssertions()
    chaosIndexes.forEach {chaosIndex->
        this.filterIndexed{ index,_->
            index != chaosIndex
        }.create(defaultAnswer)
                .let {
                    assertion(nonNUllSoftAssertions,it,chaosIndex)
                }
    }
    nonNUllSoftAssertions.assertAll()
}

/**
 * Creates a mock based on all the stubbings in this list
 * @param defaultAnswer the default answer for the unmocked methods and fields of the newly created mock
 * @return A mkock with all the stubbings of this list applied to it.
 * */
inline fun <reified T : Any> applyMockCommands(lst:List<(KStubbing<T>)->Unit>,defaultAnswer: Answer<Any> = Mockito.RETURNS_SMART_NULLS):T{
    return mock<T> (name = UUID.randomUUID().toString(),defaultAnswer = defaultAnswer) {
        lst.forEach { it(this) }
    }
}
