package astwalker;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScannerTest {

    private List<Token> current;

    @Test
    void assignment_associativity() throws IOException {

        String source = new String(Files.readAllBytes(Paths.get("src/main/resources/assignment/associativity.lox")));
        Scanner scanner = new Scanner(source);
        current = scanner.scanTokens();

        assertNotNull(current);
        assertEquals(31, current.size());
        assertEquals(TokenType.VAR, tokenType(0));
        assertEquals(TokenType.IDENTIFIER, tokenType(1));
        assertEquals("a", lexeme(1));
        assertEquals(TokenType.EQUAL, tokenType(2));
        assertEquals(TokenType.STRING, tokenType(3));
        assertEquals("\"a\"", lexeme(3));
        assertEquals(TokenType.SEMICOLON, tokenType(4));

        assertEquals(TokenType.VAR, tokenType(5));
        assertEquals("b", lexeme(6));
        assertEquals(TokenType.IDENTIFIER, tokenType(6));
        assertEquals(TokenType.EQUAL, tokenType(7));
        assertEquals(TokenType.STRING, tokenType(8));
        assertEquals("\"b\"", lexeme(8));
        assertEquals(TokenType.SEMICOLON, tokenType(9));

        assertEquals(TokenType.VAR, tokenType(10));
        assertEquals(TokenType.IDENTIFIER, tokenType(11));
        assertEquals("c", lexeme(11));
        assertEquals(TokenType.EQUAL, tokenType(12));
        assertEquals(TokenType.STRING, tokenType(13));
        assertEquals("\"c\"", lexeme(13));
        assertEquals(TokenType.SEMICOLON, tokenType(14));

        assertEquals(TokenType.IDENTIFIER, tokenType(15));
        assertEquals(TokenType.EQUAL, tokenType(16));
        assertEquals(TokenType.IDENTIFIER, tokenType(17));
        assertEquals(TokenType.EQUAL, tokenType(18));
        assertEquals(TokenType.IDENTIFIER, tokenType(19));
        assertEquals(TokenType.SEMICOLON, tokenType(20));

        assertEquals(TokenType.PRINT, tokenType(21));
        assertEquals(TokenType.IDENTIFIER, tokenType(22));
        assertEquals(TokenType.SEMICOLON, tokenType(23));

        assertEquals(TokenType.PRINT, tokenType(24));
        assertEquals(TokenType.IDENTIFIER, tokenType(25));
        assertEquals(TokenType.SEMICOLON, tokenType(26));

        assertEquals(TokenType.PRINT, tokenType(27));
        assertEquals(TokenType.IDENTIFIER, tokenType(28));
        assertEquals(TokenType.SEMICOLON, tokenType(29));

        assertEquals(TokenType.EOF, tokenType(30));
    }

    private TokenType tokenType(int index) {
        return current.get(index).type;
    }

    private String lexeme(int index) {
        return current.get(index).lexeme;
    }
}