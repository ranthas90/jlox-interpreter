package virtualmachine.vm;

import virtualmachine.compiler.Compiler;
import virtualmachine.compiler.Function;
import virtualmachine.compiler.FunctionType;
import virtualmachine.compiler.OpCode;
import virtualmachine.debug.Debugger;

import javax.print.attribute.standard.RequestingUserName;
import java.io.LineNumberInputStream;
import java.util.HashMap;
import java.util.Map;

public class VirtualMachine {

    private CallFrame[] frames;
    private int frameCount;
    private Object[] valueStack;
    private int valueStackCount;
    private int valueStackTop;
    private Map<String, Object> globals;

    private Debugger debugger = new Debugger();
    private Compiler compiler = new Compiler(FunctionType.SCRIPT);

    public enum InterpretResult {
        INTERPRET_OK,
        INTERPRET_COMPILE_ERROR,
        INTERPRET_RUNTIME_ERROR
    }

    public InterpretResult interpret(String source) {

        this.frames = new CallFrame[64]; // TODO: FRAMES_MAX
        this.frameCount = 0;
        this.valueStack = new Object[64 * 256]; // TODO: FRAMES_MAX * UINT8_COUNT
        this.valueStackCount = 0;
        this.valueStackTop = 0;
        this.globals = new HashMap<>();

        Function function = compiler.compile(source);
        if (function == null) {
            return InterpretResult.INTERPRET_COMPILE_ERROR;
        }

        pushValue(function);
        call(function, 0);

        return run();
    }

