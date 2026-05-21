package virtualmachine.parser;

import virtualmachine.compiler.Compiler;
import virtualmachine.compiler.OpCode;
import virtualmachine.scanner.TokenType;

public class BinaryParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        TokenType operatorType = compiler.getParser().getPrevious().getType();
        ParseRule rule = compiler.getRule(operatorType);
        compiler.parsePrecedence(rule.getPrecedence() + 1);

        switch (operatorType) {
            case BANG_EQUAL -> compiler.emitBytes(OpCode.EQUAL, OpCode.NOT);
            case EQUAL_EQUAL -> compiler.emitByte(OpCode.EQUAL);
            case GREATER -> compiler.emitByte(OpCode.GREATER);
            case GREATER_EQUAL -> compiler.emitBytes(OpCode.LESS, OpCode.NOT);
            case LESS -> compiler.emitByte(OpCode.LESS);
            case LESS_EQUAL -> compiler.emitBytes(OpCode.GREATER, OpCode.NOT);
            case PLUS -> compiler.emitByte(OpCode.ADD);
            case MINUS -> compiler.emitByte(OpCode.SUBTRACT);
            case STAR -> compiler.emitByte(OpCode.MULTIPLY);
            case SLASH -> compiler.emitByte(OpCode.DIVIDE);
        }
    }
}
