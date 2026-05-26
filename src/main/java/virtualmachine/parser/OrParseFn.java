package virtualmachine.parser;

import virtualmachine.compiler.Compiler;
import virtualmachine.compiler.OpCode;
import virtualmachine.compiler.Precedence;

public class OrParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        int elseJump = compiler.emitJump(OpCode.JUMP_IF_FALSE);
        int endJump = compiler.emitJump(OpCode.JUMP);

        compiler.patchJump(elseJump);
        compiler.emitByte(OpCode.POP);

        compiler.parsePrecedence(Precedence.OR);
        compiler.patchJump(endJump);
    }
}
