package virtualmachine.vm;

import virtualmachine.compiler.*;
import virtualmachine.compiler.Compiler;
import virtualmachine.debug.Debugger;

import java.util.HashMap;
import java.util.Map;

public class VirtualMachine {

    private CallFrame[] frames;
    private int frameCount;
    private Object[] valueStack;
    private int valueStackTop;
    private Upvalue openUpvalues; // Linked list, tiene un atributo de su mismo tipo que apunta al siguiente elemento de la lista
    private Map<String, Object> globals;

    private Debugger debugger = new Debugger();
    private Compiler compiler = new Compiler();

    private static final int FRAMES_MAX = 64;
    private static final int STACK_MAX = FRAMES_MAX * 127;

    public enum InterpretResult {
        INTERPRET_OK,
        INTERPRET_COMPILE_ERROR,
        INTERPRET_RUNTIME_ERROR
    }

    public VirtualMachine() {
        // TODO: mover aquí las distintas inicializaciones de frames, value stack ,compiler, etc
        globals = new HashMap<>();

        defineNativeFunction("clock", new ClockNativeFn());
        resetValueStack();
    }

    public InterpretResult interpret(String source) {

        this.frames = new CallFrame[FRAMES_MAX];
        this.frameCount = 0;
        this.valueStack = new Object[STACK_MAX];
        this.valueStackTop = 0;

        Function function = compiler.compile(source);
        if (function == null) {
            return InterpretResult.INTERPRET_COMPILE_ERROR;
        }

        pushValue(function);
        //call(function, 0);
        Closure closure = new Closure(function);
        popValue();
        Object value = closure;
        pushValue(value);
        call(closure, 0);

        return run();
    }

