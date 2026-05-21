package virtualmachine.parser;

import virtualmachine.compiler.Compiler;

public interface ParseFn {

    void parse(Compiler compiler, boolean canAssign);
}
