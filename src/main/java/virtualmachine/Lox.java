package virtualmachine;

import virtualmachine.vm.VirtualMachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {

    private static VirtualMachine virtualMachine = new VirtualMachine();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.err.println("Usage: jlox [path]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            repl();
        }
    }

    private static void repl() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        boolean keepRunning = true;
        String line = "";

        while (true) {
            System.out.print("> ");
            String current = bufferedReader.readLine();
            if (current == null || current.isBlank()) {
                break;
            }
            line = line + current;
        }

        VirtualMachine.InterpretResult result = virtualMachine.interpret(line);
        keepRunning = result == VirtualMachine.InterpretResult.INTERPRET_OK;
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        virtualMachine.interpret(new String(bytes, Charset.defaultCharset()));
    }
}
