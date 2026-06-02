package virtualmachine.vm;

// Represents a single ongoing function call.
public class CallFrame {

    // Ongoing function call.
    private Closure closure;

    // Instruction pointer for returning address.
    private int instructionPointer;

    // Pointer to the first slot that this function can use.
    private int basePointer;

    public Closure getClosure() {
        return closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
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
