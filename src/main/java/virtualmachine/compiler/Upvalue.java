package virtualmachine.compiler;

public class Upvalue {

    private byte index;
    private boolean isLocal;

    public Upvalue(byte index, boolean isLocal) {
        this.index = index;
        this.isLocal = isLocal;
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }
}
