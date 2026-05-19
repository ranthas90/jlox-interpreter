package virtualmachine.parser;

import virtualmachine.compiler.Compiler;

public class NumberParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler) {
        Object value = compiler.getParser().getPrevious().getLiteral();
        compiler.emitConstant(value);
    }
}
