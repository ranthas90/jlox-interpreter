package virtualmachine;

import virtualmachine.scanner.TokenType;

public class UnaryParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler) {
        TokenType operatorType = compiler.previous().getType();
        compiler.parsePrecedence(Compiler.Precedence.UNARY);

        switch (operatorType) {
            case TokenType.MINUS:
                compiler.emitByte(OpCode.NEGATE);
                break;
            default:
                return;
        }    }
}