    private InterpretResult run() {
        CallFrame frame = frames[frameCount - 1];
        System.out.println("=== Running interpreter ===");
        while (true) {
            System.out.printf("Stack: (%d/%d) ::           ", valueStackCount, valueStack.length);
            for (Object slot : valueStack) {
                if (slot != null) {
                    System.out.printf("[ %s ]", slot);
                }
            }
            System.out.println();
            debugger.disassembleInstruction(frame.getFunction().getChunk(), frame.getInstructionPointer());
            byte instruction;
            switch (instruction = readByte(frame)) {
                case OpCode.CONSTANT -> pushValue(readConstant(frame));
                case OpCode.NIL -> pushValue(null);
                case OpCode.TRUE -> pushValue(true);
                case OpCode.FALSE -> pushValue(false);
                case OpCode.POP -> popValue();
                case OpCode.GET_LOCAL -> pushValue(frame.getSlots()[readByte(frame)]);
                case OpCode.SET_LOCAL -> frame.writeSlot(peekValue(0), readByte(frame));
                case OpCode.GET_GLOBAL -> {
                    Object constant = readConstant(frame);
                    Object constantValue = globals.get((String) constant);
                    if (constantValue == null) {
                        runtimeError(instruction, "Undefined variable '" + constant + "'");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    pushValue(constantValue);
                }
                case OpCode.DEFINE_GLOBAL -> {
                    Object constant = readConstant(frame);
                    globals.put((String) constant, peekValue(0));
                    popValue();
                }
                case OpCode.SET_GLOBAL -> {
                    Object constant = readConstant(frame);
                    if (!globals.containsKey((String) constant)) {
                        runtimeError(instruction, "Undefined variable '" + constant + "'");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    globals.put((String) constant, peekValue(0));
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
                    Double b = (Double) popValue();
                    Double a = (Double) popValue();
                    pushValue(a > b);
                }
                case OpCode.LESS -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    Double b = (Double) popValue();
                    Double a = (Double) popValue();
                    pushValue(a < b);
                }
                case OpCode.ADD -> {
                    if (peekValue(0) instanceof String && peekValue(1) instanceof String) {
                        String b = (String) popValue();
                        String a = (String) popValue();
                        pushValue(a + b);
                    } else if (peekValue(0) instanceof Double && peekValue(1) instanceof Double) {
                        Double b = (Double) popValue();
                        Double a = (Double) popValue();
                        pushValue(a + b);
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
                    Double b = (Double) popValue();
                    Double a = (Double) popValue();
                    pushValue(a - b);
                }
                case OpCode.MULTIPLY -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    Double b = (Double) popValue();
                    Double a = (Double) popValue();
                    pushValue(a * b);
                }
                case OpCode.DIVIDE -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    Double b = (Double) popValue();
                    Double a = (Double) popValue();
                    pushValue(a / b);
                }
                case OpCode.NOT -> pushValue(isFalsey(popValue()));
                case OpCode.NEGATE -> {
                    if (!(peekValue(0) instanceof Double)) {
                        runtimeError(instruction, "Operand must be a number");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    pushValue(((Double) popValue()) * -1.0D);
                }
                case OpCode.PRINT -> {
                    System.out.printf("%s", popValue());
                    System.out.println();
                }
                case OpCode.JUMP -> {
                    short offset = readShort(frame);
                    frame.setInstructionPointer(frame.getInstructionPointer() + offset);
                }
                case OpCode.JUMP_IF_FALSE -> {
                    short offset = readShort(frame);
                    if (isFalsey(peekValue(0))) {
                        frame.setInstructionPointer(frame.getInstructionPointer() + offset);
                    }
                }
                case OpCode.LOOP -> {
                    short offset = readShort(frame);
                    frame.setInstructionPointer(frame.getInstructionPointer() - offset);
                }
                case OpCode.CALL -> {
                    int argCount = readByte(frame);
                    if (!callValue(peekValue(argCount), argCount)) {
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    frame = frames[frameCount - 1];
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

    private void pushFrame(CallFrame frame) {
        frames[frameCount] = frame;
        frameCount++;
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
        return valueStack[valueStackTop - 1 - distance];
    }

    private boolean call(Function function, int argCount) {
        if (argCount != function.getArity()) {
            runtimeError((byte) 0, "Expected %d arguments but got %d"); // TODO: runtime error necesita extraer el instruction del bytecode en lugar de cogerlo por parámetro
            return false;
        }

        if (frameCount == 64) { // TODO: Usaar constante FRAMES_MAX
            runtimeError((byte) 0, "Stack overflow");
            return false;
        }

        CallFrame frame = new CallFrame();
        frame.setFunction(function);
        frame.setInstructionPointer(frame.getInstructionPointer());

        int length = valueStackTop - (valueStackTop - argCount - 1);
        Object[] slots = new Object[length];
        System.arraycopy(valueStack, valueStackTop - argCount -1, slots, 0,length);
        frame.setSlots(slots);

        pushFrame(frame);
        return true;
    }

    private boolean callValue(Object callee, int argCount) {
        if (callee instanceof Function) {
            return call((Function) callee, argCount);
        }
        runtimeError((byte) 0, "Can only call functions and classes"); // TODO: cambiar runtimeError para que obtenga la instruccion actual directamente del bytecode
        return false;
    }

    private void resetValueStack() {
        valueStack = new Object[256];
        valueStackTop = 0;
        valueStackCount = 0;
    }

    private void runtimeError(byte instruction, String message) {
        System.err.println(message);
        /* Previo
        CallFrame frame = frames[frameCount - 1];
        int line = frame.getFunction().getChunk().getLineAt(instruction);
        System.err.printf("[line %d] in script\n", line);
        resetValueStack();
         */
        for (int i = frameCount - 1; i >= 0; i--) {
            CallFrame frame = frames[i];
            Function function = frame.getFunction();
            byte instr = function.getChunk().getCodeAt(frame.getInstructionPointer()); // TODO: revisar con programa erroneo del libro!!!
            System.err.printf("[line %d] in ", frame.getFunction().getChunk().getLineAt(instr));
            if (function.getName() == null) {
                System.err.println("script");
            } else {
                System.err.printf("%s()\n", function.getName());
            }
        }
        /* Nuevo (implementación en C)
          for (int i = vm.frameCount - 1; i >= 0; i--) {
            CallFrame* frame = &vm.frames[i];
            ObjFunction* function = frame->function;
            size_t instruction = frame->ip - function->chunk.code - 1;
            fprintf(stderr, "[line %d] in ", function->chunk.lines[instruction]);
            if (function->name == NULL) {
                fprintf(stderr, "script\n");
            } else {
                fprintf(stderr, "%s()\n", function->name->chars);
            }
          }
         */
    }

    private boolean isFalsey(Object value) {
        return value == null || (value instanceof Boolean && !((Boolean) value));
    }

    private byte readByte(CallFrame frame) {
        byte byteRead = frame.getFunction().getChunk().getCodeAt(frame.getInstructionPointer());
        frame.incrementInstructionPointer();
        return byteRead;
    }

    private Object readConstant(CallFrame frame) {
        byte byteRead = readByte(frame);
        return frame.getFunction().getChunk().getConstantAt(byteRead);
    }

    private short readShort(CallFrame frame) {
        int high = frame.getFunction().getChunk().getCodeAt(frame.getInstructionPointer());
        frame.incrementInstructionPointer();
        int low = frame.getFunction().getChunk().getCodeAt(frame.getInstructionPointer());
        frame.incrementInstructionPointer();
        return (short) (((high & 0xFF) << 8) | (low & 0xFF));
    }
}
