package virtualmachine.debug;

import virtualmachine.compiler.Chunk;
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
            case OpCode.EQUAL -> simpleInstruction("OP_EQUAL", offset);
            case OpCode.GREATER -> simpleInstruction("OP_GREATER", offset);
            case OpCode.LESS -> simpleInstruction("OP_LESS", offset);
            case OpCode.ADD -> simpleInstruction("OP_ADD", offset);
            case OpCode.SUBTRACT -> simpleInstruction("OP_SUBTRACT", offset);
            case OpCode.MULTIPLY -> simpleInstruction("OP_MULTIPLY", offset);
            case OpCode.DIVIDE -> simpleInstruction("OP_DIVIDE", offset);
            case OpCode.NOT -> simpleInstruction("OP_NOT", offset);
            case OpCode.NEGATE -> simpleInstruction("OP_NEGATE", offset);
            case OpCode.RETURN -> simpleInstruction("OP_RETURN", offset);
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
}
