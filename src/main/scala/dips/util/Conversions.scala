package dips.util

object Conversions {
	implicit def int2bytearray(value:Int) = {
        val b = new Array[Byte](4)
        for (i <- 0 until 4) {
            val offset = (b.length - 1 - i) * 8
            b(i) = ((value >>> offset) & 0xFF).toByte
        }
        b
	}
}