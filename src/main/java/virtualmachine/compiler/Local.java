package virtualmachine.compiler;

import virtualmachine.scanner.Token;

public class Local {

    private Token name;
    private int depth;

    public Local(Token name, int depth) {
        this.name = name;
        this.depth = depth;
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
}