    private InterpretResult run() {
        CallFrame frame = frames[frameCount - 1];
        System.out.println("=== Running interpreter ===");
        while (true) {
            debug(frame);
            byte instruction;
            switch (instruction = readByte(frame)) {
                case OpCode.CONSTANT -> pushValue(readConstant(frame));
                case OpCode.NIL -> pushValue(null);
                case OpCode.TRUE -> pushValue(true);
                case OpCode.FALSE -> pushValue(false);
                case OpCode.POP -> popValue();
                case OpCode.GET_LOCAL -> {
                    int localSlot = readByte(frame);
                    Object value = valueStack[frame.getBasePointer() + localSlot];
                    pushValue(value);
                }
                case OpCode.SET_LOCAL -> {
                    int localSlot = readByte(frame);
                    valueStack[frame.getBasePointer() + localSlot] = peekValue(0);
                }
                case OpCode.GET_GLOBAL -> {
                    Object constant = readConstant(frame);
                    Object constantValue = globals.get((String) constant);
                    if (constantValue == null) {
                        runtimeError("Undefined variable '" + constant + "'");
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
                        runtimeError("Undefined variable '" + constant + "'");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    globals.put((String) constant, peekValue(0));
                }
                case OpCode.GET_UPVALUE ->{
                    byte slot = readByte(frame);
                    Upvalue upvalue = frame.getClosure().getUpvalues()[slot];
                    if (upvalue.getLocation() == -1) {
                        pushValue(upvalue.getClosedValue());
                    } else {
                        pushValue(valueStack[upvalue.getLocation()]);
                    }
                }
                case OpCode.SET_UPVALUE -> {
                    byte slot = readByte(frame);
                    // TODO: revisar, hay que implementar la misma solucion que con GET_UPVALUE para localizar valores cerrados
                    int valueStackIndex = frame.getClosure().getUpvalues()[slot].getLocation();
                    valueStack[valueStackIndex] = peekValue(0);
                }
                case OpCode.GET_PROPERTY -> {
                    if (!(peekValue(0) instanceof ObjInstance)) {
                        runtimeError("Only instances have properties");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }

                    ObjInstance instance = (ObjInstance) peekValue(0);
                    String name = readString(frame);
                    Object instanceFieldValue = instance.getFieldByName(name);
                    if (instanceFieldValue != null) {
                        popValue(); // Pops instance from stack
                        pushValue(instanceFieldValue); // Pushes instance field value
                        break;
                    }

                    if (!bindMethod(instance.getClazz(), name)) {
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                }
                case OpCode.SET_PROPERTY -> {
                    // Cuando se ejecuta el SET_PROPERTY, en la cima de la pila tiene la instancia cuyo campo está
                    // siendo setteado, y por encima de este registro, el valor que se va a settear.
                    // Por eso, primero popeamos el valor, luego la instancia y finalmente volvemos a pushear el valor
                    // a la cima de la pila. Lo que hemos hecho, realmente, es eliminar el 2º valor de la pila.

                    // Otra nota: no hace falta declarar los atributos de la clase. Si se intenta settear un atributo
                    // que no existe, se crea.
                    if (!(peekValue(1) instanceof ObjInstance)) {
                        runtimeError("Only instances have fields");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }

                    ObjInstance instance = (ObjInstance) peekValue(1);
                    instance.addValueToField(readString(frame), peekValue(0));
                    Object value = popValue();
                    popValue();
                    pushValue(value);
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
                        runtimeError("Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    Double b = (Double) popValue();
                    Double a = (Double) popValue();
                    pushValue(a > b);
                }
                case OpCode.LESS -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError("Operands must be numbers");
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
                        runtimeError("Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                }
                case OpCode.SUBTRACT -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError("Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    Double b = (Double) popValue();
                    Double a = (Double) popValue();
                    pushValue(a - b);
                }
                case OpCode.MULTIPLY -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError("Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    Double b = (Double) popValue();
                    Double a = (Double) popValue();
                    pushValue(a * b);
                }
                case OpCode.DIVIDE -> {
                    if (!(peekValue(0) instanceof Double) || !(peekValue(1) instanceof Double)) {
                        runtimeError("Operands must be numbers");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    Double b = (Double) popValue();
                    Double a = (Double) popValue();
                    pushValue(a / b);
                }
                case OpCode.NOT -> pushValue(isFalsey(popValue()));
                case OpCode.NEGATE -> {
                    if (!(peekValue(0) instanceof Double)) {
                        runtimeError("Operand must be a number");
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
                case OpCode.INVOKE -> {
                    String method = readString(frame);
                    int argCount = readByte(frame);
                    if (!invoke(method, argCount)) {
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    frame = frames[frameCount - 1];
                }
                case OpCode.CLOSURE -> {
                    Object function = readConstant(frame);
                    Closure closure = new Closure((Function) function);
                    pushValue(closure);
                    for (int i = 0; i < closure.getUpvaluesCount(); i++) {
                        byte isLocal = readByte(frame);
                        byte index = readByte(frame);
                        if (isLocal == 1) {
                            closure.getUpvalues()[i] = captureUpvalue(frame.getBasePointer() + index);
                        } else {
                            closure.getUpvalues()[i] = frame.getClosure().getUpvalues()[index];
                        }
                    }
                }
                case OpCode.CLOSE_UPVALUE -> {
                    closeUpvalues(valueStackTop - 1);
                    popValue();
                }
                case OpCode.RETURN -> {
                    Object result = popValue();
                    closeUpvalues(frame.getBasePointer());
                    frameCount--;
                    if (frameCount == 0) {
                        popValue();
                        return InterpretResult.INTERPRET_OK;
                    }

                    valueStackTop = frame.getBasePointer();
                    pushValue(result);
                    frame = frames[frameCount - 1];
                }
                case OpCode.CLASS -> pushValue(new ObjClass(readString(frame)));
                case OpCode.METHOD -> defineMethod(readString(frame));
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
        // TODO: ¿ponemos a NULL el valor que acabamos de popear de la pila?
        return valueStack[valueStackTop];
    }

    private Object peekValue(int distance) {
        return valueStack[valueStackTop - 1 - distance];
    }

    private void defineNativeFunction(String name, NativeFn nativeFn) {
        globals.put(name, nativeFn);
    }

    private boolean call(Closure closure, int argCount) {
        if (argCount != closure.getFunction().getArity()) {
            runtimeError(String.format("Expected %d arguments but got %d", closure.getFunction().getArity(), argCount));
            return false;
        }

        if (frameCount == 64) { // TODO: Usaar constante FRAMES_MAX
            runtimeError("Stack overflow");
            return false;
        }

        CallFrame frame = new CallFrame();
        frame.setClosure(closure);
        frame.setInstructionPointer(0); // TODO: esto sigue sin convencerme!
        frame.setBasePointer(valueStackTop - 1 - argCount);

        pushFrame(frame);
        return true;
    }

    private boolean callValue(Object callee, int argCount) {
        if (callee instanceof BoundMethod) {
            BoundMethod boundMethod = (BoundMethod)  callee;
            valueStack[valueStackTop - 1 - argCount] = boundMethod.getReceiver();
            return call(boundMethod.getMethod(), argCount);
        } else if (callee instanceof ObjClass) {
            ObjClass clazz = (ObjClass)  callee;
            ObjInstance instance = new ObjInstance(clazz);
            valueStack[valueStackTop - 1 - argCount] = instance;

            Closure initializer = clazz.getMethod("init");
            if (initializer != null) {
                return call(initializer, argCount);
            } else if (argCount != 0) {
                runtimeError("Expected 0 arguments but got " + argCount);
                return false;
            }
            return true;
        } else if (callee instanceof Closure) {
            return call((Closure) callee, argCount);
        } else if (callee instanceof NativeFn) {
            NativeFn nativeFn = (NativeFn) callee;
            // TODO: los parametros los sacamos contando hacia atras desde stackTop un total de argCount veces,
            Object result = nativeFn.call();
            valueStackTop = valueStackTop - (argCount + 1);
            pushValue(result);
            return true;
        }
        runtimeError("Can only call functions and classes");
        return false;
    }

    private boolean invokeFromClass(ObjClass clazz, String name, int argCount) {
        Closure method = clazz.getMethod(name);
        if (method == null) {
            runtimeError("Undefined property '" + name + "'");
            return false;
        }
        return call(method, argCount);
    }

    private boolean invoke(String name, int argCount) {
        Object receiver = peekValue(argCount);

        if (!(receiver instanceof ObjInstance)) {
            runtimeError("Only instances have methods");
            return false;
        }

        ObjInstance instance = (ObjInstance) receiver;
        Object value = instance.getFieldByName(name);
        if (value != null) {
            valueStack[valueStackTop - 1 - argCount] = value;
            return callValue(value, argCount);
        }

        return invokeFromClass(instance.getClazz(), name, argCount);
    }

    private boolean bindMethod(ObjClass clazz, String name) {
        Closure method = clazz.getMethod(name);
        if (method == null) {
            runtimeError(String.format("Undefined property '%s'", name));
            return false;
        }

        BoundMethod boundMethod = new BoundMethod(peekValue(0), method);

        popValue();
        pushValue(boundMethod);
        return true;
    }

    private Upvalue captureUpvalue(int location) {
        Upvalue previousUpvalue = null;
        Upvalue upvalue = openUpvalues;
        while (upvalue != null && upvalue.getLocation() > location) {
            previousUpvalue = upvalue;
            upvalue = upvalue.getNext();
        }

        if (upvalue != null && upvalue.getLocation() == location) {
            return upvalue;
        }

        Upvalue createdUpvalue = new Upvalue(location);
        createdUpvalue.setNext(upvalue);

        if (previousUpvalue == null) {
            openUpvalues = createdUpvalue;
        } else {
            previousUpvalue.setNext(createdUpvalue);
        }

        return createdUpvalue;
    }

    private void closeUpvalues(int last) {
        while(openUpvalues != null && openUpvalues.getLocation() >= last) {
            Upvalue upvalue = openUpvalues;
            upvalue.setClosedValue(valueStack[upvalue.getLocation()]);
            upvalue.setLocation(-1);
            openUpvalues = upvalue.getNext();
            upvalue.setNext(null);
        }
    }

    private void defineMethod(String name) {
        Closure method = (Closure) peekValue(0);
        ObjClass clazz = (ObjClass) peekValue(1);
        clazz.addMethod(name, method);
        popValue();
    }

    private void resetValueStack() {
        valueStack = new Object[256];
        valueStackTop = 0;
    }

    private void runtimeError(String message) {
        System.err.println(message);
        for (int i = frameCount - 1; i >= 0; i--) {
            CallFrame frame = frames[i];
            Function function = frame.getClosure().getFunction();
            byte instr = function.getChunk().getCodeAt(frame.getInstructionPointer()); // TODO: revisar con programa erroneo del libro!!!
            System.err.printf("[line %d] in ", frame.getClosure().getFunction().getChunk().getLineAt(instr));
            if (function.getName() == null) {
                System.err.println("script");
            } else {
                System.err.printf("%s()\n", function.getName());
            }
        }
    }

    private boolean isFalsey(Object value) {
        return value == null || (value instanceof Boolean && !((Boolean) value));
    }

    private byte readByte(CallFrame frame) {
        byte byteRead = frame.getClosure().getFunction().getChunk().getCodeAt(frame.getInstructionPointer());
        frame.incrementReturnPointer();
        return byteRead;
    }

    private Object readConstant(CallFrame frame) {
        byte byteRead = readByte(frame);
        return frame.getClosure().getFunction().getChunk().getConstantAt(byteRead);
    }

    private short readShort(CallFrame frame) {
        int high = frame.getClosure().getFunction().getChunk().getCodeAt(frame.getInstructionPointer());
        frame.incrementReturnPointer();
        int low = frame.getClosure().getFunction().getChunk().getCodeAt(frame.getInstructionPointer());
        frame.incrementReturnPointer();
        return (short) (((high & 0xFF) << 8) | (low & 0xFF));
    }

    private String readString(CallFrame frame) {
        return (String) readConstant(frame);
    }

    private void debug(CallFrame frame) {
        System.out.print("Stack   ::           ");
        for (int i = 0; i < valueStackTop; i++) {
            System.out.printf("[ %s ]", valueStack[i]);
        }
        System.out.println();

        // Print globals
        System.out.print("Globals ::           ");
        globals.forEach((key, val) -> System.out.printf("[ %s :: %s ]", key, val));
        System.out.println();

        debugger.disassembleInstruction(frame.getClosure().getFunction().getChunk(), frame.getInstructionPointer());
        System.out.println();
    }
}
