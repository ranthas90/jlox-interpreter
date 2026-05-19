package virtualmachine.parser;

public class ParseRule {

    private ParseFn prefix;
    private ParseFn infix;
    private int precedence;

    public ParseRule(ParseFn prefix, ParseFn infix, int precedence) {
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

    public int getPrecedence() {
        return precedence;
    }

    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }
}
