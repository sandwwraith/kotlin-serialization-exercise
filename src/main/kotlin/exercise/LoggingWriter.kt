package exercise

import kotlin.serialization.*

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

class LoggingWriter : ElementValueOutput() {
    override fun writeBegin(desc: KSerialClassDesc, vararg typeParams: KSerializer<*>): KOutput {
        println("writeBegin ${desc.kind}")
        return this
    }

    override fun writeEnd(desc: KSerialClassDesc) {
        println("writeEnd $desc")
    }

    override fun writeBooleanValue(value: Boolean) {
        println("writeBoolean : $value")
    }

    override fun writeByteValue(value: Byte) {
        println("writeByte : $value")
    }

    override fun writeCharValue(value: Char) {
        println("writeChar : $value")
    }

    override fun writeDoubleValue(value: Double) {
        println("writeDouble : $value")
    }

    override fun writeIntValue(value: Int) {
        println("writeInt : $value")
    }

    override fun writeLongValue(value: Long) {
        println("writeLong : $value")
    }

    override fun writeElement(desc: KSerialClassDesc, index: Int): Boolean {
        println("writeElement: ${desc.getElementName(index)} , $index")
        return true
    }

    override fun writeNullValue() {
        println("writeNull")
    }

    override fun writeStringValue(value: String) {
        println("writeString : $value")
    }

    override fun writeUnitValue() {
        println("writeUnit")
    }

    override fun writeValue(value: Any) {
        println("writeValue : $value")
    }

    companion object {
        fun <T> stringify(saver: KSerialSaver<T>, obj: T): Unit {
            val output = LoggingWriter()
            output.write(saver, obj)
        }

        inline fun <reified T : Any> stringify(obj: T) = stringify(T::class.serializer(), obj)
    }
}