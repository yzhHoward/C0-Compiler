package InstructionWriter;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class InstructionWriter {
    private ArrayList<String> constants = new ArrayList<>();
    private ArrayList<String> start = new ArrayList<>();
    private ArrayList<String> functions = new ArrayList<>();
    private ArrayList<ArrayList<String>> functionList = new ArrayList<>();
    private ArrayList<String> function;
    private Assembler assembler;

    public InstructionWriter() {
        assembler = new Assembler();
    }

    public InstructionWriter(String outputPath) {
        try {
            System.setOut(new PrintStream(outputPath));
        } catch (IOException e) {
            System.out.println("不能写入文件！");
        }
        assembler = new Assembler();
    }

    public int writeConstants(String string) {
        int index = constants.size();
        constants.add("S \"" + string + '"');
        return index;
    }

    public void writeFunctions(int index, int nameIndex, int sizeOfParameter) {
        functions.add(String.valueOf(index) + ' ' + nameIndex + ' ' + sizeOfParameter + " 1");
    }

    public void newFunction() {
        function = new ArrayList<>();
        functionList.add(function);
    }

    public int getSize() {
        return function.size() - 1;
    }

    public void write(int level, Instructions instructions) {
        if (level == 0) {
            writeStart(instructions);
        } else {
            writeFunction(instructions);
        }
    }

    public void write(int level, Instructions instructions, int x) {
        if (level == 0) {
            writeStart(instructions, x);
        } else {
            writeFunction(instructions, x);
        }
    }

    public void write(int level, Instructions instructions, int x, int y) {
        if (level == 0) {
            writeStart(instructions, x, y);
        } else {
            writeFunction(instructions, x, y);
        }
    }

    public void write(int level, Instructions instructions, String x) {
        if (level == 0) {
            writeStart(instructions, x);
        } else {
            writeFunction(instructions, x);
        }
    }

    public void insert(int index, Instructions instructions, int x) {
        function.add(index, instructions.toString() + ' ' + x);
    }

    private void writeStart(Instructions instructions) {
        start.add(instructions.toString());
    }

    private void writeStart(Instructions instructions, int x) {
        start.add(instructions.toString() + ' ' + x);
    }

    private void writeStart(Instructions instructions, int x, int y) {
        start.add(instructions.toString() + ' ' + x + ' ' + y);
    }

    private void writeStart(Instructions instructions, String x) {
        start.add(instructions.toString() + ' ' + x);
    }

    private void writeFunction(Instructions instructions) {
        function.add(instructions.toString());
    }

    private void writeFunction(Instructions instructions, int x) {
        function.add(instructions.toString() + ' ' + x);
    }

    private void writeFunction(Instructions instructions, int x, int y) {
        function.add(instructions.toString() + ' ' + x + ' ' + y);
    }

    private void writeFunction(Instructions instructions, String x) {
        function.add(instructions.toString() + ' ' + x);
    }

    public void output() {
        System.out.println(".constants");
        for (int i = 0; i < constants.size(); ++i) {
            System.out.print(i);
            System.out.print(' ');
            System.out.println(constants.get(i));
        }
        System.out.println(".start:");
        for (int i = 0; i < start.size(); ++i) {
            System.out.print(i);
            System.out.print('\t');
            System.out.println(start.get(i));
        }
        System.out.println(".functions:");
        for (String string : functions) {
            System.out.println(string);
        }
        for (int i = 0; i < functionList.size(); ++i) {
            ArrayList<String> function = functionList.get(i);
            System.out.print(".F");
            System.out.print(i);
            System.out.println(':');
            for (int j = 0; j < function.size(); ++j) {
                System.out.print(j);
                System.out.print('\t');
                System.out.println(function.get(j));
            }
        }
    }

    public void assemble() {
        assembler.output(constants, start, functions, functionList);
    }
}
