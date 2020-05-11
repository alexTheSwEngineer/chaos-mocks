# Mutation testing extensions

This is a very tiny framework that reduces the number of false positive unit tests. It will run a combination of provided mutations on a test and expect it to fail.

* What is the problem being solved (why bother with this?????)
* How to use this
* Minimal example
* Advanced setup FAQ and tricks
* When to use it or not

# What is the problem being solved:

Consider an obviously false positive test:

```kotlin
    
    //obviously wrong add method:
    fun add(vararg operands: Int): Int {
        return 6
    }


    val operandsResultingIn6= listOf(1, 2, 3)
    val expectedResultOf6 = 6
    @Test
    fun adding1_2_3resultsIn6() {
        val result = add(*operandsResultingIn6.toIntArray())
        assertThat(result).isEqualTo(expectedResultOf6)
    }
```

To make sure that the test is not a false positive, we need to add mutated scenarios, and check that they fail. 

Not only ```1+2+3 equals 6```, but also ```SomethingThatIsNot1 + 2 + 3 doesn't equal 6```.
 
Not only ```1+2+3 equals 6```, but also ```1 + SomethingThatIsNot2 + 3 doesn't equal 6```.
 
Not only ```1+2+3 equals 6```, but also ```1 + 2 + SomethingThatIsNot3 doesn't equal 6```.

Or in code: 
```kotlin
     
     //obviously wrong add method:
     fun add(vararg operands: Int): Int {
         return 6
     }
 
 
     val operandsResultingIn6= listOf(1, 2, 3)
     val expectedResultOf6 = 6
     @Test
     fun adding1_2_3resultsIn6() {
         val result = add(*operandsResultingIn6.toIntArray())
         assertThat(result).isEqualTo(expectedResultOf6)
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

 ```

This project automates the process above.

# How to use this:

Recipe for using this library in 3 simple steps:
1. You will need a test that is passing, and that will be tested for false positives:
    ```kotlin
    
        //obviously wrong add method:
        fun add(vararg operands: Int): Int {
            return 6
        }
    
        val operandsResultingIn6 = listOf(1, 2, 3)
        val expectedResultOf6 = 6
    
        @Test
        fun adding1_2_3resultsIn6() {
            val result = add(*operandsResultingIn6.toIntArray())
            assertThat(result).isEqualTo(expectedResultOf6)
        }
    ```

2. You will need a lsit of mutations any of which will make the test fail. The mutations can be of any type you choose, in this example they are of type ```(MutableList<Int>) -> Unit```:
    ```kotlin
        val arbitraryMutation = 132
        val mutations = listOf<(MutableList<Int>) -> Unit>(
                { it[0] = it[0] + arbitraryMutation },
                { it[1] = it[1] + arbitraryMutation },
                { it[2] = it[2] + arbitraryMutation }
        )
    ```
    In this example, if we apply any of the mutations to a mutable copy of the original list ```operandsResultingIn6``` we will get
    a new mutated list, (a mutated input to our test, if you will) that should not equal to 6 when passed to the function add() (had that function been correct). 
 
3. You will need a method that knows how to take a random list of mutations of the type of your choosing (in this example ```(MutableList<Int>) -> Unit```):
    ```kotlin
    
        fun createMutatedInput(mutations: List<(MutableList<Int>) -> Unit>):List<Int>{
                return operandsResultingIn6.toMutableList().also {
                    mutations.forEach { mutate -> mutate(it) }
                }
        }   
    ```

### Voila! Done!. All that is left is to use the ingredients (shown bellow).
The code below will:
* Create a ```List<Int>``` input for our test, with the help of the createMutatedInput function we defined above.
* Run the test and __expect it to fail__.
* Run the steps above not only for the mutations list that we defined, but for __every possible non empty combination__ of it.

