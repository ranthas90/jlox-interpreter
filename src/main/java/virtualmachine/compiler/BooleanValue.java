package virtualmachine.compiler;

public final class BooleanValue extends Value {

    public BooleanValue(Object value) {
        super(value, ValueType.BOOL);
    }

    public Boolean getValue() {
        return (Boolean) super.getValue();
    }

    @Override
    public String toString() {
        return this.getValue().toString();
    }
}
