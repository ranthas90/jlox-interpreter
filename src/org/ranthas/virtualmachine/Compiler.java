package org.ranthas.virtualmachine;

public class Compiler {

    public void compile(String source) {
        int line = -1;
        Scanner scanner = new Scanner(source);

        while(true) {
            Token token = scanner.scanToken();
            if (token != null) {
                if (token.getLine() != line) {
                    System.out.printf("%4d ", token.getLine());
                    line = token.getLine();
                } else {
                    System.out.print("   | ");
                }
                System.out.printf("%s\n", token);
                if (token.getType() == TokenType.EOF) {
                    break;
                }
            }
        }
    }
}
