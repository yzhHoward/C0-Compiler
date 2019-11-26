package SyntaxAnalyzer;

import WordAnalyzer.WordSymbol;

public class NextWord {
    String token;
    WordSymbol symbolType;
    int lineOffset;
    int wordOffset;

    NextWord(String token, WordSymbol symbolType, int lineOffset, int wordOffset) {
        this.symbolType = symbolType;
        this.token = token;
        this.lineOffset = lineOffset;
        this.wordOffset = wordOffset;
    }
}
