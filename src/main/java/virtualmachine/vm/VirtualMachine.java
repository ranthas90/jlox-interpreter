package virtualmachine.vm;

import virtualmachine.compiler.*;
import virtualmachine.compiler.Compiler;
import virtualmachine.debug.Debugger;

public class VirtualMachine {

    private byte instructionPointer;
    private Chunk chunk;
    private Value[] valueStack;
    private int valueStackTop;

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
        this.valueStack = new Value[256];
        this.valueStackTop = 0;

        if (!compiler.compile(source, chunk)) {
            chunk.free();
            return InterpretResult.INTERPRET_COMPILE_ERROR;
        }


        InterpretResult result = run();
        chunk.free();

        return result;
    }

    private InterpretResult run() {
        while (true) {
            System.out.print("          ");
            for (Object slot : valueStack) {
                System.out.printf("[ %s ]", slot);
            }
            System.out.println();
            debugger.disassembleInstruction(this.chunk, this.instructionPointer);
            byte instruction;
            switch (instruction = chunk.getCodeAt(instructionPointer++)) {
                case OpCode.CONSTANT -> {
                    int constantIndex = chunk.getCodeAt(instructionPointer++);
                    Object constant = chunk.getConstantAt(constantIndex);
                    pushValue(new DoubleValue(constant));
                    System.out.printf("%s", constant);
                    System.out.println();
                }
                case OpCode.NIL -> pushValue(new NilValue());
                case OpCode.TRUE -> pushValue(new BooleanValue(true));
                case OpCode.FALSE -> pushValue(new BooleanValue(false));
                case OpCode.EQUAL -> {
                    Value a = popValue();
                    Value b = popValue();
                    pushValue(new BooleanValue(a.equals(b)));
                }
                case OpCode.GREATER -> {
                    if (!peekValue(0).isNumber() || !peekValue(1).isNumber()) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    double a = new DoubleValue(popValue().getValue()).getValue();
                    double b = new DoubleValue(popValue().getValue()).getValue();
                    pushValue(new BooleanValue(a > b));
                }
                case OpCode.LESS -> {
                    if (!peekValue(0).isNumber() || !peekValue(1).isNumber()) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    double a = new DoubleValue(popValue().getValue()).getValue();
                    double b = new DoubleValue(popValue().getValue()).getValue();
                    pushValue(new BooleanValue(a < b));
                }
                case OpCode.ADD -> {
                    if (!peekValue(0).isNumber() || !peekValue(1).isNumber()) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    double a = new DoubleValue(popValue().getValue()).getValue();
                    double b = new DoubleValue(popValue().getValue()).getValue();
                    pushValue(new DoubleValue(a + b));
                }
                case OpCode.SUBTRACT -> {
                    if (!peekValue(0).isNumber() || !peekValue(1).isNumber()) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    double a = new DoubleValue(popValue().getValue()).getValue();
                    double b = new DoubleValue(popValue().getValue()).getValue();
                    pushValue(new DoubleValue(a - b));
                }
                case OpCode.MULTIPLY -> {
                    if (!peekValue(0).isNumber() || !peekValue(1).isNumber()) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    double a = new DoubleValue(popValue().getValue()).getValue();
                    double b = new DoubleValue(popValue().getValue()).getValue();
                    pushValue(new DoubleValue(a * b));
                }
                case OpCode.DIVIDE -> {
                    if (!peekValue(0).isNumber() || !peekValue(1).isNumber()) {
                        runtimeError(instruction, "Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    double a = new DoubleValue(popValue().getValue()).getValue();
                    double b = new DoubleValue(popValue().getValue()).getValue();
                    pushValue(new DoubleValue(a / b));
                }
                case OpCode.NOT -> pushValue(new BooleanValue(isFalsey(popValue())));
                case OpCode.NEGATE -> {
                    if (!peekValue(0).isNumber()) {
                        runtimeError(instruction, "Operand must be a number");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    double val = new DoubleValue(popValue().getValue()).getValue() * -1.0D;
                    pushValue(new DoubleValue(val));
                }
                case OpCode.RETURN -> {
                    System.out.printf("%s", popValue());
                    System.out.println();
                    return InterpretResult.INTERPRET_OK;
                }
            }
        }
    }

    private void pushValue(Value value) {
        if (valueStackTop > 255) {
            // TODO: gestionar este tipo de error
            throw new RuntimeException("Exceeded value stack capacity");
        }
        valueStack[valueStackTop] = value;
        valueStackTop++;
    }

    private Value popValue() {
        if (valueStackTop == 0) {
            // TODO: gestionar este tipo de error
            throw new RuntimeException("Value stack is already empty");
        }
        valueStackTop--;
        return valueStack[valueStackTop];
    }

    private Value peekValue(int distance) {
        return valueStack[-1 - distance];
    }

    private void resetValueStack() {
        valueStack = new Value[256];
        valueStackTop = 0;
    }

    private void runtimeError(byte instruction, String message) {
        System.err.println(message);
        int line = chunk.getLineAt(instruction);
        System.err.printf("[line %d] in script\n", line);
        resetValueStack();
    }

    private boolean isFalsey(Value value) {
        return value.isNil() ||
                (value.isBool() && !((BooleanValue) value).getValue());
    }
}
