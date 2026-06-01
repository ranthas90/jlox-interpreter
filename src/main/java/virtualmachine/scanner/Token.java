package virtualmachine.scanner;

public class Token {

    private final TokenType type;
    private final Object lexeme;
    private final int line;

    public Token(TokenType type) {
        this(type, null, -1);
    }

    public Token(TokenType type, Object lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public Object getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", literal=" + lexeme +
                ", line=" + line +
                '}';
    }
}
