package virtualmachine.compiler;

public class Locals {

    private Local[] locals;
    private int localCount;
    private int scopeDepth;

    public Locals() {
        locals = new Local[256]; // TODO: chequear cuanto hay que poner aqui, Integer.MAX_VALUE - 1 es demasiado
        localCount = 0;
        scopeDepth = 0;
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
}
