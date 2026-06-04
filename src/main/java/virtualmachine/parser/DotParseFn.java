package virtualmachine.parser;

import virtualmachine.compiler.Compiler;
import virtualmachine.compiler.OpCode;
import virtualmachine.scanner.TokenType;

public class DotParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        compiler.consume(TokenType.IDENTIFIER, "Expect property name after '.'");
        byte name = compiler.identifierConstant(compiler.getParser().getPrevious());

        if (canAssign && compiler.match(TokenType.EQUAL)) {
            compiler.expression();
            compiler.emitBytes(OpCode.SET_PROPERTY, name);
        } else if (compiler.match(TokenType.LEFT_PAREN)) {
            int argCount = compiler.argumentList();
            compiler.emitBytes(OpCode.INVOKE, name);
            compiler.emitByte((byte) argCount);
        } else {
            compiler.emitBytes(OpCode.GET_PROPERTY, name);
        }
    }
}
