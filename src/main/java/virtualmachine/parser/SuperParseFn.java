package virtualmachine.parser;

import virtualmachine.compiler.Compiler;
import virtualmachine.compiler.OpCode;
import virtualmachine.scanner.Token;
import virtualmachine.scanner.TokenType;

public class SuperParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        if (compiler.getCurrentClass() == null) {
            compiler.errorAt(compiler.getParser().getPrevious(), "Can't use 'super' outside a class");
        } else if (!compiler.getCurrentClass().isHasSuperclass()) {
            compiler.errorAt(compiler.getParser().getPrevious(), "Can't use 'super' in a class with no superclass");
        }

        compiler.consume(TokenType.DOT, "Expect '.' after 'super'");
        compiler.consume(TokenType.IDENTIFIER, "Expect superclass method name");
        byte name = compiler.identifierConstant(compiler.getParser().getPrevious());

        compiler.namedVariable(new Token(null, "this", -1), false);
        if (compiler.match(TokenType.LEFT_PAREN)) {
            int argCount = compiler.argumentList();
            compiler.namedVariable(new Token(null, "super", -1), false);
            compiler.emitBytes(OpCode.SUPER_INVOKE, name);
            compiler.emitByte((byte) argCount);
        } else {
            compiler.namedVariable(new Token(null, "super", -1), false);
            compiler.emitBytes(OpCode.GET_SUPER, name);
        }
    }
}
