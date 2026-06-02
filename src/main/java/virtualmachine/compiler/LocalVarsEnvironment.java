package virtualmachine.compiler;

import virtualmachine.scanner.Token;

public class LocalVarsEnvironment {

    private LocalVar[] localVars;
    private int localCount;
    private Upvalue[] upvalues;
    private int scopeDepth;

    // Parent environment for local variables
    private LocalVarsEnvironment enclosing;
    private Function function;
    private FunctionType type;

    private static final int MAX_SIGNED_BYTE = 127;

    public LocalVarsEnvironment(LocalVarsEnvironment enclosing, FunctionType type, String functionName) {
        this.enclosing = enclosing;
        this.type = type;

        function = new Function(functionName);
        localVars = new LocalVar[MAX_SIGNED_BYTE];
        localCount = 0;
        upvalues = new Upvalue[MAX_SIGNED_BYTE];
        scopeDepth = 0;

        // Each local variables context is tied to a function.
        // FunctionType.FUNCTION are user defined functions.
        // FunctionType.SCRIPT is the main script/program.
        // The first position in the stack is reserved for the function call

        Token token;

        if (type != FunctionType.FUNCTION) {
            token = new Token(null, "this", -1);
        } else {
            token = new Token(null, "", -1);
        }

        localVars[localCount++] = new LocalVar(token, 0);
    }

    public LocalVar[] getLocals() {
        return localVars;
    }

    public int getLocalCount() {
        return localCount;
    }

    public void setLocalCount(int localCount) {
        this.localCount = localCount;
    }

    public int getScopeDepth() {
        return scopeDepth;
    }

    public void setScopeDepth(int scopeDepth) {
        this.scopeDepth = scopeDepth;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public FunctionType getType() {
        return type;
    }

    public void setType(FunctionType type) {
        this.type = type;
    }

    public LocalVarsEnvironment getEnclosing() {
        return enclosing;
    }

    public LocalVar getAt(int offset) {
        return localVars[offset];
    }

    public void incrementScopeDepth() {
        scopeDepth++;
    }

    public void decrementScopeDepth() {
        scopeDepth--;
    }

    public void decrementLocalCount() {
        localCount--;
    }

    public void markInitialized() {
        localVars[localCount - 1].setDepth(scopeDepth);
    }

    public void addLocal(LocalVar localVar) {
        localVars[localCount] = localVar;
        localCount++;
    }

    public Upvalue getUpvalueAt(int index) {
        return upvalues[index];
    }

    public int addUpvalue(Upvalue upvalue) {
        int upvalueCount = function.getUpvalueCount();
        upvalues[upvalueCount] = upvalue;

        function.incrementUpvalueCount();

        return upvalueCount;
    }
}
