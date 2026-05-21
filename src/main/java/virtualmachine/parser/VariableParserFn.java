package virtualmachine.parser;

import virtualmachine.compiler.Compiler;

public class VariableParserFn implements ParseFn {
    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        compiler.namedVariable(compiler.getParser().getPrevious(), canAssign);
    }
}
