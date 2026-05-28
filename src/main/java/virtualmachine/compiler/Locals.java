package virtualmachine.compiler;

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

        // TODO: revisar capítulo de funciones donde se reserva el stack 0 para la propia llamada de la función
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
