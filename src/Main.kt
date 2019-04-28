import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: binary <rom file>")
        exitProcess(1)
    }
    val romFile = File(args[0])
    val bytes = romFile.readBytes()

    val disassembler = Disassembler(bytes.toTypedArray())
    disassembler.parseRawBytes()
    disassembler.disassemble()
}