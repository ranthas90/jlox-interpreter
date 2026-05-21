package virtualmachine.parser;

import virtualmachine.compiler.Compiler;
import virtualmachine.compiler.OpCode;

public class LiteralParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        switch (compiler.getParser().getPrevious().getType()) {
            case FALSE: compiler.emitByte(OpCode.FALSE); break;
            case NIL: compiler.emitByte(OpCode.NIL); break;
            case TRUE: compiler.emitByte(OpCode.TRUE); break;
            default: break;
        }
    }
}
