package virtualmachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {

    private static VirtualMachine virtualMachine = new VirtualMachine();

    public static void main(String[] args) throws IOException {
        /*
        Chunk chunk = new Chunk();

        int constantIndex = chunk.writeConstant(1.2);
        chunk.writeCode(OpCode.CONSTANT, 123);
        chunk.writeCode((byte)constantIndex, 123);

        constantIndex = chunk.writeConstant(3.4);
        chunk.writeCode(OpCode.CONSTANT, 123);
        chunk.writeCode((byte)constantIndex, 123);

        chunk.writeCode(OpCode.ADD, 123);

        constantIndex = chunk.writeConstant(5.6);
        chunk.writeCode(OpCode.CONSTANT, 123);
        chunk.writeCode((byte)constantIndex, 123);

        chunk.writeCode(OpCode.DIVIDE, 123);
        chunk.writeCode(OpCode.NEGATE, 123);

        chunk.writeCode(OpCode.RETURN, 123);

        //Debugger debugger = new Debugger();
        //debugger.disassembleChunk(chunk, "test chunk");
        VirtualMachine virtualMachine = new VirtualMachine();
        virtualMachine.interpret(chunk);
         */
        if (args.length > 1) {
            System.err.println("Usage: jlox [path]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            //repl();
            runFile("./src/main/resources/assignment/associativity.lox");
            //runFile("./test/block/empty.lox");
            //runFile("./test/field/get_on_bool.lox");
        }
    }

    private static void repl() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        for (;;) {
            System.out.print("> ");
            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            }
            virtualMachine.interpret(line);
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        virtualMachine.interpret(new String(bytes, Charset.defaultCharset()));
    }
}
