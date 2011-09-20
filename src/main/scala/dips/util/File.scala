package dips.util
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.nio.MappedByteBuffer
import java.nio.charset.Charset

object File {
	def read(path:String) = {
		val stream = new FileInputStream(new java.io.File(path))
		val fc = stream.getChannel()
		val bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size())
		/* Instead of using default, pass in a decoder. */
		Charset.forName("utf8").decode(bb).toString()
	}
}