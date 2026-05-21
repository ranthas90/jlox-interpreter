package virtualmachine.parser;

import virtualmachine.compiler.Compiler;
import virtualmachine.compiler.OpCode;
import virtualmachine.compiler.Precedence;
import virtualmachine.scanner.TokenType;

public class UnaryParseFn implements ParseFn {

    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        TokenType operatorType = compiler.getParser().getPrevious().getType();
        compiler.parsePrecedence(Precedence.UNARY);

        switch (operatorType) {
            case BANG:
                compiler.emitByte(OpCode.NOT);
                break;
            case MINUS:
                compiler.emitByte(OpCode.NEGATE);
                break;
            default:
                break;
        }    }
}
