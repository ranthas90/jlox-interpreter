package virtualmachine.compiler;

public final class DoubleValue extends Value {

    public DoubleValue(Object value) {
        super(value, ValueType.NUMBER); // TODO: check null
    }

    public Double getValue() {
        return (Double) super.getValue();
    }

    @Override
    public String toString() {
        return this.getValue().toString();
    }
}
