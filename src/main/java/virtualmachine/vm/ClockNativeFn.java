package virtualmachine.vm;

public class ClockNativeFn implements NativeFn {

    @Override
    public Object call(Object... params) {
        return Double.parseDouble(Long.toString(System.currentTimeMillis()));
    }

    @Override
    public String toString() {
        return "ClockNativeFn{}";
    }
}
