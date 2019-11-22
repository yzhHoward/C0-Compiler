import SyntaxAnalyze.SyntaxAnalyzer;
import WordAnalyzer.WordAnalyzer;

import java.io.IOException;

public class Main {

    private static void wordAnalyze(String source) {
        try {
            WordAnalyzer wordAnalyzer = new WordAnalyzer(source);
            while (wordAnalyzer.getsym() != WordAnalyzer.Symbols.Unknown && wordAnalyzer.symbol != WordAnalyzer.Symbols.EOF) {
                if (wordAnalyzer.symbol == WordAnalyzer.Symbols.UnsignedInt ||
                        wordAnalyzer.symbol == WordAnalyzer.Symbols.UnsignedFloat) {
                    System.out.println(String.format("%-16d%-16s%-16s%-16s", wordAnalyzer.count, wordAnalyzer.getType(), wordAnalyzer.symbol, wordAnalyzer.num));
                } else {
                    System.out.println(String.format("%-16d%-16s%-16s%-16s", wordAnalyzer.count, wordAnalyzer.getType(), wordAnalyzer.symbol, wordAnalyzer.token));
                }
                if (wordAnalyzer.error != null) {
                    System.out.println("Error:" + wordAnalyzer.error);
                    break;
                }
                //System.out.println(wordAnalyzer.token);
            }
            if (wordAnalyzer.error != null) {
                System.out.println("Error: " + wordAnalyzer.error);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void syntaxAnalyze(String source, String outputPath) {
        try {
            SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(new WordAnalyzer(source), outputPath);
            syntaxAnalyzer.start();
            if (syntaxAnalyzer.error != null) {
                System.out.println(syntaxAnalyzer.error + " at " + syntaxAnalyzer.lineOffset + ":" + syntaxAnalyzer.wordOffset + " word: " + syntaxAnalyzer.token);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String source = "./1.c";
        String outputPath = "./1.txt";
        syntaxAnalyze(source, outputPath);
    }
}
