package virtualmachine.compiler;

public class Function {

    //private Object object;
    private int arity;
    private int upvalueCount;
    private Chunk chunk;
    private String name;

    public Function(String name) {
        arity = 0;
        upvalueCount = 0;
        this.name = name;
        chunk = new Chunk();
    }

    public int getArity() {
        return arity;
    }

    public int getUpvalueCount() {
        return upvalueCount;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void incrementArity() {
        arity++;
    }

    public void incrementUpvalueCount() {
        upvalueCount++;
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
