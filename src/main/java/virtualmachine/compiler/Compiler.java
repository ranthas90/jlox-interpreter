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
    private LocalVarsEnvironment currentLocals;
    private Debugger debugger;
    private Map<TokenType, ParseRule> rules;

    // *********************************************************************
    // Init

    public Compiler() {
        parser = new Parser();
        debugger = new Debugger();
        currentLocals = new LocalVarsEnvironment(null, FunctionType.SCRIPT, null);
        initRules();
    }

    private void initRules() {
        rules = new HashMap<>();
        addRule(LEFT_PAREN,     new GroupingParseFn(),  new CallParseFn(),      CALL);
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
        addRule(TOKEN_AND,      null,                   new AndParseFn(),       AND);
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

    public Function compile(String source) {
        scanner = new Scanner(source);
        advance();

        while(!match(TokenType.EOF)) {
            declaration();
        }

        Function function = endCompiler();
        return parser.isHadError() ? null : function;
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
        if (match(FUN)) {
            funDeclaration();
        }else if (match(VAR)) {
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
        } else if (match(FOR)) {
            forStatement();
        } else if (match(IF)) {
            ifStatement();
        } else if (match(RETURN)) {
            returnStatement();
        } else if (match(WHILE)) {
            whileStatement();
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

    public void function(FunctionType type) {

        // Inicializa un contexto para las variables locales de la función
        // Luego hace que el contexto actual sea el nuevo que hemos creado
        LocalVarsEnvironment functionLocals = new LocalVarsEnvironment(currentLocals, type, (String) parser.getPrevious().getLexeme());
        currentLocals = functionLocals;

        beginScope();

        consume(LEFT_PAREN, "Expect '(' after function name");
        if (!check(RIGHT_PAREN)) {
            do {
                functionLocals.getFunction().incrementArity();
                if (functionLocals.getFunction().getArity() > 255) {
                    errorAt(parser.getCurrent(), "Can't have more than 255 parameters"); // Este errorAt esta bien, cambiar el resto por previous
                }
                byte constant = parseVariable("Expect parameter name");
                defineVariable(constant);
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expect ')' after parameters");
        consume(LEFT_BRACE, "Expect '{' before function body");
        block();

        Function function = endCompiler();
        emitBytes(OpCode.CLOSURE, makeConstant(function));

        for (int i = 0; i < function.getUpvalueCount(); i++) {
            emitByte(functionLocals.getUpvalueAt(i).isLocal() ? (byte) 1 : (byte) 0);
            emitByte(functionLocals.getUpvalueAt(i).getIndex());
        }
    }

    public void funDeclaration() {
        byte global = parseVariable("Expect function name");
        markInitialized();
        function(FunctionType.FUNCTION);
        defineVariable(global);
    }

    public void expressionStatement() {
        expression();
        consume(SEMICOLON, "Expect ';' after expression");
        emitByte(OpCode.POP);
    }

    public void forStatement() {
        beginScope();
        consume(LEFT_PAREN, "Expect '(' after 'for'");
        if (match(SEMICOLON)) {
            // No initializer
        } else if (match(VAR)) {
            varDeclaration();
        } else {
            expressionStatement();
        }

        int loopStart = currentChunk().getCodesCount();
        int exitJump = -1;
        if (!match(SEMICOLON)) {
            expression();
            consume(SEMICOLON, "Expect ';' after loop condition");

            // Jump out of the loop if the condition is false
            exitJump = emitJump(OpCode.JUMP_IF_FALSE);
            emitByte(OpCode.POP);
        }

        consume(SEMICOLON, "Expect ';'");

        if (!match(RIGHT_PAREN)) {
            int bodyJump = emitJump(OpCode.JUMP);
            int incrementStart = currentChunk().getCodesCount();
            expression();
            emitByte(OpCode.POP);
            consume(RIGHT_PAREN, "Expect ')' after for clauses");

            emitLoop(loopStart);
            loopStart = incrementStart;
            patchJump(bodyJump);
        }

        statement();
        emitLoop(loopStart);

        if (exitJump != -1) {
            patchJump(exitJump);
            emitByte(OpCode.POP);
        }

        endScope();
    }

    public void ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'");
        expression();
        consume(RIGHT_PAREN, "Expect ')' after condition");

        int thenJump = emitJump(OpCode.JUMP_IF_FALSE);
        emitByte(OpCode.POP);
        statement();

        int elseJump = emitJump(OpCode.JUMP);
        patchJump(thenJump);
        emitByte(OpCode.POP);

        if (match(ELSE)) {
            statement();
        }

        patchJump(elseJump);
    }

    public void printStatement() {
        expression();
        consume(SEMICOLON, "Expect ';' after value");
        emitByte(OpCode.PRINT);
    }

    public void returnStatement() {
        if (currentLocals.getType() == FunctionType.SCRIPT) {
            errorAt(parser.getPrevious(), "Can't return from top-level code");
        }

        if (match(SEMICOLON)) {
            emitReturn();
        } else {
            expression();
            consume(SEMICOLON, "Expect ';' after return value");
            emitByte(OpCode.RETURN);
        }
    }

    public void whileStatement() {
        int loopStart = currentChunk().getCodesCount();
        consume(LEFT_PAREN, "Expect '(' after 'while'");
        expression();
        consume(RIGHT_PAREN, "Expect ')' after condition");

        int exitJump = emitJump(OpCode.JUMP_IF_FALSE);
        emitByte(OpCode.POP);
        statement();
        emitLoop(loopStart);

        patchJump(exitJump);
        emitByte(OpCode.POP);
    }

    public void namedVariable(Token name, boolean canAssign) {
        byte getOperation, setOperation;
        int arg = resolveLocal(currentLocals, name);

        if (arg != -1) {
            getOperation = OpCode.GET_LOCAL;
            setOperation = OpCode.SET_LOCAL;
        } else if ((arg = resolveUpvalue(currentLocals, name)) != -1) {
            getOperation = OpCode.GET_UPVALUE;
            setOperation = OpCode.SET_UPVALUE;
        } else {
            arg = identifierConstant(name);
            getOperation = OpCode.GET_GLOBAL;
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
        errorAt(parser.getCurrent(), message); // TODO: este error_at está correcto
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
            return (byte) 0;
        }

        return identifierConstant(parser.getPrevious());
    }

    public void markInitialized() {
        if (currentLocals.getScopeDepth() == 0) {
            return;
        }
        currentLocals.markInitialized();
    }

    public void defineVariable(byte global) {
        if (currentLocals.getScopeDepth() > 0) {
            markInitialized();
            return;
        }

        emitBytes(OpCode.DEFINE_GLOBAL, global);
    }

    public int argumentList() {
        int argCount = 0;
        if (!check(RIGHT_PAREN)) {
            do {
                expression();
                if (argCount == 255) {
                    errorAt(parser.getPrevious(), "Can't have more than 255 arguments");
                }
                argCount++;
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after arguments");
        return argCount;
    }

    public void emitByte(byte aByte) {
        currentChunk().writeCode(aByte, parser.getPrevious().getLine());
    }

    public void emitBytes(byte b1, byte b2) {
        emitByte(b1);
        emitByte(b2);
    }

    public void emitLoop(int loopStart) {
        emitByte(OpCode.LOOP);

        int offset = currentChunk().getCodesCount() - loopStart + 2;
        if (offset > 65535) {
            errorAt(parser.getCurrent(), "Loop body too large");
        }

        emitByte((byte) ((offset >> 8) & 0xFF));
        emitByte((byte) (offset & 0xFF));
    }

    public int emitJump(byte instruction) {
        emitByte(instruction);
        emitByte((byte) 0xFF);
        emitByte((byte) 0xFF);

        return currentChunk().getCodesCount() - 2;
    }

    public void patchJump(int offset) {
        // emitJump returns current byte with -2 offset!
        int jump = currentChunk().getCodesCount() - offset - 2;
        if (jump > 65535) {
            errorAt(parser.getCurrent(), "Too much code to jump over");
        }

        currentChunk().setCodeAt(offset, ((byte) ((jump >> 8) & 0xFF)));
        currentChunk().setCodeAt(offset + 1, ((byte) (jump & 0xFF)));
    }

    public byte makeConstant(Object value) {
        int constantIndex = currentChunk().writeConstant(value);
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
            if (currentLocals.getLocals()[currentLocals.getLocalCount() - 1].isCaptured()) {
                emitByte(OpCode.CLOSE_UPVALUE);
            } else {
                emitByte(OpCode.POP);
            }
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
            errorAt(parser.getCurrent(), (String) parser.getCurrent().getLexeme());
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
        return makeConstant(name.getLexeme());
    }

    private boolean identifiersEqual(Token a, Token b) {
        return a.getLexeme().equals(b.getLexeme()); // TODO: mejorar esto!!!!
    }

    private int resolveLocal(LocalVarsEnvironment localVarsEnvironment, Token name) {
        for (int i = localVarsEnvironment.getLocalCount() - 1; i >= 0; i--) {
            LocalVar localVar = localVarsEnvironment.getAt(i);
            if (identifiersEqual(name, localVar.getName())) {
                if (localVar.getDepth() == -1) {
                    errorAt(parser.getCurrent(), "Can't read local variable in its own initializer");
                }
                return i;
            }
        }
        return -1;
    }

    private int addUpvalue(LocalVarsEnvironment localVarsEnvironment, byte index, boolean isLocal) {
        int upvalueCount = localVarsEnvironment.getFunction().getUpvalueCount();

        for (int i = 0; i < upvalueCount; i++) {
            Upvalue upvalue = localVarsEnvironment.getUpvalueAt(i);
            if (upvalue.getIndex() == index && upvalue.isLocal() == isLocal) {
                return i;
            }
        }

        if (upvalueCount == 127) { // TODO: UINT8_COUNT
            errorAt(parser.getPrevious(), "Too many closure variables in function");
            return 0;
        }

        return localVarsEnvironment.addUpvalue(new Upvalue(index, isLocal));
    }

    private int resolveUpvalue(LocalVarsEnvironment localVarsEnvironment, Token name) {
        if (localVarsEnvironment.getEnclosing() == null) {
            return -1;
        }

        int local = resolveLocal(localVarsEnvironment.getEnclosing(), name);
        if (local != -1) {
            localVarsEnvironment.getEnclosing().getLocals()[local].setCaptured(true);
            return addUpvalue(localVarsEnvironment, (byte) local, true);
        }

        int upvalue = resolveUpvalue(localVarsEnvironment.getEnclosing(), name);
        if (upvalue != -1) {
            return addUpvalue(localVarsEnvironment, (byte) upvalue, false);
        }

        return -1;
    }

    private void declareVariable() {
        if (currentLocals.getScopeDepth() == 0) {
            return;
        }

        Token name = parser.getPrevious();
        for (int i = currentLocals.getLocalCount() - 1; i >= 0; i--) {
            LocalVar localVar = currentLocals.getLocals()[i];
            if (localVar.getDepth() != -1 && localVar.getDepth() < currentLocals.getScopeDepth()) {
                break;
            }

            if (identifiersEqual(name, localVar.getName())) {
                errorAt(parser.getCurrent(), "Already a variable with this name in this scope");
            }
        }

        addLocal(name);
    }

    private void addLocal(Token name) {
        if (currentLocals.getLocalCount() >= 127) { // TODO: Este 127 es UINT8_COUNT
            errorAt(parser.getCurrent(), "Too many local variables in function");
            return;
        }

        LocalVar localVar = new LocalVar(name, currentLocals.getScopeDepth(), false);
        currentLocals.addLocal(localVar);
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
        emitByte(OpCode.NIL);
        emitByte(OpCode.RETURN);
    }

    private Function endCompiler() {
        emitReturn();

        // Extracts the current function
        Function function = currentLocals.getFunction();

        if (!parser.isHadError()) {
            debugger.disassembleChunk(function.getChunk(), function.getName() != null ? function.getName() : "<script>");
        }

        // Restores previous locals context
        currentLocals = currentLocals.getEnclosing();

        return function;
    }

    private Chunk currentChunk() {
        return currentLocals.getFunction().getChunk();
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
            System.err.printf(" at '%s'", token.getLexeme());
        }
        System.err.printf(": %s\n", message);
        parser.setHadError(true);
    }
}