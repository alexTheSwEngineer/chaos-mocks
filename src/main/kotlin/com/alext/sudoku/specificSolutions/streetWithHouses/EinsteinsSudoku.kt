package com.alext.sudoku.specificSolutions.streetWithHouses

import com.alext.sudoku.GenericSudoku
import com.alext.sudoku.printBoard
import com.alext.sudoku.specificSolutions.locks.propertyName
import com.alext.sudoku.specificSolutions.streetWithHouses.Properties.Companion.CIGARS
import com.alext.sudoku.specificSolutions.streetWithHouses.Properties.Companion.DRINK
import com.alext.sudoku.specificSolutions.streetWithHouses.Properties.Companion.HOUSENUMBER
import com.alext.sudoku.specificSolutions.streetWithHouses.Properties.Companion.NATIONALITY
import com.alext.sudoku.specificSolutions.streetWithHouses.Properties.Companion.PET
import com.alext.sudoku.specificSolutions.streetWithHouses.Properties.Companion.WALLS
import java.awt.Color
import com.alext.sudoku.specificSolutions.streetWithHouses.RowAssertion as Person

class Properties {
    companion object {
        public val WALLS: String = "walls"
        public val NATIONALITY: String = "nat"
        public val DRINK: String = "drink"
        public val PET: String = "pet"
        public val CIGARS: String = "cigars"
        public val HOUSENUMBER: String = "houseNumber"
    }
}

class Walls {
    companion object {
        val red = Pair(WALLS, "red")
        val green = Pair(WALLS, "gre")
        val white = Pair(WALLS, "whi")
        val yellow = Pair(WALLS, "yel")
        val blue = Pair(WALLS, "blu")

    }
}

class Nationalities {
    companion object {
        val Danish = Pair(NATIONALITY, "DAN")
        val Norwegian = Pair(NATIONALITY, "NOR")
        val Swedish = Pair(NATIONALITY, "SWE")
        val British = Pair(NATIONALITY, "BRI")
        val German = Pair(NATIONALITY, "GER")
    }
}

class Drinks {
    companion object {
        val tea = Pair(DRINK, "tea")
        val coffie = Pair(DRINK, "cof")
        val milk = Pair(DRINK, "mlk")
        val beer = Pair(DRINK, "ber")
        val water = Pair(DRINK, "wtr")
    }
}

class Pets {
    companion object {
        val dog = Pair(PET, "dog")
        val horse = Pair(PET, "hrs")
        val bird = Pair(PET, "brd")
        val cat = Pair(PET, "cat")
        val fish = Pair(PET, "***")
    }
}

class Smokes {
    companion object {
        val PallMall = Pair(CIGARS, "pml")
        val Dunhil = Pair(CIGARS, "dhl")
        val Blands = Pair(CIGARS, "bld")
        val Prince = Pair(CIGARS, "prc")
        val BlueMaster = Pair(CIGARS, "***")
    }
}

class HouseNumber {
    companion object {
        val One = Pair(HOUSENUMBER, "001")
        val Two = Pair(HOUSENUMBER, "002")
        val Three = Pair(HOUSENUMBER, "003")
        val Four = Pair(HOUSENUMBER, "004")
        val Five = Pair(HOUSENUMBER, "005")
    }
}

