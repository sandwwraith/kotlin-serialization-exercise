package exercise

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import javax.xml.bind.DatatypeConverter
import kotlin.experimental.or
import kotlin.serialization.*

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

class CBOR {

    internal class CBOREntryWriter(output: OutputStream) : CBORWriter(output) {
        override fun writeBeginToken() {
            // no-op
        }

        override fun writeEnd(desc: KSerialClassDesc) {
            // no-op
        }

        override fun writeElement(desc: KSerialClassDesc, index: Int) = true
    }

    internal class CBORMapWriter(output: OutputStream) : CBORWriter(output) {

        override fun writeBeginToken() {
            output.write(MAP_BEGIN)
        }

        override fun writeElement(desc: KSerialClassDesc, index: Int): Boolean = desc.getElementName(index) != "size"
    }

    internal class CBORListWriter(output: OutputStream) : CBORWriter(output) {

        override fun writeBeginToken() {
            output.write(ARRAY_BEGIN)
        }

        override fun writeElement(desc: KSerialClassDesc, index: Int): Boolean = desc.getElementName(index) != "size"
    }

    open class CBORWriter(val output: OutputStream) : ElementValueOutput() {

        protected open fun writeBeginToken() {
            output.write(MAP_BEGIN)
        }

        private var currentKind = KSerialClassKind.CLASS

        override fun writeBegin(desc: KSerialClassDesc, vararg typeParams: KSerializer<*>): KOutput {
            val writer = when (desc.kind) {
                KSerialClassKind.LIST, KSerialClassKind.SET -> CBORListWriter(output)
                KSerialClassKind.MAP -> CBORMapWriter(output)
                KSerialClassKind.ENTRY -> CBOREntryWriter(output)
                else -> CBORWriter(output)
            }
            currentKind = desc.kind
            writer.writeBeginToken()
            return writer
        }

        override fun writeEnd(desc: KSerialClassDesc) {
            output.write(BREAK)
        }

        override fun writeElement(desc: KSerialClassDesc, index: Int): Boolean {
            val name = desc.getElementName(index)
            writeStringValue(name)
            return true
        }

        override fun writeStringValue(value: String) {
            val data = value.toByteArray()
            val header = composeNumber(data.size.toLong())
            header[0] = header[0] or 0b011_00000
            output.write(header)
            output.write(data)
        }

        override fun writeFloatValue(value: Float) {
            val data = ByteBuffer.allocate(5)
                    .put(NEXT_FLOAT.toByte())
                    .putInt(java.lang.Float.floatToIntBits(value))
                    .array()
            output.write(data)
        }

        override fun writeDoubleValue(value: Double) {
            val data = ByteBuffer.allocate(9)
                    .put(NEXT_DOUBLE.toByte())
                    .putLong(java.lang.Double.doubleToLongBits(value))
                    .array()
            output.write(data)
        }

        override fun writeCharValue(value: Char) {
            output.write(composeNumber(value.toLong()))
        }

        override fun writeByteValue(value: Byte) {
            output.write(composeNumber(value.toLong()))
        }

        override fun writeShortValue(value: Short) {
            output.write(composeNumber(value.toLong()))
        }

        override fun writeIntValue(value: Int) {
            output.write(composeNumber(value.toLong()))
        }

        override fun writeLongValue(value: Long) {
            output.write(composeNumber(value))
        }

        override fun writeBooleanValue(value: Boolean) {
            output.write(if (value) TRUE else FALSE)
        }

        override fun writeNullValue() {
            output.write(NULL)
        }

        private fun composeNumber(value: Long): ByteArray =
                if (value >= 0) composePositive(value) else composeNegative(value)


        private fun composePositive(value: Long): ByteArray = when (value) {
            in 0..23 -> byteArrayOf(value.toByte())
            in 24..Byte.MAX_VALUE -> byteArrayOf(24, value.toByte())
            in Byte.MAX_VALUE + 1..Short.MAX_VALUE -> ByteBuffer.allocate(3).put(25.toByte()).putShort(value.toShort()).array()
            in Short.MAX_VALUE + 1..Int.MAX_VALUE -> ByteBuffer.allocate(5).put(26.toByte()).putInt(value.toInt()).array()
            in (Int.MAX_VALUE.toLong() + 1..Long.MAX_VALUE) -> ByteBuffer.allocate(9).put(27.toByte()).putLong(value).array()
            else -> throw IllegalArgumentException()
        }

        private fun composeNegative(value: Long): ByteArray {
            val aVal = if (value == Long.MIN_VALUE) Long.MAX_VALUE else -1 - value
            val data = composePositive(aVal)
            data[0] = data[0] or 0b001_00000
            return data
        }
    }

    companion object {
        private const val FALSE = 0xf4
        private const val TRUE = 0xf5
        private const val NULL = 0xf6

        private const val NEXT_FLOAT = 0xfa
        private const val NEXT_DOUBLE = 0xfb

        private const val ARRAY_BEGIN = 0x9f
        private const val MAP_BEGIN = 0xbf
        private const val BREAK = 0xff


        inline fun <reified T : Any> dump(obj: T): ByteArray {
            val output = ByteArrayOutputStream()
            val dumper = CBORWriter(output)
            dumper.write(T::class.serializer(), obj)
            return output.toByteArray()
        }

        inline fun <reified T : Any> dumps(obj: T): Any = DatatypeConverter.printHexBinary(dump(obj)).toLowerCase()

    }
}