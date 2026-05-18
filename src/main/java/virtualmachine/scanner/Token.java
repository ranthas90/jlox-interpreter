package virtualmachine.scanner;

public class Token {

    private final TokenType type;
    private final Object literal;
    private final int line;

    public Token(TokenType type) {
        this(type, null, -1);
    }

    public Token(TokenType type, Object literal, int line) {
        this.type = type;
        this.literal = literal;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", literal=" + literal +
                ", line=" + line +
                '}';
    }
}
