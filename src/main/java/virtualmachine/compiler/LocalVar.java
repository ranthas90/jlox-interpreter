package virtualmachine.compiler;

import virtualmachine.scanner.Token;

public class LocalVar {

    private Token name;
    private int depth;
    private boolean isCaptured;

    public LocalVar(Token name, int depth, boolean isCaptured) {
        this.name = name;
        this.depth = depth;
        this.isCaptured = isCaptured;
    }

    public Token getName() {
        return name;
    }

    public void setName(Token name) {
        this.name = name;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isCaptured() {
        return isCaptured;
    }

    public void setCaptured(boolean captured) {
        isCaptured = captured;
    }
}
