package SyntaxAnalyze;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class PCodeWriter {

    private BufferedWriter writer;
    private int address = 0;

    PCodeWriter(String outputPath) {
        if (outputPath != null) {
            try {
                if(new File(outputPath).isFile()) {
                    throw new IOException("文件已存在，请修改文件名或删除！");
                }
                writer = new BufferedWriter(new FileWriter(outputPath));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    void write(String operator, int x, int y) {
        if (writer != null) {
            try {
                writer.write(operator + " " + x + " " + y);
                address += (operator + " " + x + " " + y).length();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    int getNextAddress() {
        return address;
    }
}
