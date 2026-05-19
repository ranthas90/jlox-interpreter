package virtualmachine.parser;

import virtualmachine.compiler.Compiler;
import virtualmachine.scanner.TokenType;

public class GroupingParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler) {
        compiler.expression();
        compiler.consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");
    }
}
