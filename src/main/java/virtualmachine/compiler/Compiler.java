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
    private Locals currentLocals;
    private Debugger debugger;
    private Chunk compilingChunk;
    private Map<TokenType, ParseRule> rules;

    // *********************************************************************
    // Init

    public Compiler() {
        parser = new Parser();
        currentLocals = new Locals();
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
        addRule(IDENTIFIER,     new VariableParserFn(), null,                   NONE);
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

        while(!match(TokenType.EOF)) {
            declaration();
        }

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
    }

    public void declaration() {
        if (match(VAR)) {
            varDeclaration();
        } else {
            statement();
        }

        if (parser.isPanicMode()) {
            synchronize();
        }
    }

    public void varDeclaration() {
        byte global = parseVariable("Expect variable name");

        if (match(EQUAL)) {
            expression();
        } else {
            emitByte(OpCode.NIL);
        }

        consume(SEMICOLON, "Expect ';' after variable declaration");
        defineVariable(global);
    }

    public void statement() {
        if (match(PRINT)) {
            printStatement();
        } else if (match(IF)) {
            ifStatement();
        } else if (match(LEFT_BRACE)) {
            beginScope();
            block();
            endScope();
        } else {
            expressionStatement();
        }
    }

    public void block() {
        while(!check(RIGHT_BRACE) && !check(EOF)) {
            declaration();
        }
        consume(RIGHT_BRACE, "Expect '}' after block");
    }

    public void expressionStatement() {
        expression();
        consume(SEMICOLON, "Expect ';' after expression");
        emitByte(OpCode.POP);
    }

    public void ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'");
        expression();
        consume(RIGHT_PAREN, "Expect ')' after condition");

        int thenJump = emitJump(OpCode.JUMP_IF_FALSE);
        statement();

        patchJump(thenJump);
    }

    public void printStatement() {
        expression();
        consume(SEMICOLON, "Expect ';' after value");
        emitByte(OpCode.PRINT);
    }

    public void namedVariable(Token name, boolean canAssign) {
        byte getOperation, setOperation;
        int arg = resolveLocal(name);

        if (arg != -1) {
            getOperation = OpCode.GET_LOCAL;
            setOperation = OpCode.SET_LOCAL;
        } else {
            arg = identifierConstant(name);
            getOperation = OpCode.SET_GLOBAL;
            setOperation = OpCode.SET_GLOBAL;
        }

        if (canAssign && match(EQUAL)) {
            expression();
            emitBytes(setOperation, (byte) arg);
        } else {
            emitBytes(getOperation, (byte) arg);
        }
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
            return;
        }

        boolean canAssign = precedence <= ASSIGNMENT;
        prefixRule.parse(this, canAssign);
        while (precedence <= getRule(parser.getCurrent().getType()).getPrecedence()) {
            advance();
            ParseFn infixRule = getRule(parser.getPrevious().getType()).getInfix();
            infixRule.parse(this, canAssign);
        }

        if (canAssign && match(EQUAL)) {
            errorAt(parser.getCurrent(), "Invalid assignment target");
        }
    }

    public byte parseVariable(String errorMessage) {
        consume(IDENTIFIER, errorMessage);
        declareVariable();

        if (currentLocals.getScopeDepth() > 0) {
            return 0x0;
        }

        return identifierConstant(parser.getPrevious());
    }

    public void markInitialized() {
        currentLocals.markInitialized();
    }

    public void defineVariable(byte global) {
        if (currentLocals.getScopeDepth() > 0) {
            markInitialized();
            return;
        }

        emitBytes(OpCode.DEFINE_GLOBAL, global);
    }

    public void emitByte(byte aByte) {
        compilingChunk.writeCode(aByte, parser.getPrevious().getLine());
    }

    public void emitBytes(byte b1, byte b2) {
        emitByte(b1);
        emitByte(b2);
    }

    public int emitJump(byte instruction) {
        emitByte(instruction);
        emitByte((byte) 0xFF);
        emitByte((byte) 0xFF);

        return compilingChunk.getCodesCount() - 2;
    }

    public void patchJump(int offset) {
        // emitJump returns current byte with -2 offset!
        int jump = compilingChunk.getCodesCount() - offset - 2;
        if (jump > 65535) {
            errorAt(parser.getCurrent(), "Too much code to jump over");
        }

        compilingChunk.setCodeAt(offset, ((byte) ((jump >> 8) & 0xFF)));
        compilingChunk.setCodeAt(offset + 1, ((byte) (jump & 0xFF)));
    }

    public byte makeConstant(Object value) {
        int constantIndex = compilingChunk.writeConstant(value);
        if (constantIndex > Chunk.MAX_CONSTANTS_CAPACITY) {
            errorAt(parser.getPrevious(), "Too many constants in one chunk");
            constantIndex = 0;
        }
        return (byte)constantIndex;
    }

    public void emitConstant(Object value) {
        emitBytes(OpCode.CONSTANT, makeConstant(value));
    }

    // *********************************************************************
    // Private methods

    private void beginScope() {
        currentLocals.incrementScopeDepth();
    }

    private void endScope() {
        currentLocals.decrementScopeDepth();
        while (currentLocals.getLocalCount() > 0 &&
                currentLocals.getAt(currentLocals.getLocalCount() - 1).getDepth() > currentLocals.getScopeDepth()) {
            emitByte(OpCode.POP);
            currentLocals.decrementLocalCount();
        }
    }

    private void advance() {
        parser.setPrevious(parser.getCurrent());
        while (true) {
            Token token = scanner.scanToken();
            parser.setCurrent(token);
            if (parser.getCurrent().getType() != TokenType.ERROR) {
                break;
            }
            errorAt(parser.getCurrent(), (String) parser.getCurrent().getLiteral());
        }
    }

    private boolean match(TokenType type) {
        if (!check(type)) {
            return false;
        }
        advance();
        return true;
    }

    private boolean check(TokenType type) {
        return parser.getCurrent().getType() == type;
    }

    private byte identifierConstant(Token name) {
        return makeConstant(name.getLiteral());
    }

    private boolean identifiersEqual(Token a, Token b) {
        return a.getLiteral().equals(b.getLiteral()); // TODO: mejorar esto!!!!
    }

    private int resolveLocal(Token name) {
        for (int i = currentLocals.getLocalCount() - 1; i >= 0; i--) {
            Local local = currentLocals.getAt(i);
            if (identifiersEqual(name, local.getName())) {
                if (local.getDepth() == -1) {
                    errorAt(parser.getCurrent(), "Can't read local variable in its own initializer");
                }
                return i;
            }
        }
        return -1;
    }

    private void declareVariable() {
        if (currentLocals.getScopeDepth() > 0) {
            return;
        }

        Token name = parser.getPrevious();
        for (int i = currentLocals.getLocalCount() - 1; i >= 0; i--) {
            Local local = currentLocals.getLocals()[i];
            if (local.getDepth() != -1 && local.getDepth() < currentLocals.getScopeDepth()) {
                break;
            }

            if (identifiersEqual(name, local.getName())) {
                errorAt(parser.getCurrent(), "Already a variable with this name in this scope");
            }
        }

        addLocal(name);
    }

    private void addLocal(Token name) {
        if (currentLocals.getLocalCount() == (Integer.MAX_VALUE -1)) {
            errorAt(parser.getCurrent(), "Too many local variables in function");
            return;
        }

        Local local = currentLocals.getLocals()[currentLocals.getLocalCount() + 1];
        local.setName(name);
        local.setDepth(-1);
    }

    // Skip tokens until we reach a statement boundary (semicolon).
    private void synchronize() {
        parser.setPanicMode(false);
        while (parser.getCurrent().getType() != EOF) {
            if (parser.getPrevious().getType() == SEMICOLON) {
                switch (parser.getCurrent().getType()) {
                    case CLASS,FUN,VAR,FOR,IF,WHILE,PRINT,RETURN: return;
                    default:;
                }
                advance();
            }
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
