package org.ranthas.astwalker;

import java.io.IOException;

public class TestRunner {

    private static final String ASSIGNMENT = "assignment";

    public static void main(String[] args) throws IOException {
        runAssignment();
    }

    private static void runAssignment() throws IOException {
        run(ASSIGNMENT, "associativity");
        run(ASSIGNMENT, "global");
        run(ASSIGNMENT, "grouping");
        run(ASSIGNMENT, "infix_operator");
        run(ASSIGNMENT, "local");
        run(ASSIGNMENT, "prefix_operator");
        run(ASSIGNMENT, "syntax");
        run(ASSIGNMENT, "to_this");
        run(ASSIGNMENT, "undefined");
    }

    private static void run(String folder, String file) throws IOException {
        System.out.println("Running " + folder.toUpperCase() + " :: " + file);
        Lox.main(new String[] {String.format("./test/%s/%s.lox", folder, file)});
    }
}
