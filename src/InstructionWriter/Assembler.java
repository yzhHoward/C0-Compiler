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
            switch (string.charAt(0)) {
                case 'S':
                    bufferedOutputStream.write(0);
                    int length = string.length() - 4;
                    bufferedOutputStream.write(length / 0x100);
                    bufferedOutputStream.write(length % 0x100);
                    bufferedOutputStream.write(string.substring(3, string.length() - 1).getBytes());
                    break;
                case 'I':
                    System.out.println("Error!");
                    bufferedOutputStream.write(1);
                    break;
                case 'D':
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
                    break;
                case "pop":
                    bufferedOutputStream.write(0x04);
                    break;
                case "pop2":
                    bufferedOutputStream.write(0x05);
                    break;
                case "dup":
                    bufferedOutputStream.write(0x07);
                    break;
                case "dup2":
                    bufferedOutputStream.write(0x08);
                    break;
                case "new":
                    bufferedOutputStream.write(0x0b);
                    break;
                case "iload":
                    bufferedOutputStream.write(0x10);
                    break;
                case "dload":
                    bufferedOutputStream.write(0x11);
                    break;
                case "aload":
                    bufferedOutputStream.write(0x12);
                    break;
                case "iaload":
                    bufferedOutputStream.write(0x18);
                    break;
                case "daload":
                    bufferedOutputStream.write(0x19);
                    break;
                case "aaload":
                    bufferedOutputStream.write(0x1a);
                    break;
                case "istore":
                    bufferedOutputStream.write(0x20);
                    break;
                case "dstore":
                    bufferedOutputStream.write(0x21);
                    break;
                case "astore":
                    bufferedOutputStream.write(0x22);
                    break;
                case "iastore":
                    bufferedOutputStream.write(0x28);
                    break;
                case "dastore":
                    bufferedOutputStream.write(0x29);
                    break;
                case "aastore":
                    bufferedOutputStream.write(0x2a);
                    break;
                case "iadd":
                    bufferedOutputStream.write(0x30);
                    break;
                case "dadd":
                    bufferedOutputStream.write(0x31);
                    break;
                case "isub":
                    bufferedOutputStream.write(0x34);
                    break;
                case "dsub":
                    bufferedOutputStream.write(0x35);
                    break;
                case "imul":
                    bufferedOutputStream.write(0x38);
                    break;
                case "dmul":
                    bufferedOutputStream.write(0x39);
                    break;
                case "idiv":
                    bufferedOutputStream.write(0x3c);
                    break;
                case "ddiv":
                    bufferedOutputStream.write(0x3d);
                    break;
                case "ineg":
                    bufferedOutputStream.write(0x40);
                    break;
                case "dneg":
                    bufferedOutputStream.write(0x41);
                    break;
                case "icmp":
                    bufferedOutputStream.write(0x44);
                    break;
                case "dcmp":
                    bufferedOutputStream.write(0x45);
                    break;
                case "i2d":
                    bufferedOutputStream.write(0x60);
                    break;
                case "d2i":
                    bufferedOutputStream.write(0x61);
                    break;
                case "i2c":
                    bufferedOutputStream.write(0x62);
                    break;
                case "ret":
                    bufferedOutputStream.write(0x88);
                    break;
                case "iret":
                    bufferedOutputStream.write(0x89);
                    break;
                case "dret":
                    bufferedOutputStream.write(0x8a);
                    break;
                case "aret":
                    bufferedOutputStream.write(0x8b);
                    break;
                case "iprint":
                    bufferedOutputStream.write(0xa0);
                    break;
                case "dprint":
                    bufferedOutputStream.write(0xa1);
                    break;
                case "cprint":
                    bufferedOutputStream.write(0xa2);
                    break;
                case "sprint":
                    bufferedOutputStream.write(0xa3);
                    break;
                case "printl":
                    bufferedOutputStream.write(0xaf);
                    break;
                case "iscan":
                    bufferedOutputStream.write(0xb0);
                    break;
                case "dscan":
                    bufferedOutputStream.write(0xb1);
                    break;
                case "cscan":
                    bufferedOutputStream.write(0xb2);
                    break;
                default:
                    System.out.println("Instruction not found!");
            }
        } else if (strings.length == 2) {
            switch (strings[0]) {
                case "bipush":
                    bufferedOutputStream.write(0x01);
                    bufferedOutputStream.write(Integer.parseInt(strings[1]));
                    break;
                case "ipush":
                    bufferedOutputStream.write(0x02);
                    write4(strings[1]);
                    break;
                case "pop4":
                    bufferedOutputStream.write(0x06);
                    write4(strings[1]);
                    break;
                case "loadc":
                    bufferedOutputStream.write(0x09);
                    write2(strings[1]);
                    break;
                case "snew":
                    bufferedOutputStream.write(0x0c);
                    write4(strings[1]);
                    break;
                case "jmp":
                    bufferedOutputStream.write(0x70);
                    write2(strings[1]);
                    break;
                case "je":
                    bufferedOutputStream.write(0x71);
                    write2(strings[1]);
                    break;
                case "jne":
                    bufferedOutputStream.write(0x72);
                    write2(strings[1]);
                    break;
                case "jl":
                    bufferedOutputStream.write(0x73);
                    write2(strings[1]);
                    break;
                case "jge":
                    bufferedOutputStream.write(0x74);
                    write2(strings[1]);
                    break;
                case "jg":
                    bufferedOutputStream.write(0x75);
                    write2(strings[1]);
                    break;
                case "jle":
                    bufferedOutputStream.write(0x76);
                    write2(strings[1]);
                    break;
                case "call":
                    bufferedOutputStream.write(0x80);
                    write2(strings[1]);
                    break;
                default:
                    System.out.println("Instruction not found!");
            }
        } else {
            switch (strings[0]) {
                case "loada":
                    bufferedOutputStream.write(0x0a);
                    write2(strings[1]);
                    write4(strings[2]);
                    break;
                default:
                    System.out.println("Instruction not found!");
            }
        }
    }

    private void write2(String string) throws IOException {
        int num;
        if (string.length() > 2 && string.charAt(1) == 'x') {
            num = Integer.parseInt(string.substring(2), 16);
        } else {
            num = Integer.parseInt(string);
        }
        bufferedOutputStream.write(num / 0x100);
        bufferedOutputStream.write(num % 0x100);
    }

    private void write4(String string) throws IOException {
        int num;
        if (string.length() > 2 && string.charAt(1) == 'x') {
            num = Integer.parseInt(string.substring(2), 16);
        } else {
            num = Integer.parseInt(string);
        }
        bufferedOutputStream.write(num / 0x1000000);
        bufferedOutputStream.write(num / 0x10000 % 0x100);
        bufferedOutputStream.write(num / 0x100 % 0x100);
        bufferedOutputStream.write(num % 0x100);
    }
}
