package virtualmachine.parser;

import virtualmachine.compiler.Compiler;
import virtualmachine.compiler.OpCode;

public class CallParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        int argCount = compiler.argumentList();
        compiler.emitBytes(OpCode.CALL, (byte) argCount);
    }
}
