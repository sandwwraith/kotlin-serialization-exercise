package exercise

import java.io.*
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

    internal class CBORMapWriter(output: OutputStream) : CBORListWriter(output) {
        override fun writeBeginToken() = output.write(BEGIN_MAP)
    }

    internal open class CBORListWriter(output: OutputStream) : CBORWriter(output) {
        override fun writeBeginToken() = output.write(BEGIN_ARRAY)

        override fun writeElement(desc: KSerialClassDesc, index: Int): Boolean = desc.getElementName(index) != "size"
    }

    open class CBORWriter(val output: OutputStream) : ElementValueOutput() {

        protected open fun writeBeginToken() = output.write(BEGIN_MAP)

        override fun writeBegin(desc: KSerialClassDesc, vararg typeParams: KSerializer<*>): KOutput {
            val writer = when (desc.kind) {
                KSerialClassKind.LIST, KSerialClassKind.SET -> CBORListWriter(output)
                KSerialClassKind.MAP -> CBORMapWriter(output)
                KSerialClassKind.ENTRY -> CBOREntryWriter(output)
                else -> CBORWriter(output)
            }
            writer.writeBeginToken()
            return writer
        }

        override fun writeEnd(desc: KSerialClassDesc) = output.write(BREAK)

        override fun writeElement(desc: KSerialClassDesc, index: Int): Boolean {
            val name = desc.getElementName(index)
            writeStringValue(name)
            return true
        }

        override fun writeStringValue(value: String) {
            val data = value.toByteArray()
            val header = composeNumber(data.size.toLong())
            header[0] = header[0] or HEADER_STRING
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

        override fun writeCharValue(value: Char) = output.write(composeNumber(value.toLong()))
        override fun writeByteValue(value: Byte) = output.write(composeNumber(value.toLong()))
        override fun writeShortValue(value: Short) = output.write(composeNumber(value.toLong()))
        override fun writeIntValue(value: Int) = output.write(composeNumber(value.toLong()))
        override fun writeLongValue(value: Long) = output.write(composeNumber(value))

        override fun writeBooleanValue(value: Boolean) = output.write(if (value) TRUE else FALSE)

        override fun writeNullValue() = output.write(NULL)

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
            data[0] = data[0] or HEADER_NEGATIVE
            return data
        }
    }

    internal class CBOREntryReader(reader: CBORTokenizer) : CBORReader(reader) {
        private var ind = 0

        override fun skipBeginToken() {
            // no-op
        }

        override fun readEnd(desc: KSerialClassDesc) {
            // no-op
        }

        override fun readElement(desc: KSerialClassDesc) = when (ind++) {
            0 -> 0
            1 -> 1
            else -> READ_DONE
        }
    }

    internal class CBORMapReader(reader: CBORTokenizer) : CBORListReader(reader) {
        override fun skipBeginToken() = reader.skipByte(BEGIN_MAP)
    }

    open class CBORListReader(reader: CBORTokenizer) : CBORReader(reader) {
        private var ind = 0

        override fun skipBeginToken() = reader.skipByte(BEGIN_ARRAY)

        override fun readElement(desc: KSerialClassDesc) = if (reader.curByte == BREAK) READ_DONE else ++ind
    }

    open class CBORReader(val reader: CBORTokenizer) : ElementValueInput() {

        protected open fun skipBeginToken() = reader.skipByte(BEGIN_MAP)

        override fun readBegin(desc: KSerialClassDesc, vararg typeParams: KSerializer<*>): KInput {
            val re = when (desc.kind) {
                KSerialClassKind.LIST, KSerialClassKind.SET -> CBORListReader(reader)
                KSerialClassKind.MAP -> CBORMapReader(reader)
                KSerialClassKind.ENTRY -> CBOREntryReader(reader)
                else -> CBORReader(reader)
            }
            re.skipBeginToken()
            return re
        }

        override fun readEnd(desc: KSerialClassDesc) = reader.skipByte(BREAK)

        override fun readElement(desc: KSerialClassDesc): Int {
            if (reader.curByte == BREAK) return READ_DONE
            val elemName = reader.nextString()
            return desc.getElementIndex(elemName)
        }

        override fun readStringValue() = reader.nextString()

        override fun readNotNullMark(): Boolean = reader.curByte != NULL

        override fun readDoubleValue() = reader.nextDouble()
        override fun readFloatValue() = reader.nextFloat()

        override fun readBooleanValue(): Boolean {
            val ans = when (reader.curByte) {
                TRUE -> true
                FALSE -> false
                else -> throw CBORParsingException("Expected boolean value")
            }
            reader.nextByte()
            return ans
        }

        override fun readByteValue() = reader.nextNumber().toByte()
        override fun readShortValue() = reader.nextNumber().toShort()
        override fun readCharValue() = reader.nextNumber().toChar()
        override fun readIntValue() = reader.nextNumber().toInt()
        override fun readLongValue() = reader.nextNumber()

        override fun readNullValue(): Nothing? {
            reader.skipByte(NULL)
            return null
        }
    }

    class CBORTokenizer(val input: InputStream) {
        var curByte: Int = -1
            private set

        init {
            nextByte()
        }

        fun nextByte(): Int {
            curByte = input.read()
            return curByte
        }

        fun skipByte(expected: Int) {
            if (curByte != expected) throw CBORParsingException("Unexpected byte")
            nextByte()
        }

        fun nextString(): String {
            val strLen = readNumber().toInt()
            val arr = readExactNBytes(strLen)
            val ans = String(arr, Charsets.UTF_8)
            nextByte()
            return ans
        }

        fun nextNumber(): Long {
            val res = readNumber()
            nextByte()
            return res
        }

        private fun readNumber(): Long {
            val value = curByte and 0b000_11111
            val negative = (curByte and 0b111_00000) == HEADER_NEGATIVE.toInt()
            val bytesToRead = when (value) {
                24 -> 1
                25 -> 2
                26 -> 4
                27 -> 8
                else -> 0
            }
            if (bytesToRead == 0) {
                if (negative) return -(value + 1).toLong()
                else return value.toLong()
            }
            val buf = readToByteBuffer(bytesToRead)
            val res = when (bytesToRead) {
                1 -> buf.get().toLong()
                2 -> buf.getShort().toLong()
                4 -> buf.getInt().toLong()
                8 -> buf.getLong()
                else -> throw IllegalArgumentException()
            }
            if (negative) return -(res + 1)
            else return res
        }

        fun nextFloat(): Float {
            if (curByte != NEXT_FLOAT) throw CBORParsingException("Expected float header")
            val res = readToByteBuffer(4).getFloat()
            nextByte()
            return res
        }

        fun nextDouble(): Double {
            if (curByte != NEXT_DOUBLE) throw CBORParsingException("Expected double header")
            val res = readToByteBuffer(8).getDouble()
            nextByte()
            return res
        }

        private fun readToByteBuffer(bytes: Int): ByteBuffer {
            val arr = readExactNBytes(bytes)
            val buf = ByteBuffer.allocate(bytes)
            buf.put(arr).flip()
            return buf
        }

        private fun readExactNBytes(bytes: Int): ByteArray {
            val array = ByteArray(bytes)
            var read = 0
            while (read < bytes) {
                val i = input.read(array, read, bytes - read)
                if (i == -1) throw CBORParsingException("Unexpected EOF")
                read += i
            }
            return array
        }

    }

    companion object {
        private const val FALSE = 0xf4
        private const val TRUE = 0xf5
        private const val NULL = 0xf6

        private const val NEXT_FLOAT = 0xfa
        private const val NEXT_DOUBLE = 0xfb

        private const val BEGIN_ARRAY = 0x9f
        private const val BEGIN_MAP = 0xbf
        private const val BREAK = 0xff

        private const val HEADER_STRING: Byte = 0b011_00000
        private const val HEADER_NEGATIVE: Byte = 0b001_00000


        inline fun <reified T : Any> dump(obj: T): ByteArray {
            val output = ByteArrayOutputStream()
            val dumper = CBORWriter(output)
            dumper.write(T::class.serializer(), obj)
            return output.toByteArray()
        }

        inline fun <reified T : Any> dumps(obj: T): String = DatatypeConverter.printHexBinary(dump(obj)).toLowerCase()

        inline fun <reified T : Any> load(raw: ByteArray): T {
            val stream = ByteArrayInputStream(raw)
            val loader = CBORReader(CBORTokenizer(stream))
            return loader.read(T::class.serializer())
        }

        inline fun <reified T : Any> loads(hex: String): T = load(DatatypeConverter.parseHexBinary(hex.toUpperCase()))
    }
}

class CBORParsingException(message: String = "Unknown error") : IOException(message)