fun einsteinsSudoku(): GenericSudoku{
    var allPosiblePropertyValues = mapOf(
            WALLS to setOf(Walls.red, Walls.green, Walls.white, Walls.yellow, Walls.blue).map { it.second }.toSet(),
            NATIONALITY to setOf(Nationalities.Norwegian, Nationalities.German, Nationalities.British, Nationalities.Swedish, Nationalities.Danish).map { it.second }.toSet(),
            DRINK to setOf(Drinks.tea, Drinks.coffie, Drinks.milk, Drinks.beer, Drinks.water).map { it.second }.toSet(),
            PET to setOf(Pets.dog, Pets.horse, Pets.bird, Pets.cat, Pets.fish).map { it.second }.toSet(),
            CIGARS to setOf(Smokes.PallMall, Smokes.Dunhil, Smokes.Blands, Smokes.Prince, Smokes.BlueMaster).map { it.second }.toSet(),
            HOUSENUMBER to setOf(HouseNumber.One, HouseNumber.Two, HouseNumber.Three, HouseNumber.Four, HouseNumber.Five).map { it.second }.toSet()
    )

    val rules = listOf(
            Person().with(Nationalities.British).`is`(Person().with(Walls.red)),
            Person().with(Nationalities.Swedish).`is`(Person().with(Pets.dog)),
            Person().with(Nationalities.Danish).`is`(Person().that(Drinks.tea)),
            Person().that(Walls.green).isNeighbourWith(Person().with(Walls.white), ::immediatelyOnTheRight),
            Person().that(Smokes.PallMall).`is`(Person().that(Pets.bird)),
            Person().that(Smokes.Blands).isNeighbourWith(Person().with(PET).`is`(Pets.cat)),
            Person().that(Smokes.BlueMaster).`is`(Person().that(Drinks.beer)),
            Person().with(Nationalities.German).`is`(Person().that(Smokes.Prince)),
            Person().with(Walls.blue).`is`(Person().that(HouseNumber.Two)),
            Person().that(Smokes.Blands).isNeighbourWith(Person().`is`(Person().that(Drinks.water))),
            Person().that(Pets.horse).isNeighbourWith(Person().with(CIGARS).`is`(Smokes.Dunhil)),
            Person().with(Nationalities.Norwegian).`is`(Person().that(HouseNumber.One)),
            Person().with(Walls.green).`is`(Person().that(Drinks.coffie)),
            Person().with(Walls.yellow).`is`(Person().that(Smokes.Dunhil)),
            Person().with(HouseNumber.Three).`is`(Person().that(Drinks.milk)),
            Person()
    ).map { it.toRule() }

    val solution = listOf(
          mutableMapOf(HouseNumber.One,Walls.yellow, Nationalities.Norwegian, Smokes.Dunhil,Drinks.water,Pets.cat),
          mutableMapOf(HouseNumber.Two,Walls.blue, Nationalities.Danish, Smokes.Blands,Drinks.tea,Pets.horse),
          mutableMapOf(HouseNumber.Three,Walls.red, Nationalities.British, Smokes.PallMall,Drinks.milk,Pets.bird),
          mutableMapOf(HouseNumber.Four,Walls.green, Nationalities.German, Smokes.Prince,Drinks.coffie,Pets.fish),
          mutableMapOf(HouseNumber.Five,Walls.white, Nationalities.Swedish, Smokes.BlueMaster,Drinks.beer,Pets.dog)
    )

    GenericSudoku(
            state = solution,
            availablePropertyValues = allPosiblePropertyValues,
            rules = rules,
            solveCriteria = {_->true}
    ).let {
        it.printBoard()
    }

        rules[3](solution)
        rules.forEachIndexed(){ index,it ->
            if(!it(solution)){
                println("Rule $index failed")
            }
        }
    println()
    val initialState = GenericSudoku.emptyBoard(5)
    initialState[0][HOUSENUMBER] = HouseNumber.One.second
    initialState[1][HOUSENUMBER] = HouseNumber.Two.second
    initialState[2][HOUSENUMBER] = HouseNumber.Three.second
    initialState[3][HOUSENUMBER] = HouseNumber.Four.second
    initialState[4][HOUSENUMBER] = HouseNumber.Five.second
    initialState[0][NATIONALITY] = Nationalities.Norwegian.second
    return GenericSudoku(
            availablePropertyValues = allPosiblePropertyValues,
            state = GenericSudoku.emptyBoard(5),
            solveCriteria = { state ->
                state.all { person -> person.size == allPosiblePropertyValues.size} &&
                        rules.all {  it(state) }
            },
            debugInterceptor = { board,name->
                println()
                println(name)
                board.printBoard()
                println()
            },
            posibleMovesDebugger ={a,b,c->GenericSudoku.printPosibleMoves(a,b,c)},
            rules = rules
    )
}

