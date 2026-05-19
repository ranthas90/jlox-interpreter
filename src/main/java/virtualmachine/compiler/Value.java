package virtualmachine.compiler;

public sealed class Value permits DoubleValue, BooleanValue, NilValue {

    protected static class ValueType {
        public static final int BOOL = 0;
        public static final int NUMBER = 1;
        public static final int NIL = 2;
    }

    private final Object value;
    private final int type;

    protected Value(Object value, int type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public boolean isNumber() {
        return type == ValueType.NUMBER;
    }

    public boolean isBool() {
        return type == ValueType.BOOL;
    }

    public boolean isNil() {
        return type == ValueType.NIL;
    }

    public  boolean equals(Value a) {
        if (type != a.type) {
            return false;
        }
        return switch (a.type) {
            case ValueType.BOOL, ValueType.NUMBER -> value == a.value;
            case ValueType.NIL -> true;
            default -> throw new IllegalStateException("Unexpected value: " + a.type);
        };
    }
}