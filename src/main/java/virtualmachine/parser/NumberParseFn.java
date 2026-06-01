package virtualmachine.parser;

import virtualmachine.compiler.Compiler;

public class NumberParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        Object value = compiler.getParser().getPrevious().getLexeme();
        compiler.emitConstant(value);
    }
}