fun testSudoku(): GenericSudoku{
    var allPosiblePropertyValues = mapOf(
            WALLS to setOf(Walls.red, Walls.green, Walls.white, Walls.yellow, Walls.blue).map { it.second }.toSet(),
            NATIONALITY to setOf(Nationalities.Norwegian, Nationalities.German, Nationalities.British, Nationalities.Swedish, Nationalities.Danish).map { it.second }.toSet(),
            HOUSENUMBER to setOf(HouseNumber.One, HouseNumber.Two, HouseNumber.Three, HouseNumber.Four, HouseNumber.Five).map { it.second }.toSet()
    )

    val rules = listOf(
            Person().with(Nationalities.British).`is`(Person().with(Walls.green)),
            Person().with(Walls.green).isNeighbourWith(Person().with(WALLS).`is`(Walls.white), ::immediatelyOnTheRight),
            Person().with(Walls.white).`is`(Nationalities.Norwegian),
            Person().with(Nationalities.Norwegian).`is`(HouseNumber.Four)
    ).map { it.toRule() }
/*
   walls       nat         houseNumber
1  red         GER         001
2  yel         SWE         002
3  gre         BRI         003
4  whi         NOR         004
5  blu         DAN         005
*/
    return GenericSudoku(
            availablePropertyValues = allPosiblePropertyValues,
            state = GenericSudoku.emptyBoard(5),
            solveCriteria = { state ->
                state.all { person -> person.size == allPosiblePropertyValues.size} &&
                        rules.all {  it(state) }
            },
            debugInterceptor = { board,name->
                println()
                println(name)
                board.printBoard()
                println()
            },
            rules = rules
    )
}

fun main() {

    val solved = einsteinsSudoku().solve()?.apply {
        printBoard(numeredRows = false)
    }
    println()
}

fun Person.isNeighbourWith(other: Person, neighbournessAssertion: (Int,Int)->Boolean = {l,r->Math.abs(l-r)==1}):Person{
    return this.with(HOUSENUMBER).let {
           Person(
                   it.candidates+it.predicates,
                   it.predicates+ listOf{state,me->
                       val myHouseNm = me[HOUSENUMBER]!!.toInt()
                       val neighbours = state.filter { it.containsKey(HOUSENUMBER) &&  neighbournessAssertion(myHouseNm,it[HOUSENUMBER]!!.toInt()) }
                       if(neighbours.isEmpty()){
                           return@listOf true
                       }

                       if(neighbours.any{neghbour -> other.predicates.all { it(state,neghbour) }}){
                           //there are still neighbours that might fit
                           return@listOf true
                       }

                       return@listOf neighbours.any {neghbour ->
                           !other.predicates.all { it(state,neghbour) } || other.predicates.all { it(state,neghbour)}
                       }
                   },
                   false
           )
    }
}

fun somewhereOnTheLeft(person: Int, neighbour: Int): Boolean {
    return person - neighbour < 0
}

fun somewhereOnTheRight(person: Int, neighbour: Int): Boolean {
    return person - neighbour > 0
}

fun immediatelyOnTheRight(person: Int, neighbour: Int): Boolean {
    return person - neighbour == -1
}


fun immediatelyOnTheLeft(person: Int, neighbour: Int): Boolean {
    return (person - neighbour) == 1
}

/*
     walls       nat         drink       pet         cigars      houseNumber
1    gre         NOR         cof         brd         pml         001
2    blu         SWE         ber         dog         ***         002
3    red         BRI         mlk         hrs         bld         003
4    whi         GER         wtr         cat         prc         004
5    yel         DAN         tea         ***         dhl         005

*/