That's it. Now, if our add function is obviously wrong (which it is), even if we have a passing test due to a false positive, the mutated version of it will catch that.
This mutation test below, will pass if and only if every mutation combination results in an assertion __fail__.  
```kotlin
    @Test
    fun mutatingOperands_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        mutations.assertAllFailForMutations(allCombinations(), this::createMutatedInput)
        { mutatedInput, mutationId ->
            val result = add(*mutatedInput.toIntArray())
            assertThat(result).isEqualTo(expectedResultOf6)
        }
    }
```

# Advanced setup FAQ and tricks:
* ## I want all the mutation scenarios to pass instead of the default state of things in which the framework expects all of them to fail. (Because of more complex custom assertions, of course)
__Ask and ye shall receive:__ 
use the ```assertAllForMutations``` instead of ```assertAllFailForMutations```.
 
Note how in this scenario, we assert that addition of the mutated input is NOT equal to the expected result.
The mutation test below will pass if and only if all assertions are __successful__. 

```kotlin
    @Test
    fun mutatingOperands_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        mutations.assertAllForMutations(allCombinations(), this::createMutatedInput)
        { mutatedInput, mutationId ->
            val result = add(*mutatedInput.toIntArray())
            assertThat(result).isEqualTo(expectedResultOf6)
        }
    }
```

* ## I don't want __all__ combinations of the mutations
__Ask not and ye shall receive not. ```allCombinations()``` is not mandatory, you can use any of the following functions:__

* ```allCombinations()```
* ```allCombinationsOfSize(maxCombinationSize: Int, minCombinationSize: Int = 1)```
* ```allCombinationsThatDoNotContain(vararg doNotMutateIndex: Int)```
* ```allSingleCombinations() same as allCombinationsOfSize(maxCombinationSize = 1)```
* ```allSingleCombinationsExcept(vararg doNotMutateIndex: Int)```
* ```allCombinationsExceptContaining(vararg doNotMutateIndex: Int)```
* ```allCombinationsExcept(vararg skipCombinations:Set<Int>)```
* ```allCombinationsExcept(predicate:(Set<Int>)->Boolean)```
* ```allCombinationsWithOneMissing()```

* ## I don't want any of the above combinations
__Ask and ye shall type it yourself:__

If the above functions are not sufficient, you need to implement the following interface ```(List<T>) -> Iterator<Set<Int>>```, where ```(List<T>)``` is the type of the mutations. 

The result needs to be iterator of sets of indexes that should be used. Each set should define a combination of indexes we want to select from the mutation list.
The example below will only test the combinations (0,1,2),(0,1)(1,2):
```kotlin
    @Test
    fun mutatingOperands_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        mutations.assertAllForMutations({_->listOf(setOf(0,1,2),setOf(0,1),setOf(1,2))}, this::createMutatedInput)
        { mutatedInput, mutationId ->
            val result = add(*mutatedInput.toIntArray())
            assertThat(result).isEqualTo(expectedResultOf6)
        }
    }
    
```

* ## I want better error messages. :(
__Ask and thine assertion framework will provide.__
Getting the mutations to all run for the first time can be... not so pleasant of an experience. Using the parameters provided is strongly suggested.
The parameter mutationId is a ```Set<Int>``` of all mutation list indexes that are being used for the current mutation.
Common ways to use this in assertJ is:
```kotlin
    @Test
    fun mutatingOperands_ProducesDiferentResultNotEqualToExpectedResultInNormalScenario() {
        mutations.assertAllForMutations({_->listOf(setOf(0,1,2),setOf(0,1),setOf(1,2))}, this::createMutatedInput)
        { mutatedInput, mutationId ->
            val mutationIndexesUsed = mutationId.joinToString(", ")
            val valuesUsed = mutatedInput.joinToString(", ")
            val result = add(*mutatedInput.toIntArray())
            assertThat(result)
            .`as`("Mutation indexes $mutationIndexesUsed with values $valuesUsed")
            .isEqualTo(expectedResultOf6)
        }
    }
    
```

* ## I am already using mockito, are there any shortcuts?
__No.__ There are some hacks though. You can preserve readability by transforming the construct mock<T>{} into List<KStubbing<T>> example:

