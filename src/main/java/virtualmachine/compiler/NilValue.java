package virtualmachine.compiler;

public final class NilValue extends Value {

    public NilValue() {
        super(0.0D, ValueType.NIL);
    }

    @Override
    public String toString() {
        return "nil";
    }
}
