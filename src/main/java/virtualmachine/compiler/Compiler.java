package virtualmachine.compiler;

import virtualmachine.debug.Debugger;
import virtualmachine.parser.*;
import virtualmachine.scanner.Scanner;
import virtualmachine.scanner.Token;
import virtualmachine.scanner.TokenType;

import java.util.HashMap;
import java.util.Map;

import static virtualmachine.compiler.Precedence.*;
import static virtualmachine.scanner.TokenType.*;

public class Compiler {

    private Scanner scanner;
    private Parser parser;
    private Debugger debugger;
    private Chunk compilingChunk;
    private Map<TokenType, ParseRule> rules;

    // *********************************************************************
    // Init

    public Compiler() {
        parser = new Parser();
        debugger = new Debugger();
        initRules();
    }

    private void initRules() {
        rules = new HashMap<>();
        addRule(LEFT_PAREN,     new GroupingParseFn(),  null,                   NONE);
        addRule(RIGHT_PAREN,    null,                   null,                   NONE);
        addRule(LEFT_BRACE,     null,                   null,                   NONE);
        addRule(RIGHT_BRACE,    null,                   null,                   NONE);
        addRule(COMMA,          null,                   null,                   NONE);
        addRule(DOT,            null,                   null,                   NONE);
        addRule(MINUS,          new UnaryParseFn(),     new BinaryParseFn(),    TERM);
        addRule(PLUS,           null,                   new BinaryParseFn(),    TERM);
        addRule(SEMICOLON,      null,                   null,                   NONE);
        addRule(SLASH,          null,                   new BinaryParseFn(),    FACTOR);
        addRule(STAR,           null,                   new BinaryParseFn(),    FACTOR);
        addRule(BANG,           new UnaryParseFn(),     null,                   NONE);
        addRule(BANG_EQUAL,     null,                   new BinaryParseFn(),    EQUALITY);
        addRule(EQUAL,          null,                   null,                   NONE);
        addRule(EQUAL_EQUAL,    null,                   new BinaryParseFn(),    COMPARISON);
        addRule(GREATER,        null,                   new BinaryParseFn(),    COMPARISON);
        addRule(GREATER_EQUAL,  null,                   new BinaryParseFn(),    COMPARISON);
        addRule(LESS,           null,                   new BinaryParseFn(),    COMPARISON);
        addRule(LESS_EQUAL,     null,                   new BinaryParseFn(),    COMPARISON);
        addRule(IDENTIFIER,     null,                   null,                   NONE);
        addRule(STRING,         new StringParseFn(),    null,                   NONE);
        addRule(NUMBER,         new NumberParseFn(),    null,                   NONE);
        addRule(TOKEN_AND,      null,                   null,                   NONE);
        addRule(CLASS,          null,                   null,                   NONE);
        addRule(ELSE,           null,                   null,                   NONE);
        addRule(FALSE,          new LiteralParseFn(),   null,                   NONE);
        addRule(FOR,            null,                   null,                   NONE);
        addRule(FUN,            null,                   null,                   NONE);
        addRule(IF,             null,                   null,                   NONE);
        addRule(NIL,            new LiteralParseFn(),   null,                   NONE);
        addRule(TOKEN_OR,       null,                   null,                   NONE);
        addRule(PRINT,          null,                   null,                   NONE);
        addRule(RETURN,         null,                   null,                   NONE);
        addRule(SUPER,          null,                   null,                   NONE);
        addRule(THIS,           null,                   null,                   NONE);
        addRule(TRUE,           new LiteralParseFn(),   null,                   NONE);
        addRule(VAR,            null,                   null,                   NONE);
        addRule(WHILE,          null,                   null,                   NONE);
        addRule(ERROR,          null,                   null,                   NONE);
        addRule(EOF,            null,                   null,                   NONE);
    }

    private void addRule(TokenType type, ParseFn prefix, ParseFn infix, int precedence) {
        rules.put(type, new ParseRule(prefix, infix, precedence));
    }

    // *********************************************************************
    // Public methods

    public boolean compile(String source, Chunk chunk) {
        scanner = new Scanner(source);
        compilingChunk = chunk;

        advance();
        expression();
        consume(TokenType.EOF, "Expect end of expression");
        endCompiler();

        return !parser.isHadError();
    }

    public Parser getParser() {
        return parser;
    }

    public ParseRule getRule(TokenType type) {
        return rules.get(type);
    }

    public void expression() {
        parsePrecedence(ASSIGNMENT);
        debugger.disassembleChunk(compilingChunk, "chunk test1");
    }

    public void consume(TokenType type, String message) {
        if (parser.getCurrent().getType() == type) {
            advance();
            return;
        }
        errorAt(parser.getCurrent(), message);
    }

    public void parsePrecedence(int precedence) {
        advance();
        ParseFn prefixRule = getRule(parser.getPrevious().getType()).getPrefix();
        if (prefixRule == null) {
            errorAt(parser.getPrevious(), "Expect expression");
        } else {
            prefixRule.parse(this);
            while (precedence <= getRule(parser.getCurrent().getType()).getPrecedence()) {
                advance();
                ParseFn infixRule = getRule(parser.getPrevious().getType()).getInfix();
                infixRule.parse(this);
            }
        }
    }

    public void emitByte(byte aByte) {
        compilingChunk.writeCode(aByte, parser.getPrevious().getLine());
    }

    public void emitBytes(byte b1, byte b2) {
        emitByte(b1);
        emitByte(b2);
    }

    public void emitConstant(Object value) {
        int constantIndex = compilingChunk.writeConstant(value);
        if (constantIndex > Chunk.MAX_CONSTANTS_CAPACITY) {
            errorAt(parser.getPrevious(), "Too many constants in one chunk");
            constantIndex = 0;
        }
        emitBytes(OpCode.CONSTANT, (byte) constantIndex);
    }

    // *********************************************************************
    // Private methods

    private void advance() {
        parser.setPrevious(parser.getCurrent());
        while (true) {
            parser.setCurrent(scanner.scanToken());
            if (parser.getCurrent().getType() != TokenType.ERROR) {
                break;
            }
            errorAt(parser.getCurrent(), (String) parser.getCurrent().getLiteral());
        }
    }

    private void emitReturn() {
        emitByte(OpCode.RETURN);
    }

    private void endCompiler() {
        emitReturn();
        if (!parser.isHadError()) {
            debugger.disassembleChunk(compilingChunk, "code");
        }
    }

    private void errorAt(Token token, String message) {
        if (parser.isPanicMode()) {
            return;
        }
        parser.setPanicMode(true);
        System.err.printf("[line %d] Error", token.getLine());
        if (token.getType() == TokenType.EOF) {
            System.err.print(" at end");
        } else if (token.getType() == TokenType.ERROR) {
            // Just do nothing
        } else {
            System.err.printf(" at '%s'", token.getLiteral());
        }
        System.err.printf(": %s\n", message);
        parser.setHadError(true);
    }
}
