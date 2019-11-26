package SymbolTable;

public class VariableSymbol extends Symbol {
    boolean isConst;
    String value;

    public VariableSymbol(String name, SymbolType symbolType, DataType dataType,
                          int lineOffset, int wordOffset) {
        super(name, symbolType, dataType, 1, lineOffset, wordOffset);
        this.isConst = false;
    }

    public VariableSymbol(String name, SymbolType symbolType, DataType dataType,
                          int lineOffset, int wordOffset, String value) {
        super(name, symbolType, dataType, 1, lineOffset, wordOffset);
        this.isConst = false;
        this.value = value;
    }

    public VariableSymbol(String name, SymbolType symbolType, DataType dataType,
                          int lineOffset, int wordOffset, boolean isConst, String value) {
        super(name, symbolType, dataType, 1, lineOffset, wordOffset);
        this.isConst = isConst;
        this.value = value;
    }
}
