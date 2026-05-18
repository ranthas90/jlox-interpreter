package virtualmachine;

public class ParseRule {

    private ParseFn prefix;
    private ParseFn infix;
    private Precedence precedence;

    public ParseRule(ParseFn prefix, ParseFn infix, Precedence precedence) {
        this.prefix = prefix;
        this.infix = infix;
        this.precedence = precedence;
    }

    public ParseFn getPrefix() {
        return prefix;
    }

    public void setPrefix(ParseFn prefix) {
        this.prefix = prefix;
    }

    public ParseFn getInfix() {
        return infix;
    }

    public void setInfix(ParseFn infix) {
        this.infix = infix;
    }

    public Precedence getPrecedence() {
        return precedence;
    }

    public void setPrecedence(Precedence precedence) {
        this.precedence = precedence;
    }
}
