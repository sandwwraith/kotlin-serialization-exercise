import exercise.CBOR
import exercise.Simple
import exercise.SmallZoo
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec
import java.io.ByteArrayInputStream
import javax.xml.bind.DatatypeConverter

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

class CBORReaderTest : WordSpec() {
    init {

        fun withTokenizer(input: String, block: CBOR.CBORTokenizer.() -> Unit) {
            val bytes = DatatypeConverter.parseHexBinary(input.toUpperCase())
            val tokenizer = CBOR.CBORTokenizer(ByteArrayInputStream(bytes))
            tokenizer.block()
        }

        "Primitives reading" should {
            "read integers" {
                withTokenizer("0C1903E8", {
                    nextNumber() shouldBe 12L
                    nextNumber() shouldBe 1000L
                })
                withTokenizer("203903e7", {
                    nextNumber() shouldBe -1L
                    nextNumber() shouldBe -1000L
                })
            }

            "read strings"{
                withTokenizer("6568656C6C6F", {
                    nextString() shouldBe "hello"
                })
                withTokenizer("7828737472696E672074686174206973206C6F6E676572207468616E2032332063686172616374657273", {
                    nextString() shouldBe "string that is longer than 23 characters"
                })
            }

            "read floats and doubles" {
                withTokenizer("fb7e37e43c8800759c", {
                    nextDouble() shouldBe 1e+300
                })
                withTokenizer("fa47c35000", {
                    nextFloat() shouldBe 100000.0f
                })
            }
        }

        "Object reading" should {
            "read simple object" {
                CBOR.loads<Simple>("bf616163737472ff") shouldBe Simple("str")
            }

            "read complicated object" {
                val test = SmallZoo(
                        "Hello, world!",
                        42,
                        null,
                        listOf("a", "b"),
                        mapOf(1 to true, 2 to false),
                        Simple("lol"),
                        listOf(Simple("kek"))
                )

                CBOR.loads<SmallZoo>(
                        "bf637374726d48656c6c6f2c20776f726c64216169182a686e756c6c61626c65f6646c6973749f61616162ff636d6170bf01f502f4ff65696e6e6572bf6161636c6f6cff6a696e6e6572734c6973749fbf6161636b656bffffff"
                ) shouldBe test
            }
        }
    }
}