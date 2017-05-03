package exercise

import kotlin.serialization.JSON
import kotlin.serialization.Serializable

@Serializable
data class Test(
        val message: String
)

@Serializable
data class Simple(val a: String)

@Serializable
data class SmallZoo(
        val str: String,
        val i: Int,
        val nullable: Double?,
        val list: List<String>,
        val map: Map<Int, Boolean>,
        val inner: Simple,
        val innersList: List<Simple>
)

fun main(args: Array<String>) {
    val test = Test("Hello, world!")
    println(JSON.stringify(test))
    println(CBOR.dumps(test))

    val zooString = JSON.stringify(zoo)
    val zooAgain = JSON.parse<Zoo>(zooString)
    println("Zoo JSON test passes: ${zooAgain == zoo}")

    val zooBytes = CBOR.dump(zoo)
    val zooAgainFromCBOR = CBOR.load<Zoo>(zooBytes)
    println("Zoo test passes: ${zooAgainFromCBOR == zoo}")
}