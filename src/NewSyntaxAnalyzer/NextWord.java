package NewSyntaxAnalyzer;

import WordAnalyzer.WordAnalyzer;

public class NextWord {
    String token;
    WordAnalyzer.Symbols symbolType;
    int lineOffset;
    int wordOffset;

    NextWord(String token, WordAnalyzer.Symbols symbolType, int lineOffset, int wordOffset) {
        this.symbolType = symbolType;
        this.token = token;
        this.lineOffset = lineOffset;
        this.wordOffset = wordOffset;
    }
}
