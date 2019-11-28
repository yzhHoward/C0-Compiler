package InstructionWriter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Assembler {

    private BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(System.out);

    public void output(ArrayList<String> constants, ArrayList<String> start, ArrayList<String> functions, ArrayList<ArrayList<String>> functionList) {
        try {
            init();
            constants(constants);
            start(start);
            functions(functions, functionList);
            bufferedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        byte[] magic = {0x43, 0x30, 0x3A, 0x29};
        byte[] version = {0, 0, 0, 1};
        bufferedOutputStream.write(magic);
        bufferedOutputStream.write(version);
    }

    private void constants(ArrayList<String> constants) throws IOException {
        int size = constants.size();
        bufferedOutputStream.write(size / 0x100);
        bufferedOutputStream.write(size % 0x100);
        for (String string : constants) {
            String[] strings = string.split(" ");
            switch (strings[1]) {
                case "S":
                    bufferedOutputStream.write(0);
                    int length = strings[2].length() - 2;
                    bufferedOutputStream.write(length / 0x100);
                    bufferedOutputStream.write(length % 0x100);
                    bufferedOutputStream.write(strings[2].substring(1, strings[2].length() - 1).getBytes());
                    break;
                case "I":
                    System.out.println("Error!");
                    bufferedOutputStream.write(1);
                    break;
                case "D":
                    System.out.println("Error!");
                    bufferedOutputStream.write(2);
                    break;
                default:
                    System.out.println("Error!");
                    break;
            }
        }
    }

    private void start(ArrayList<String> start) throws IOException {
        int size = start.size();
        bufferedOutputStream.write(size / 0x100);
        bufferedOutputStream.write(size % 0x100);
        for (String string : start) {
            instructions(string);
        }
    }

    private void functions(ArrayList<String> functions, ArrayList<ArrayList<String>> functionList) throws IOException {
        int functionsSize = functions.size();
        bufferedOutputStream.write(functionsSize / 0x100);
        bufferedOutputStream.write(functionsSize % 0x100);
        for (int i = 0; i < functions.size(); ++i) {
            String string = functions.get(i);
            String[] strings = string.split(" ");
            for (int j = 1; j < 4; ++j) {
                bufferedOutputStream.write(Integer.parseInt(strings[j]) / 0x100);
                bufferedOutputStream.write(Integer.parseInt(strings[j]) % 0x100);
            }
            ArrayList<String> function = functionList.get(i);
            int size = function.size();
            bufferedOutputStream.write(size / 0x100);
            bufferedOutputStream.write(size % 0x100);
            for (String s : function) {
                instructions(s);
            }
        }
    }

    private void instructions(String string) throws IOException {
        String[] strings = string.split(" ");
        if (strings.length == 1) {
            switch (strings[0]) {
                case "nop":
                    bufferedOutputStream.write(0x00);
                case "pop":
                    bufferedOutputStream.write(0x04);
                case "pop2":
                    bufferedOutputStream.write(0x05);
                case "dup":
                    bufferedOutputStream.write(0x07);
                case "dup2":
                    bufferedOutputStream.write(0x08);
                case "new":
                    bufferedOutputStream.write(0x0b);
                case "iload":
                    bufferedOutputStream.write(0x10);
                case "dload":
                    bufferedOutputStream.write(0x11);
                case "aload":
                    bufferedOutputStream.write(0x12);
                case "iaload":
                    bufferedOutputStream.write(0x18);
                case "daload":
                    bufferedOutputStream.write(0x19);
                case "aaload":
                    bufferedOutputStream.write(0x1a);
                case "istore":
                    bufferedOutputStream.write(0x20);
                case "dstore":
                    bufferedOutputStream.write(0x21);
                case "astore":
                    bufferedOutputStream.write(0x22);
                case "iastore":
                    bufferedOutputStream.write(0x28);
                case "dastore":
                    bufferedOutputStream.write(0x29);
                case "aastore":
                    bufferedOutputStream.write(0x2a);
                case "iadd":
                    bufferedOutputStream.write(0x30);
                case "dadd":
                    bufferedOutputStream.write(0x31);
                case "isub":
                    bufferedOutputStream.write(0x34);
                case "dsub":
                    bufferedOutputStream.write(0x35);
                case "imul":
                    bufferedOutputStream.write(0x38);
                case "dmul":
                    bufferedOutputStream.write(0x39);
                case "idiv":
                    bufferedOutputStream.write(0x3c);
                case "ddiv":
                    bufferedOutputStream.write(0x3d);
                case "ineg":
                    bufferedOutputStream.write(0x40);
                case "dneg":
                    bufferedOutputStream.write(0x41);
                case "icmp":
                    bufferedOutputStream.write(0x44);
                case "dcmp":
                    bufferedOutputStream.write(0x45);
                case "i2d":
                    bufferedOutputStream.write(0x60);
                case "d2i":
                    bufferedOutputStream.write(0x61);
                case "i2c":
                    bufferedOutputStream.write(0x62);
                case "ret":
                    bufferedOutputStream.write(0x88);
                case "iret":
                    bufferedOutputStream.write(0x89);
                case "dret":
                    bufferedOutputStream.write(0x8a);
                case "aret":
                    bufferedOutputStream.write(0x8b);
                case "iprint":
                    bufferedOutputStream.write(0xa0);
                case "dprint":
                    bufferedOutputStream.write(0xa1);
                case "cprint":
                    bufferedOutputStream.write(0xa2);
                case "sprint":
                    bufferedOutputStream.write(0xa3);
                case "printl":
                    bufferedOutputStream.write(0xaf);
                case "iscan":
                    bufferedOutputStream.write(0xb0);
                case "dscan":
                    bufferedOutputStream.write(0xb1);
                case "cscan":
                    bufferedOutputStream.write(0xb2);
                default:
                    System.out.println("Instruction not found!");
            }
        } else if (strings.length == 2) {
            switch (strings[0]) {
                case "bipush":
                    bufferedOutputStream.write(0x01);
                    bufferedOutputStream.write(Integer.parseInt(strings[1]));
                case "ipush":
                    bufferedOutputStream.write(0x02);
                    write4(strings[1]);
                case "pop4":
                    bufferedOutputStream.write(0x06);
                    write4(strings[1]);
                case "loadc":
                    bufferedOutputStream.write(0x09);
                    write2(strings[1]);
                case "snew":
                    bufferedOutputStream.write(0x0c);
                    write4(strings[1]);
                case "jmp":
                    bufferedOutputStream.write(0x02);
                    write2(strings[1]);
                case "je":
                    bufferedOutputStream.write(0x02);
                    write2(strings[1]);
                case "jne":
                    bufferedOutputStream.write(0x02);
                    write2(strings[1]);
                case "jl":
                    bufferedOutputStream.write(0x02);
                    write2(strings[1]);
                case "jge":
                    bufferedOutputStream.write(0x02);
                    write2(strings[1]);
                case "jg":
                    bufferedOutputStream.write(0x02);
                    write2(strings[1]);
                case "jle":
                    bufferedOutputStream.write(0x02);
                    write2(strings[1]);
                case "call":
                    bufferedOutputStream.write(0x02);
                    write2(strings[1]);
                default:
                    System.out.println("Instruction not found!");
            }
        } else {
            switch (strings[0]) {
                case "loada":
                    bufferedOutputStream.write(0x0a);
                    write2(strings[1]);
                    write4(strings[2]);
                default:
                    System.out.println("Instruction not found!");
            }
        }
    }

    private void write2(String string) throws IOException {
        int num = Integer.parseInt(string);
        bufferedOutputStream.write(num / 0x100);
        bufferedOutputStream.write(num % 0x100);
    }

    private void write4(String string) throws IOException {
        int num = Integer.parseInt(string);
        bufferedOutputStream.write(num / 0x1000000);
        bufferedOutputStream.write(num / 0x10000 % 0x100);
        bufferedOutputStream.write(num / 0x100 % 0x100);
        bufferedOutputStream.write(num % 0x100);
    }
}
