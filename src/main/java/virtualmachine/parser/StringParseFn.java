package virtualmachine.parser;

import virtualmachine.compiler.Compiler;

public class StringParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler, boolean canAssign) {
        // TODO: el libro crea una copia del String en lugar de usar directamente el valor apuntado por el scanner
        compiler.emitConstant(compiler.getParser().getPrevious().getLexeme());
    }
}
