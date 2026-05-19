package virtualmachine;

import virtualmachine.scanner.Token;

public class Parser {

    private Token previous;
    private Token current;
    private boolean hadError;
    private boolean panicMode;

    public Parser() {
        previous = null;
        current = null;
        hadError = false;
        panicMode = false;
    }

    public Token getPrevious() {
        return previous;
    }

    public void setPrevious(Token previous) {
        this.previous = previous;
    }

    public Token getCurrent() {
        return current;
    }

    public void setCurrent(Token current) {
        this.current = current;
    }

    public boolean isHadError() {
        return hadError;
    }

    public void setHadError(boolean hadError) {
        this.hadError = hadError;
    }

    public boolean isPanicMode() {
        return panicMode;
    }

    public void setPanicMode(boolean panicMode) {
        this.panicMode = panicMode;
    }
}
