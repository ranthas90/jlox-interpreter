import org.junit.jupiter.api.Test;
import virtualmachine.Lox;

import java.io.IOException;

public class BaseLoxTest {

    void run_test(String path) throws IOException {
        Lox.main(new String[] {"src/main/resources/" + path});
    }
}
