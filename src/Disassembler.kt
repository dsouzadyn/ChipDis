import java.lang.Exception
import kotlin.experimental.and

class Disassembler(private val rawBytes: Array<Byte>) {
    private var opcodes: MutableList<OpCode> = mutableListOf()

    fun parseRawBytes() {
        var ip = 0
        println("Parsing bytes")
        while(ip <= rawBytes.size - 1) {
            try {
                ip += when (rawBytes[ip] and 0xf0.toByte()) {
                    0x00.toByte() -> {
                        opcodes.add(OpCode(ip, rawBytes[ip], listOf(rawBytes[ip + 1])))
                        2
                    }
                    0x10.toByte(), 0x20.toByte(), 0xa0.toByte(), 0xb0.toByte() -> {
                        opcodes.add(OpCode(ip, rawBytes[ip], listOf((rawBytes[ip] and 0x0f), rawBytes[ip + 1])))
                        2
                    }
                    0x30.toByte(), 0x40.toByte(), 0x60.toByte(), 0x70.toByte(), 0xc0.toByte() -> {
                        opcodes.add(OpCode(ip, rawBytes[ip], listOf(rawBytes[ip + 1])))
                        2
                    }
                    0x50.toByte() -> {
                        val x = rawBytes[ip] and 0x0f
                        val y = ((rawBytes[ip + 1].toInt() and 0xf0) shr 4).toByte()
                        opcodes.add(OpCode(ip, rawBytes[ip], listOf(x, y)))
                        2
                    }
                    0x80.toByte() -> {
                        val x = rawBytes[ip] and 0x0f
                        val y = ((rawBytes[ip + 1].toInt() and 0xf0) shr 4).toByte()
                        val opcode = ((rawBytes[ip].toInt() and 0xf0) or (rawBytes[ip+1].toInt() and 0x0f)).toByte()
                        opcodes.add(OpCode(ip, opcode, listOf(x, y)))
                        2
                    }
                    0xd0.toByte() -> {
                        val x = rawBytes[ip] and 0x0f
                        val y = ((rawBytes[ip + 1].toInt() and 0xf0) shr 4).toByte()
                        val n = (rawBytes[ip + 1].toInt() and 0x0f).toByte()
                        opcodes.add(OpCode(ip, rawBytes[ip], listOf(x, y, n)))
                        2
                    }
                    0xe0.toByte() -> {
                        opcodes.add(OpCode(ip, rawBytes[ip], listOf((rawBytes[ip] and 0x0f), rawBytes[ip + 1])))
                        2
                    }
                    0xf0.toByte() -> {
                        val x = rawBytes[ip] and 0x0f
                        opcodes.add(OpCode(ip, rawBytes[ip], listOf(x, rawBytes[ip + 1])))
                        2
                    }
                    else -> {
                        println("Unknown %2x".format(rawBytes[ip] and 0xf0.toByte()))
                        1
                    }
                }
            } catch (e: Exception) {
                println("Exception at: $ip")
                e.printStackTrace()
                break
            }

        }
        println("Done")
    }
    fun disassemble() {
        var st: String
        val base = 0x200
        for(opcode in opcodes) {
            print("%-4x ".format(base + opcode.ip))
            when ((opcode.code and 0xf0.toByte())) {
                0x00.toByte() -> {
                    when (opcode.operands[0]) {
                        0xe0.toByte() -> {
                            st = String.format("%-8s", "CLS")
                            println(st)
                        }
                        0xee.toByte() -> {
                            st = String.format("%-8s", "RET")
                            println(st)
                        }
                    }
                }
                0x10.toByte() -> {
                    st = String.format("%-8s 0x%x%02x", "JP", opcode.operands[0], opcode.operands[1])
                    println(st)
                }
                0x20.toByte() -> {
                    st = String.format("%-8s 0x%x%02x", "CALL", opcode.operands[0], opcode.operands[1])
                    println(st)
                }
                0x30.toByte() -> {
                    st = String.format("%-8s V%x, 0x%02x", "SE", (opcode.code and 0x0f.toByte()), opcode.operands[0])
                    println(st)
                }
                0x40.toByte() -> {
                    st = String.format("%-8s V%x, 0x%02x", "SNE", (opcode.code and 0x0f.toByte()), opcode.operands[0])
                    println(st)
                }
                0x50.toByte() -> {
                    st = String.format("%-8s V%x, V%x", "SE", opcode.operands[0], opcode.operands[1])
                    println(st)
                }
                0x60.toByte() -> {
                    st = String.format("%-8s V%x, 0x%02x", "LD", (opcode.code and 0x0f.toByte()), opcode.operands[0])
                    println(st)
                }
                0x70.toByte() -> {
                    st = String.format("%-8s V%x, 0x%02x", "ADD", (opcode.code and 0x0f.toByte()), opcode.operands[0])
                    println(st)
                }
                0x80.toByte() -> {
                    when (opcode.code) {
                        0x80.toByte() ->{
                            st = String.format("%-8s V%x, V%x", "LD", opcode.operands[0], opcode.operands[1])
                            println(st)
                        }
                        0x81.toByte() ->{
                            st = String.format("%-8s V%x, V%x", "OR", opcode.operands[0], opcode.operands[1])
                            println(st)
                        }
                        0x82.toByte() ->{
                            st = String.format("%-8s V%x, V%x", "AND", opcode.operands[0], opcode.operands[1])
                            println(st)
                        }
                        0x83.toByte() ->{
                            st = String.format("%-8s V%x, V%x", "XOR", opcode.operands[0], opcode.operands[1])
                            println(st)
                        }
                        0x84.toByte() ->{
                            st = String.format("%-8s V%x, V%x", "ADD", opcode.operands[0], opcode.operands[1])
                            println(st)
                        }
                        0x85.toByte() ->{
                            st = String.format("%-8s V%x, V%x", "SUB", opcode.operands[0], opcode.operands[1])
                            println(st)
                        }
                        0x86.toByte() ->{
                            st = String.format("%-8s V%x, V%x", "SHR", opcode.operands[0], opcode.operands[1])
                            println(st)
                        }
                        0x87.toByte() ->{
                            st = String.format("%-8s V%x, V%x", "SUBN", opcode.operands[0], opcode.operands[1])
                            println(st)
                        }
                        0x8e.toByte() ->{
                            st = String.format("%-8s V%x, V%x", "SHL", opcode.operands[0], opcode.operands[1])
                            println(st)
                        }
                    }
                }
                0xa0.toByte() -> {
                    st = String.format("%-8s I, 0x%x%02x", "LD", opcode.operands[0], opcode.operands[1])
                    println(st)
                }
                0xb0.toByte() -> {
                    st = String.format("%-8s V0, 0x%x%02x", "JP", opcode.operands[0], opcode.operands[1])
                    println(st)
                }
                0xc0.toByte() -> {
                    st = String.format("%-8s V%x, 0x%02x", "RND", (opcode.code and 0x0f.toByte()), opcode.operands[0])
                    println(st)
                }
                0xd0.toByte() -> {
                    st = String.format("%-8s V%x, V%x, %x", "DRW", opcode.operands[0], opcode.operands[1],
                        opcode.operands[2])
                    println(st)
                }
                0xe0.toByte() -> {
                    when (opcode.operands[1]) {
                        0x9e.toByte() -> {
                            st = String.format("%-8s V%x", "SKP", (opcode.code and 0x0f.toByte()))
                            println(st)
                        }
                        0xa1.toByte() -> {
                            st = String.format("%-8s V%x", "SKNP", (opcode.code and 0x0f.toByte()))
                            println(st)
                        }
                    }
                }
                0xf0.toByte() -> {
                    when (opcode.operands[1]) {
                        0x07.toByte() -> {
                            st = String.format("%-8s V%x, DT", "LD", opcode.operands[0])
                            println(st)
                        }
                        0x0a.toByte() -> {
                            st = String.format("%-8s V%x, K", "LD", opcode.operands[0])
                            println(st)
                        }
                        0x15.toByte() -> {
                            st = String.format("%-8s DT, V%x", "LD", opcode.operands[0])
                            println(st)
                        }
                        0x18.toByte() -> {
                            st = String.format("%-8s ST, V%x", "LD", opcode.operands[0])
                            println(st)
                        }
                        0x1e.toByte() -> {
                            st = String.format("%-8s I, DT", "ADD", opcode.operands[0])
                            println(st)
                        }
                        0x29.toByte() -> {
                            st = String.format("%-8s F, V%x", "LD", opcode.operands[0])
                            println(st)
                        }
                        0x33.toByte() -> {
                            st = String.format("%-8s B, V%x", "LD", opcode.operands[0])
                            println(st)
                        }
                        0x55.toByte() -> {
                            st = String.format("%-8s ${base + opcode.ip}, V%x", "LD", opcode.operands[0])
                            println(st)
                        }
                        0x65.toByte() -> {
                            st = String.format("%-8s V%x, ${base + opcode.ip}", "LD", opcode.operands[0])
                            println(st)
                        }
                    }
                }
            }
        }
    }
}