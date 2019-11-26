package SymbolTable;

public class Symbol {
    public SymbolType symbolType;
    public DataType dataType;
    String name;
    int length;
    int lineOffset;
    int wordOffset;

    Symbol(String name, SymbolType symbolType, DataType dataType,
           int length, int lineOffset, int wordOffset) {
        this.name = name;
        this.symbolType = symbolType;
        this.dataType = dataType;
        this.length = length;
        this.lineOffset = lineOffset;
        this.wordOffset = wordOffset;
    }
}
