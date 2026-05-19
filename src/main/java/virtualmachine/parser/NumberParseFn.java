package virtualmachine.parser;

import virtualmachine.compiler.Compiler;
import virtualmachine.compiler.DoubleValue;

public class NumberParseFn implements ParseFn {
    @Override
    public void parse(Compiler compiler) {
        Object value = compiler.getParser().getPrevious().getLiteral();
        Double doubleValue = new DoubleValue(value).getValue();
        compiler.emitConstant(doubleValue);
    }
}
