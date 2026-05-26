package virtualmachine.compiler;

public class Function {

    //private Object object;
    private int arity;
    private Chunk chunk;
    private String name;

    public Function() {
        arity = 0;
        name = null;
        chunk = new Chunk();
    }

    public Chunk getChunk() {
        return chunk;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (name == null) {
            return "<script>";
        } else {
            return String.format("<fn %s>", name);
        }
    }
}
