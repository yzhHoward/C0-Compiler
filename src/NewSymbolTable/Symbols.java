package NewSymbolTable;

public class Symbols {
    public SymbolTable.SymbolType symbolType;
    public SymbolTable.DataType dataType;
    String name;
    int length;
    int lineOffset;
    int wordOffset;

    Symbols(String name, SymbolTable.SymbolType symbolType, SymbolTable.DataType dataType,
            int length, int lineOffset, int wordOffset) {
        this.name = name;
        this.symbolType = symbolType;
        this.dataType = dataType;
        this.length = length;
        this.lineOffset = lineOffset;
        this.wordOffset = wordOffset;
    }
}
