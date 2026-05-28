package virtualmachine.vm;

public interface NativeFn {

    Object call(Object... params);

    @Override
    String toString();
}
