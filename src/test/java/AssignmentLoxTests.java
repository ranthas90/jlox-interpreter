import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AssignmentLoxTests extends BaseLoxTest {

    private void run(String name) throws IOException {
        super.run_test("assignment/" + name + ".lox");
    }

    @Test
    void associativity() throws IOException {
        run("associativity");
    }

    @Test
    void global() throws IOException {
        run("global");
    }

    @Test
    void grouping() throws IOException {
        run("grouping");
    }

    @Test
    void infix_operator() throws IOException {
        run("infix_operator");
    }

    @Test
    void local() throws IOException {
        run("local");
    }

    @Test
    void prefix_operator() throws IOException {
        run("prefix_operator");
    }

    @Test
    void syntax() throws IOException {
        run("syntax");
    }

    @Test
    void to_this() throws IOException {
        run("to_this");
    }

    @Test
    void undefined() throws IOException {
        run("undefined");
    }
}