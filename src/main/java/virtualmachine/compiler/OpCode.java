package virtualmachine.compiler;

public class OpCode {
    public static final byte CONSTANT =     0x00;
    public static final byte NIL =          0x01;
    public static final byte TRUE =         0x02;
    public static final byte FALSE =        0x03;
    public static final byte EQUAL =        0x04;
    public static final byte GREATER =      0x05;
    public static final byte LESS =         0x06;
    public static final byte ADD =          0x07;
    public static final byte SUBTRACT =     0x08;
    public static final byte MULTIPLY =     0x09;
    public static final byte DIVIDE =       0x10;
    public static final byte NOT =          0x11;
    public static final byte NEGATE =       0x12;
    public static final byte RETURN =       0x13;
}
