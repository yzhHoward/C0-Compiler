package SyntaxAnalyze;

import WordAnalyze.WordAnalyzer;

public class SyntaxRead {
    String token;
    WordAnalyzer.Symbols symbolType;
    int lineOffset;
    int wordOffset;

    SyntaxRead(String token, WordAnalyzer.Symbols symbolType, int lineOffset, int wordOffset) {
        this.symbolType = symbolType;
        this.token = token;
        this.lineOffset = lineOffset;
        this.wordOffset = wordOffset;
    }
}
