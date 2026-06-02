package virtualmachine.vm;

import virtualmachine.compiler.Function;

public class Closure {

    private Function function;
    private Upvalue[] upvalues;

    public Closure(Function function) {
        this.function = function;
        upvalues = new Upvalue[getUpvaluesCount()];
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public Upvalue[] getUpvalues() {
        return upvalues;
    }

    public int getUpvaluesCount() {
        return function.getUpvalueCount();
    }

    @Override
    public String toString() {
        return function.toString();
    }
}
