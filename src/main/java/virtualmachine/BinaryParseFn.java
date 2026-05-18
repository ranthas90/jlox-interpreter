package virtualmachine;

import virtualmachine.scanner.TokenType;

public class BinaryParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler) {
        TokenType operatorType = compiler.previous().getType();
        ParseFn rule = compiler.getRule(operatorType);
        compiler.parsePrecedence(rule.get);
        //parsePrecedence((Precedence)(rule->precedence + 1));
        switch (operatorType) {
            case TokenType.PLUS:
                compiler.emitByte(OpCode.ADD);
                break;
            case TokenType.MINUS:
                compiler.emitByte(OpCode.SUBTRACT);
                break;
            case TokenType.STAR:
                compiler.emitByte(OpCode.MULTIPLY);
                break;
            case TokenType.SLASH:
                compiler.emitByte(OpCode.DIVIDE);
                break;
            default:
                return;
        }
    }
}
