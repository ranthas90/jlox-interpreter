package virtualmachine.compiler;

import virtualmachine.vm.Closure;

import java.util.HashMap;
import java.util.Map;

public class ObjClass {

    private String name;
    private Map<String, Closure> methods;

    public ObjClass(String name) {
        this.name = name;
        this.methods = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, Closure> getMethods() {
        return methods;
    }

    public void addMethod(String name, Closure method) {
        methods.put(name, method);
    }

    public Closure getMethod(String name) {
        return methods.get(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
