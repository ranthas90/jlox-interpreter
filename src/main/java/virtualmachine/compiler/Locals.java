package virtualmachine.compiler;

import virtualmachine.scanner.Token;

public class Locals {

    private Local[] locals;
    private int localCount;
    private int scopeDepth;

    private Locals enclosing;
    private Function function;
    private FunctionType type;

    private static final int MAX_SIGNED_BYTE = 127;

    public Locals(Locals enclosing, FunctionType type, String functionName) {
        this.enclosing = enclosing;
        this.type = type;

        function = new Function(functionName);
        locals = new Local[MAX_SIGNED_BYTE];
        localCount = 0;
        scopeDepth = 0;

        Token token;

        if (type != FunctionType.FUNCTION) {
            token = new Token(null, "this", -1);
        } else {
            token = new Token(null, "", -1);
        }

        locals[localCount++] = new Local(token, 0);
    }

    public Local[] getLocals() {
        return locals;
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

    public Locals getEnclosing() {
        return enclosing;
    }

    public Local getAt(int offset) {
        return locals[offset];
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
        locals[localCount - 1].setDepth(scopeDepth);
    }

    public void addLocal(Local local) {
        locals[localCount] = local;
        localCount++;
    }
}
