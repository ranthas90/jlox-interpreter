package virtualmachine;

public class VirtualMachine {

    private byte instructionPointer;
    private Chunk chunk;
    private Object[] valueStack;
    private int valueStackTop;

    private Debugger debugger = new Debugger();
    private Compiler compiler = new Compiler();

    private enum InterpretResult {
        INTERPRET_OK,
        INTERPRET_COMPILE_ERROR,
        INTERPRET_RUNTIME_ERROR
    }

    InterpretResult interpret(Chunk chunk) {
        this.chunk = chunk;
        this.instructionPointer = 0;
        this.valueStack = new Object[256];
        this.valueStackTop = 0;
        return run();
    }

    InterpretResult interpret(String source) {
        /*
        Chunk chunk = new Chunk();
        compiler.compile(source); // TODO: ¿cómo gestionar errores de compilación?

        this.chunk = chunk;
        this.instructionPointer = 0;

        InterpretResult result = run();
        chunk.freeCodes();
        chunk.freeConstants();

        return result;
         */

        compiler.compile(source);
        return InterpretResult.INTERPRET_OK;
    }

    private void pushValue(Object value) {
        if (valueStackTop > 255) {
            // TODO: gestionar este tipo de error
            throw new RuntimeException("Exceeded value stack capacity");
        }
        valueStack[valueStackTop] = value;
        valueStackTop++;
    }

    private Object popValue() {
        if (valueStackTop == 0) {
            // TODO: gestionar este tipo de error
            throw new RuntimeException("Value stack is already empty");
        }
        valueStackTop--;
        return valueStack[valueStackTop];
    }

    private void resetValueStack() {
        valueStack = new Object[256];
        valueStackTop = 0;
    }

    InterpretResult run() {
        while(true) {
            System.out.printf("          ");
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
                    pushValue(constant);
                    System.out.printf("%s", constant);
                    System.out.println();
                }
                case OpCode.ADD -> {
                    double b = (double)popValue();
                    double a = (double)popValue();
                    pushValue(a + b);
                }
                case OpCode.SUBTRACT -> {
                    double b = (double)popValue();
                    double a = (double)popValue();
                    pushValue(a - b);
                }
                case OpCode.MULTIPLY -> {
                    double b = (double)popValue();
                    double a = (double)popValue();
                    pushValue(a * b);
                }
                case OpCode.DIVIDE -> {
                    double b = (double)popValue();
                    double a = (double)popValue();
                    pushValue(a / b);
                }
                case OpCode.NEGATE -> {
                    double val = ((double)popValue()) * -1.0D;
                    pushValue(val);
                }
                case OpCode.RETURN -> {
                    System.out.printf("%s", popValue());
                    System.out.println();
                    return InterpretResult.INTERPRET_OK;
                }
            };
        }
    }
}
