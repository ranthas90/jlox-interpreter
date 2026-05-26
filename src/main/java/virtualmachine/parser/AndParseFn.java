package virtualmachine.parser;

import virtualmachine.compiler.Compiler;
import virtualmachine.compiler.OpCode;
import virtualmachine.compiler.Precedence;

public class AndParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        int endJump = compiler.emitJump(OpCode.JUMP_IF_FALSE);
        compiler.emitByte(OpCode.POP);
        compiler.parsePrecedence(Precedence.AND);

        compiler.patchJump(endJump);
    }
}
