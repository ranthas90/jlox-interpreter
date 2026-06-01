package virtualmachine.vm;

import virtualmachine.compiler.Function;

// Represents a single ongoing function call.
public class CallFrame {

    // Ongoing function call.
    private Function function;

    // Instruction pointer for returning address.
    private int instructionPointer;

    // Pointer to the first slot that this function can use.
    private int basePointer;

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public int getInstructionPointer() {
        return instructionPointer;
    }

    public void setInstructionPointer(int instructionPointer) {
        this.instructionPointer = instructionPointer;
    }

    public int getBasePointer() {
        return basePointer;
    }

    public void setBasePointer(int basePointer) {
        this.basePointer = basePointer;
    }

    public void incrementReturnPointer() {
        instructionPointer++;
    }
}
