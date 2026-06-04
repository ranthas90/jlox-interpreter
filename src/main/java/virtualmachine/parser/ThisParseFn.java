package virtualmachine.parser;

import virtualmachine.compiler.Compiler;

public class ThisParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        if (compiler.getCurrentClass() == null) {
            compiler.errorAt(compiler.getParser().getPrevious(), "Can't use 'this' outside of a class");
            return;
        }
        new VariableParserFn().parse(compiler, false);
    }
}
