package virtualmachine.compiler;

public class ClassCompiler {

    private ClassCompiler enclosing;

    public ClassCompiler(ClassCompiler enclosing) {
        this.enclosing = enclosing;
    }

    public ClassCompiler getEnclosing() {
        return enclosing;
    }
}
