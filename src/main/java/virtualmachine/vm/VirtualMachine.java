package virtualmachine.vm;

import virtualmachine.compiler.*;
import virtualmachine.compiler.Compiler;
import virtualmachine.debug.Debugger;

import java.util.HashMap;
import java.util.Map;

public class VirtualMachine {

    private byte instructionPointer;
    private Chunk chunk;
    private Object[] valueStack;
    private int valueStackCount;
    private int valueStackTop;
    private Map<String, Object> globals;

    private Debugger debugger = new Debugger();
    private Compiler compiler = new Compiler();

    public enum InterpretResult {
        INTERPRET_OK,
        INTERPRET_COMPILE_ERROR,
        INTERPRET_RUNTIME_ERROR
    }

    public InterpretResult interpret(String source) {

        this.chunk = new Chunk();
        this.instructionPointer = 0;
        this.valueStack = new Object[256];
        this.valueStackCount = 0;
        this.valueStackTop = 0;
        this.globals = new HashMap<>();

        if (!compiler.compile(source, chunk)) {
            chunk.free();
            return InterpretResult.INTERPRET_COMPILE_ERROR;
        }


        InterpretResult result = run();
        chunk.free();

        return result;
    }

    private InterpretResult run() {
        System.out.println("=== Running interpreter ===");
        while (true) {
            System.out.printf("Stack: (%d/%d) ::           ", valueStackCount, valueStack.length);
            for (Object slot : valueStack) {
                if (slot != null) {
                    System.out.printf("[ %s ]", slot);
                }
            }
            // TODO: si la pila está vacía, imprimirlo, junto con la info de los espacios ocupados y los totales (1/256)
            System.out.println();
            debugger.disassembleInstruction(this.chunk, this.instructionPointer);
            byte instruction;
            switch (instruction = chunk.getCodeAt(instructionPointer++)) {
                case OpCode.CONSTANT -> {
                    int constantIndex = chunk.getCodeAt(instructionPointer++);
                    Object constant = chunk.getConstantAt(constantIndex);
                    pushValue(constant);
                    System.out.printf("%s", constant);
                    System.out.println();
                }
                case OpCode.NIL -> pushValue(null);
                case OpCode.TRUE -> pushValue(true);
                case OpCode.FALSE -> pushValue(false);
                case OpCode.POP -> popValue();
                case OpCode.GET_LOCAL -> {
                    // TODO: revisar esto, los casteos byte <--> int son muy locos
                    byte slot = chunk.getCodeAt(instructionPointer);
                    pushValue(valueStack[slot]);
                }
                case OpCode.SET_LOCAL -> {
                    // TODO: revisar esto, los casteos byte <--> int son muy locos
                    byte slot = chunk.getCodeAt(instructionPointer);
                    valueStack[slot] = peekValue(0);
                }
                case OpCode.GET_GLOBAL -> {
                    int constantIndex = chunk.getCodeAt(instructionPointer++);
                    Object constant = chunk.getConstantAt(constantIndex);
                    Object constantValue = globals.get((String) constant);
                    if (constantValue == null) {
                        runtimeError(instruction, "Undefined variable '" + constant + "'");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    pushValue(constantValue);
                }
                case OpCode.DEFINE_GLOBAL -> {
                    int constantIndex = chunk.getCodeAt(instructionPointer++);
                    Object constant = chunk.getConstantAt(constantIndex);
                    globals.put((String)constant, peekValue(0));
                    popValue();
                }
                case OpCode.SET_GLOBAL -> {
                    int constantIndex = chunk.getCodeAt(instructionPointer++);
                    Object constant = chunk.getConstantAt(constantIndex);
                    if (!globals.containsKey((String) constant)) {
                        runtimeError(instruction, "Undefined variable '" + constant + "'");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    globals.put((String)constant, peekValue(0));
                }
                case OpCode.EQUAL -> {
                    Object a = popValue();
                    Object b = popValue();
                    if (a == null) {
                        pushValue(b == null);
                    } else {
                        pushValue(a.equals(b));
                    }
                }
                case OpCode.GREATER -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    pushValue(((Double) popValue()) > ((Double) popValue()));
                }
                case OpCode.LESS -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    pushValue(((Double) popValue()) < ((Double) popValue()));
                }
                case OpCode.ADD -> {
                    if (peekValue(0) instanceof String && peekValue(1) instanceof String) {
                        pushValue(((String) popValue()) + ((String) popValue()));
                    } else if (peekValue(0) instanceof Double && peekValue(1) instanceof Double) {
                        pushValue(((Double) popValue()) + ((Double) popValue()));
                    } else {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                }
                case OpCode.SUBTRACT -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    pushValue(((Double) popValue()) - ((Double) popValue()));
                }
                case OpCode.MULTIPLY -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    pushValue(((Double) popValue()) * ((Double) popValue()));
                }
                case OpCode.DIVIDE -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    pushValue(((Double) popValue()) / ((Double) popValue()));
                }
                case OpCode.NOT -> pushValue(isFalsey(popValue()));
                case OpCode.NEGATE -> {
                    if (!(peekValue(0) instanceof Double)) {
                        runtimeError(instruction, "Operand must be a number");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    pushValue(((Double)popValue()) * -1.0D);
                }
                case OpCode.PRINT -> {
                    System.out.printf("%s", popValue());
                    System.out.println();
                }
                case OpCode.JUMP_IF_FALSE -> {
                    short offset = 0; // TODO: POR HACER, MIRAR EL EQUIVALENTE EN C:
                    //#define READ_SHORT() \
                    //    (vm.ip += 2, (uint16_t)((vm.ip[-2] << 8) | vm.ip[-1]))
                    if (isFalsey(peekValue(0))) {
                        instructionPointer = (byte) ((int) instructionPointer + offset);
                    }
                }
                case OpCode.RETURN -> {
                    return InterpretResult.INTERPRET_OK;
                }
            }
        }
    }

    private void pushValue(Object value) {
        if (valueStackTop > 255) {
            // TODO: gestionar este tipo de error
            throw new RuntimeException("Exceeded value stack capacity");
        }
        valueStack[valueStackTop] = value;
        valueStackTop++;
        valueStackCount++;
    }

    private Object popValue() {
        if (valueStackTop == 0) {
            // TODO: gestionar este tipo de error
            throw new RuntimeException("Value stack is already empty");
        }
        valueStackTop--;
        valueStackCount--;
        // TODO: ¿ponemos a NULL el valor que acabamos de popear de la pila?
        return valueStack[valueStackTop];
    }

    private Object peekValue(int distance) {
        //return valueStack[-1 - distance];
        return valueStack[valueStackTop -1 - distance];
    }

    private void resetValueStack() {
        valueStack = new Object[256];
        valueStackTop = 0;
        valueStackCount = 0;
    }

    private void runtimeError(byte instruction, String message) {
        System.err.println(message);
        int line = chunk.getLineAt(instruction);
        System.err.printf("[line %d] in script\n", line);
        resetValueStack();
    }

    private boolean isFalsey(Object value) {
        return value == null || (value instanceof Boolean && !((Boolean) value));
    }
}
