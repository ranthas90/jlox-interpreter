package virtualmachine.vm;

import virtualmachine.compiler.Function;

public class CallFrame {

    private Function function;
    private int instructionPointer;
    private Object[] slots;

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

    public Object[] getSlots() {
        return slots;
    }

    public void setSlots(Object[] slots) {
        this.slots = slots;
    }

    public void incrementInstructionPointer() {
        instructionPointer++;
    }

    public void writeSlot(Object slot, int offset) {
        slots[offset] = slot;
    }
}
