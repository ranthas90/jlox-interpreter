package virtualmachine.debug;

import virtualmachine.compiler.Chunk;
import virtualmachine.compiler.Function;
import virtualmachine.compiler.OpCode;

public class Debugger {

    public void disassembleChunk(Chunk chunk, String name) {
        System.out.printf("== %s ==\n", name);
        int offset = 0;
        while (offset < chunk.getCodesCount()) {
            offset = disassembleInstruction(chunk, offset);
        }
    }

    public int disassembleInstruction(Chunk chunk, int offset) {
        System.out.printf("%04d ", offset);
        if (offset > 0 && (chunk.getLineAt(offset) == chunk.getLineAt(offset - 1))) {
            System.out.print("   | ");
        } else {
            System.out.printf("%4d ", chunk.getLineAt(offset));
        }
        byte instruction = chunk.getCodeAt(offset);
        return switch (instruction) {
            case OpCode.CONSTANT -> constantInstruction("OP_CONSTANT", chunk, offset);
            case OpCode.NIL -> simpleInstruction("OP_NIL", offset);
            case OpCode.TRUE -> simpleInstruction("OP_TRUE", offset);
            case OpCode.FALSE -> simpleInstruction("OP_FALSE", offset);
            case OpCode.POP -> simpleInstruction("OP_POP", offset);
            case OpCode.GET_LOCAL -> byteInstruction("OP_GET_LOCAL", chunk, offset);
            case OpCode.SET_LOCAL -> byteInstruction("OP_SET_LOCAL", chunk, offset);
            case OpCode.GET_GLOBAL -> constantInstruction("OP_GET_GLOBAL", chunk, offset);
            case OpCode.DEFINE_GLOBAL -> constantInstruction("OP_DEFINE_GLOBAL", chunk, offset);
            case OpCode.SET_GLOBAL -> constantInstruction("OP_SET_GLOBAL", chunk, offset);
            case OpCode.GET_UPVALUE -> byteInstruction("OP_GET_UPVALUE", chunk, offset);
            case OpCode.SET_UPVALUE -> byteInstruction("OP_SET_UPVALUE", chunk, offset);
            case OpCode.GET_PROPERTY -> constantInstruction("OP_GET_PROPERTY", chunk, offset);
            case OpCode.SET_PROPERTY -> constantInstruction("OP_SET_PROPERTY", chunk, offset);
            case OpCode.GET_SUPER -> constantInstruction("OP_GET_SUPER", chunk, offset);
            case OpCode.EQUAL -> simpleInstruction("OP_EQUAL", offset);
            case OpCode.GREATER -> simpleInstruction("OP_GREATER", offset);
            case OpCode.LESS -> simpleInstruction("OP_LESS", offset);
            case OpCode.ADD -> simpleInstruction("OP_ADD", offset);
            case OpCode.SUBTRACT -> simpleInstruction("OP_SUBTRACT", offset);
            case OpCode.MULTIPLY -> simpleInstruction("OP_MULTIPLY", offset);
            case OpCode.DIVIDE -> simpleInstruction("OP_DIVIDE", offset);
            case OpCode.NOT -> simpleInstruction("OP_NOT", offset);
            case OpCode.NEGATE -> simpleInstruction("OP_NEGATE", offset);
            case OpCode.PRINT -> simpleInstruction("OP_PRINT", offset);
            case OpCode.JUMP -> jumpInstruction("OP_JUMP",1,chunk, offset);
            case OpCode.JUMP_IF_FALSE -> jumpInstruction("OP_JUMP_IF_FALSE", 1, chunk, offset);
            case OpCode.LOOP -> jumpInstruction("OP_LOOP", -1, chunk, offset);
            case OpCode.CALL -> byteInstruction("OP_CALL", chunk, offset);
            case OpCode.INVOKE -> invokeInstruction("OP_INVOKE", chunk, offset);
            case OpCode.SUPER_INVOKE -> invokeInstruction("OP_SUPER_INVOKE", chunk, offset);
            case OpCode.CLOSURE -> {
                offset++;
                byte constantIndex = chunk.getCodeAt(offset++);
                System.out.printf("%-16s %4d ", "OP_CLOSURE", constantIndex);
                System.out.printf("%s\n", chunk.getConstantAt(constantIndex));

                Function function = (Function) chunk.getConstantAt(constantIndex);
                for (int j = 0; j < function.getUpvalueCount(); j++) {
                    int isLocal = chunk.getCodeAt(offset++);
                    int index = chunk.getCodeAt(offset++);
                    System.out.printf("%04d      |                     %s %d\n", offset - 2, isLocal == 1 ? "local" : "upvalue", index);
                }

                yield offset;
            }
            case OpCode.CLOSE_UPVALUE -> simpleInstruction("OP_CLOSE_UPVALUE", offset);
            case OpCode.RETURN -> simpleInstruction("OP_RETURN", offset);
            case OpCode.CLASS -> constantInstruction("OP_CLASS", chunk, offset);
            case OpCode.INHERIT -> simpleInstruction("OP_INHERIT", offset);
            case OpCode.METHOD -> constantInstruction("OP_METHOD", chunk, offset);
            default -> {
                System.out.printf("Unknown opcode %d\n", instruction);
                yield offset + 1;
            }
        };
    }

    private int constantInstruction(String name, Chunk chunk, int offset) {
        byte constantValue = chunk.getCodeAt(offset + 1);
        System.out.printf("%-16s %4d '", name, constantValue);
        System.out.printf("%s", chunk.getConstantAt(constantValue));
        System.out.println("'");
        return offset + 2;
    }

    private int simpleInstruction(String name, int offset) {
        System.out.printf("%s\n", name);
        return offset + 1;
    }

    private int byteInstruction(String name, Chunk chunk, int offset) {
        byte slot = chunk.getCodeAt(offset + 1);
        System.out.printf("%-16s %4d\n", name, slot);
        return offset + 2;
    }

    private int jumpInstruction(String name, int sign, Chunk chunk, int offset) {
        byte high = chunk.getCodeAt(offset + 1);
        byte low = chunk.getCodeAt(offset + 2);
        short jump = (short) (high | low);
        System.out.printf("%-16s %4d -> %d\n", name, offset, offset + 3 + sign * jump);
        return offset + 3;
    }

    private int invokeInstruction(String name, Chunk chunk, int offset) {
        byte constant = chunk.getCodeAt(offset + 1);
        byte argCount = chunk.getCodeAt(offset + 2);
        Object constantValue = chunk.getConstantAt(constant);
        System.out.printf("%-16s (%d args) %4d '%s'\n", name, argCount, constant, constantValue);
        return offset + 3;
    }
}
