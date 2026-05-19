package virtualmachine.scanner;

import java.util.HashMap;
import java.util.Map;

import static virtualmachine.scanner.TokenType.*;

public class Scanner {

    private int start;
    private int current;
    private int line;

    private final String source;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TOKEN_AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", TOKEN_OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    public Scanner(String source) {
        this.start = 0;
        this.current = 0;
        this.line = 1;
        this.source = source;
    }

    public Token scanToken() {
        skipWhitespaces();
        start = current;

        if (isAtEnd()) {
            return token(EOF);
        }

        char c = advance();
        return switch (c) {
            case '(' -> token(LEFT_PAREN);
            case ')' -> token(RIGHT_PAREN);
            case '{' -> token(LEFT_BRACE);
            case '}' -> token(RIGHT_BRACE);
            case ';' -> token(SEMICOLON);
            case ',' -> token(COMMA);
            case '.' -> token(DOT);
            case '-' -> token(MINUS);
            case '+' -> token(PLUS);
            case '/' -> token(SLASH);
            case '*' -> token(STAR);
            case '!' -> token(match('=') ? BANG_EQUAL : BANG);
            case '=' -> token(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> token(match('=') ? LESS_EQUAL : LESS);
            case '>' -> token(match('=') ? GREATER_EQUAL : GREATER);
            case '"' -> string();
            default -> {
                if (isDigit(c)) {
                    yield number();
                } else if (isAlpha(c)) {
                    yield identifier();
                } else {
                    yield errorToken("Unexpected character");
                }
            }
        };
    }

    private Token token(TokenType type) {
        return new Token(type, null, line);
    }

    private Token token(TokenType type, Object literal) {
        return new Token(type, literal, line);
    }

    private Token errorToken(String message) {
        return new Token(ERROR, message, line);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private Token identifier() {

        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        if (type == null) {
            return token(IDENTIFIER, text);
        }

        return token(type);
    }

    private Token number() {
        while (isDigit(peek())) {
            advance();
        }

        // Parte fraccionaria, es un punto seguido de al menos, otro número
        if (peek() == '.' && isDigit(peekNext())) {
            do {
                advance();
            } while (isDigit(peek()));
        }

        Double value = Double.parseDouble(source.substring(start, current));
        return token(NUMBER, value);
    }

    private Token string() {
        // Mientras el siguiente caracter no sea una comilla doble y no lleguemos al final del fichero, seguimos
        // consumiendo caracteres.
        // En el caso concreto de escanear un salto de línea, incrementamos el contador de líneas.
        // Si llegamos al final sin encontrar una comilla doble, generamos un error de string sin terminación.
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            return errorToken("Unterminated string");
        }

        // Consume la comilla doble que finaliza la string
        advance();

        // Extraemos el valor contenido entre las comillas dobles
        String value = source.substring(start + 1, current - 1);
        return token(STRING, value);
    }

    private void skipWhitespaces() {
        while (true) {
            char c = peek();
            switch (c) {
                case ' ', '\r', '\t':
                    advance();
                    break;
                case '\n':
                    line++;
                    advance();
                    break;
                case '/':
                    if (peekNext() == '/') {
                        while (peek() != '\n' && !isAtEnd()) {
                            advance();
                        }
                    } else {
                        return;
                    }
                    break;
                default:
                    return;
            }
        }
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }

        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
