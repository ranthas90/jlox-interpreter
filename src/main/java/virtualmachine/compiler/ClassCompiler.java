package virtualmachine.compiler;

public class ClassCompiler {

    private ClassCompiler enclosing;
    private boolean hasSuperclass;

    public ClassCompiler(ClassCompiler enclosing) {
        this.enclosing = enclosing;
        this.hasSuperclass = false;
    }

    public ClassCompiler getEnclosing() {
        return enclosing;
    }

    public boolean isHasSuperclass() {
        return hasSuperclass;
    }

    public void setHasSuperclass(boolean hasSuperclass) {
        this.hasSuperclass = hasSuperclass;
    }
}
