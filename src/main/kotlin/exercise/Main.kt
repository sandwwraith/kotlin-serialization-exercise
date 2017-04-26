package exercise

import kotlin.serialization.JSON
import kotlin.serialization.Serializable

@Serializable
data class Test(
    val message: String
)

fun main(args: Array<String>) {
    val test = Test("Hello, world!")
    println(JSON.stringify(test))
    val zooString = JSON.stringify(zoo)
    val zooAgain = JSON.parse<Zoo>(zooString)
    println("Zoo test passes: ${zooAgain == zoo}")
}