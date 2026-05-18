package virtualmachine;

class Chunk {

    private byte[] codes;
    private int codesCapacity;
    private int codesCount;

    private Object[] constants;
    private int constantsCapacity;
    private int constantsCount;

    private int[] lines;

    static final int MAX_CONSTANTS_CAPACITY = 256;

    Chunk() {
        codes = new byte[0];
        codesCapacity = 0;
        codesCount = 0;

        constants = new Object[0];
        constantsCapacity = 0;
        constantsCount = 0;

        lines = new int[0];
    }

    int getCodesCount() {
        return codesCount;
    }

     byte getCodeAt(int offset) {
        return codes[offset];
    }

     Object getConstantAt(int offset) {
        return constants[offset];
    }

     int getLineAt(int offset) {
        return lines[offset];
    }

    void writeCode(byte code, int line) {
        if (codesCapacity < codesCount + 1) {
            codesCapacity = codesCapacity < 8 ? 8 : codesCapacity * 2;

            byte[] newCodes = new byte[codesCapacity];
            int[] newLines = new int[codesCapacity];

            System.arraycopy(codes, 0, newCodes, 0, codesCount);
            System.arraycopy(lines, 0, newLines, 0, codesCount);

            codes = newCodes;
            lines = newLines;
        }
        codes[codesCount] = code;
        lines[codesCount] = line;

        codesCount++;
    }

    void free() {
        freeCodes();
        freeConstants();
    }

    void freeCodes() {
        codes = new byte[0];
        codesCapacity = 0;
        codesCount = 0;
    }

    int writeConstant(Object constant) {
        if (constantsCapacity < constantsCount + 1) {
            constantsCapacity = constantsCapacity < 8 ? 8 : constantsCapacity * 2;
            Object[] newConstants = new Object[constantsCapacity];
            System.arraycopy(constants, 0, newConstants, 0, constantsCount);
            constants = newConstants;
        }
        constants[constantsCount] = constant;
        constantsCount++;

        return constantsCount - 1;
    }

    void freeConstants() {
        constants = new Object[0];
        constantsCapacity = 0;
        constantsCount = 0;
    }
}
