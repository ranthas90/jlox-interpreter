package virtualmachine.compiler;

public class OpCode {
    public static final byte CONSTANT =         0x00;
    public static final byte NIL =              0x01;
    public static final byte TRUE =             0x02;
    public static final byte FALSE =            0x03;
    public static final byte POP =              0x04;
    public static final byte GET_LOCAL =        0x05;
    public static final byte SET_LOCAL =        0x06;
    public static final byte GET_GLOBAL =       0x07;
    public static final byte DEFINE_GLOBAL =    0x08;
    public static final byte SET_GLOBAL =       0x09;
    public static final byte GET_UPVALUE =      0x0A;
    public static final byte SET_UPVALUE =      0x0B;
    public static final byte EQUAL =            0x0C;
    public static final byte GREATER =          0x0D;
    public static final byte LESS =             0x0E;
    public static final byte ADD =              0x0F;
    public static final byte SUBTRACT =         0x10;
    public static final byte MULTIPLY =         0x11;
    public static final byte DIVIDE =           0x12;
    public static final byte NOT =              0x13;
    public static final byte NEGATE =           0x14;
    public static final byte PRINT =            0x15;
    public static final byte JUMP =             0x16;
    public static final byte JUMP_IF_FALSE =    0x17;
    public static final byte LOOP =             0x18;
    public static final byte CALL =             0x19;
    public static final byte CLOSURE =          0x20;
    public static final byte RETURN =           0x21;
}
