import SyntaxAnalyzer.SyntaxAnalyzer;
import WordAnalyzer.WordAnalyzer;
import WordAnalyzer.WordException;
import WordAnalyzer.WordSymbol;

public class Main {

    private static void wordAnalyze(String source) {
        WordAnalyzer wordAnalyzer = new WordAnalyzer(source);
        while (true) {
            try {
                if (wordAnalyzer.getsym() == WordSymbol.Unknown || wordAnalyzer.symbol == WordSymbol.EOF) break;
                System.out.println(String.format("%-16d%-16s%-16s%-16s", wordAnalyzer.count, wordAnalyzer.getType(), wordAnalyzer.symbol, wordAnalyzer.token));
            } catch (WordException e) {
                System.out.println(e.getMessage() + " at " + wordAnalyzer.lineOffset + ":" + (wordAnalyzer.wordOffset - wordAnalyzer.token.length() + 1) + " word: " + wordAnalyzer.token);
                break;
            }
        }
    }

    private static void syntaxAnalyze(String source) {
        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(new WordAnalyzer(source));
        syntaxAnalyzer.start();
    }

    private static void syntaxAnalyze(String source, String outputPath) {
        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(new WordAnalyzer(source), outputPath);
        syntaxAnalyzer.start();
    }

    public static void main(String[] args) {
        String source = "./1.c";
        String outputPath = "./1.txt";
        syntaxAnalyze(source);
//        syntaxAnalyze(source, outputPath);
    }
}
