package virtualmachine.vm;

import virtualmachine.compiler.Function;

public class CallFrame {

    // Ongoing function call
    private Function function;

    // Instruction pointer for returning address
    private int returnPointer;

    // Pointer to function's first slot
    private int basePointer;

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public int getReturnPointer() {
        return returnPointer;
    }

    public void setReturnPointer(int returnPointer) {
        this.returnPointer = returnPointer;
    }

    public int getBasePointer() {
        return basePointer;
    }

    public void setBasePointer(int basePointer) {
        this.basePointer = basePointer;
    }

    public void incrementReturnPointer() {
        returnPointer++;
    }
}
