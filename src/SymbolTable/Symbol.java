package SymbolTable;

public class Symbol {
    public SymbolType symbolType;
    public DataType dataType;
    String name;
    public int level;
    public int offset; // 在当前层的偏移量
    int lineOffset;
    int wordOffset;

    Symbol(String name, SymbolType symbolType, DataType dataType,
           int level, int offset, int lineOffset, int wordOffset) {
        this.name = name;
        this.symbolType = symbolType;
        this.dataType = dataType;
        this.level = level;
        this.offset = offset;
        this.lineOffset = lineOffset;
        this.wordOffset = wordOffset;
    }
}
