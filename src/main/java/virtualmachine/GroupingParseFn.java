package virtualmachine;

import virtualmachine.scanner.TokenType;

public class GroupingParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler) {
        compiler.expression();
        compiler.consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");
    }
}
