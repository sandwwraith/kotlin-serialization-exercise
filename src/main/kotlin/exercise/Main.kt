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
    println(CBOR.dumps(test))

    val zooString = JSON.stringify(zoo)
    val zooAgain = JSON.parse<Zoo>(zooString)
    println("Zoo JSON test passes: ${zooAgain == zoo}")

    val zooBytes = CBOR.dump(zoo)
    val zooAgainFromCBOR = CBOR.load<Zoo>(zooBytes)
    println("Zoo CBOR test passes: ${zooAgainFromCBOR == zoo}")
}