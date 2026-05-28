package virtualmachine.vm;

public class ClockNativeFn implements NativeFn {

    @Override
    public Object call(Object... params) {
        return System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "ClockNativeFn{}";
    }
}
