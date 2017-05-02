package exercise

import kotlin.serialization.Serializable

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

@Serializable
data class Simple(val a: String)

fun main(args: Array<String>) {

    val test = SmallZoo(
            "Hello, world!",
            42,
            null,
            listOf("a", "b"),
            mapOf(1 to true, 2 to false),
            Simple("lol"),
            listOf(Simple("kek"))
    )
    LoggingWriter.stringify(test)
//    println(JSON.stringify(test))
//    println(CBOR.dumps(test))
    val str = CBOR.dumps(test)
    println(str)
    println(CBOR.loads<SmallZoo>(str))
//    val zooString = JSON.stringify(zoo)
//    val zooAgain = JSON.parse<Zoo>(zooString)
//    println("Zoo test passes: ${zooAgain == zoo}")
}