This mock setup:
```kotlin
    fun postingLineSetup(senderIban: String, recipientIban: String, amount: BigDecimal, postingside: Postingside, paymentBookingTypePg: PaymentBookingTypePg, date: LocalDate): FullPostingLineExtendedView {
           return mock<FullPostingLineExtendedView>(defaultAnswer = RETURNS_DEEP_STUBS) {
               on { postingline_subledger_account_id() } doReturn specialId
               on { posting_payment_sender_iban() } doReturn senderIban
               on { posting_payment_recipient_iban() } doReturn recipientIban
               on { posting_payment_booking_type() } doReturn paymentBookingTypePg
               on { postingline_side() } doReturn postingside
               on { postingline_amount() } doReturn amount
           }
       }
    val mock = postingLineSetup(....)
```
Can be transformed into this with the help of the [create]() exstension:
```kotlin
     fun postingLineSetup(senderIban: String, recipientIban: String, amount: BigDecimal, postingside: Postingside, paymentBookingTypePg: PaymentBookingTypePg, date: LocalDate): List<(KStubbing<FullPostingView>) -> Unit> {
        return listOf<(KStubbing<FullPostingView>) -> Unit>(
                { it.on { postingline_subledger_account_id() } doReturn slId },
                { it.on { posting_payment_sender_iban() } doReturn senderIban },
                { it.on { posting_payment_recipient_iban() } doReturn recipientIban },
                { it.on { posting_payment_booking_type() } doReturn paymentBookingTypePg },
                { it.on { postingline_side() } doReturn postingside },
                { it.on { postingline_amount() } doReturn amount }
        )
    }

    val mock = postingLineSetup().create(defaultAnswer = RETURNS_DEEP_STUBS)
```
Ok, __yes__. This is the shortcut, the create function:
```kotlin
     import com.nhaarman.mockito_kotlin.KStubbing
     import com.nhaarman.mockito_kotlin.mock
     import org.mockito.listeners.InvocationListener
     import org.mockito.mock.SerializableMode
     import org.mockito.stubbing.Answer
     import kotlin.reflect.KClass
     
     inline fun <reified T: Any> List<(KStubbing<T>)->Unit>.create(
             extraInterfaces: Array<KClass<out Any>>? = null,
             name: String? = null,
             spiedInstance: Any? = null,
             defaultAnswer: Answer<Any>? = null,
             serializable: Boolean = false,
             serializableMode: SerializableMode? = null,
             verboseLogging: Boolean = false,
             invocationListeners: Array<InvocationListener>? = null,
             stubOnly: Boolean = false,
             useConstructor: Boolean = false,
             outerInstance: Any? = null
             ): T{
             val self = this
             return mock(
                     extraInterfaces= extraInterfaces,
                     name= name,
                     spiedInstance= spiedInstance,
                     defaultAnswer= defaultAnswer,
                     serializable= serializable,
                     serializableMode= serializableMode,
                     verboseLogging= verboseLogging,
                     invocationListeners= invocationListeners,
                     useConstructor= useConstructor,
                     outerInstance= outerInstance
             ){
                 self.forEach{ it (this) }
             }
     }

```


* ## I want more complex features from this (21 lines of code long) assertion function. :(
__And I want help. Please.__ Feel free to open a pull request with code or suggestions, any input is appreciated.


# When to use this, and when not to use this
* Use this when it makes things easier and simpler.
* Don't use this to enforce condition coverage or to overcomplicate things. If you find yourself doing exactly that, I suggest to stop before you create the [egg laying, beak having, venomous, mamalian Frankenstein's platypus of unit testing](https://www.google.com/search?q=platypus&source=lnms&tbm=isch&sa=X&ved=2ahUKEwi-5t6xyazpAhVkQUEAHenSCM4Q_AUoAXoECBgQAw&biw=1440&bih=766)  and just use a [proper mutation testing framework instead](https://pitest.org/)  

