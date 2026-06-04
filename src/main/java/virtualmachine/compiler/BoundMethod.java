package virtualmachine.compiler;

import virtualmachine.vm.Closure;

public class BoundMethod {

    private Object receiver;
    private Closure method;

    public BoundMethod(Object receiver, Closure method) {
        this.receiver = receiver;
        this.method = method;
    }

    public Object getReceiver() {
        return receiver;
    }

    public Closure getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "<method " + method.getFunction().toString() + ">";
    }
}
