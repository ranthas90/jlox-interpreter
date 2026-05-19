package virtualmachine;

import virtualmachine.scanner.Scanner;
import virtualmachine.scanner.Token;
import virtualmachine.scanner.TokenType;

import java.util.HashMap;
import java.util.Map;

public class Compiler {

    private Scanner scanner;
    private Parser parser;
    private Chunk compilingChunk;

    private final Map<TokenType, ParseRule> rules;
    {
        rules = new HashMap<>();
        rules.put(TokenType.LEFT_PAREN, new ParseRule(new GroupingParseFn(), null, Precedence.NONE));
        rules.put(TokenType.RIGHT_PAREN, new ParseRule(null, null, Precedence.NONE));
        rules.put(TokenType.LEFT_BRACE, new ParseRule(null, null, Precedence.NONE));
        rules.put(TokenType.RIGHT_BRACE, new ParseRule(null, null, Precedence.NONE));
        rules.put(TokenType.COMMA, new ParseRule(null, null, Precedence.NONE));
        rules.put(TokenType.DOT, new ParseRule(null, null, Precedence.NONE));
        rules.put(TokenType.MINUS, new ParseRule(new UnaryParseFn(), new BinaryParseFn(), Precedence.TERM));
    }

    ParseFn getRule(TokenType type) {
        return rules.get(type);
    }

    boolean compile(String source, Chunk chunk) {
        scanner = new Scanner(source);
        compilingChunk = chunk;

        advance();
        expression();
        consume(TokenType.EOF, "Expect end of expression");
        endCompiler();

        return !hadError;
    }

    void expression() {
        parsePrecedence(Precedence.ASSIGNMENT);
    }

    private void number() {
        emitConstant(previous.getLiteral());
    }

    void parsePrecedence(Precedence precedence) {

    }

    private void advance() {
        previous = current;
        while (true) {
            current = scanner.scanToken();
            if (current.getType() != TokenType.ERROR) {
                break;
            }
            errorAt(current, (String) current.getLiteral());
        }
    }

    private void emitConstant(Object value) {
        int constantIndex = compilingChunk.writeConstant(value);
        if (constantIndex > Chunk.MAX_CONSTANTS_CAPACITY) {
            errorAt(previous, "Too many constants in one chunk");
            constantIndex = 0;
        }
        emitBytes(OpCode.CONSTANT, (byte) constantIndex);
    }

    private void emitReturn() {
        emitByte(OpCode.RETURN);
    }

    void emitByte(byte aByte) {
        compilingChunk.writeCode(aByte, previous.getLine());
    }

    private void emitBytes(byte b1, byte b2) {
        emitByte(b1);
        emitByte(b2);
    }

    void consume(TokenType type, String message) {
        if (current.getType() == type) {
            advance();
            return;
        }
        errorAt(current, message);
    }

    private void endCompiler() {
        emitReturn();
    }

    private void errorAt(Token token, String message) {
        if (panicMode) {
            return;
        }
        panicMode = true;
        System.err.printf("[line %d] Error", token.getLine());
        if (token.getType() == TokenType.EOF) {
            System.err.print(" at end");
        } else if (token.getType() == TokenType.ERROR) {
            // Just do nothing
        } else {
            System.err.printf(" at '%s'", token.getLiteral());
        }
        System.err.printf(": %s\n", message);
        hadError = true;
    }
}
