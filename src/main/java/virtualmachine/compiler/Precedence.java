package virtualmachine.compiler;

public class Precedence {
    public static final int NONE = 0;
    public static final int ASSIGNMENT = 1;    // =
    public static final int OR = 2;    // or
    public static final int AND = 3;    // and
    public static final int EQUALITY = 4;    // == !=
    public static final int COMPARISON = 5;    // < > <= >=
    public static final int TERM = 6;    // + -
    public static final int FACTOR = 7;    // * /
    public static final int UNARY = 8;    // ! -
    public static final int CALL = 9;    // . ()
    public static final int PRIMARY = 10;
}
