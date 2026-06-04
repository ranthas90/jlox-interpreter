package virtualmachine.compiler;

public class ObjClass {

    private String name;
    private Object object;

    public ObjClass(String name) {
        this.name = name;
        this.object = null;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
