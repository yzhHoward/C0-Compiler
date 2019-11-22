package NewSymbolTable;

public class VariableSymbols extends Symbols {
    boolean isConst;
    String value;

    public VariableSymbols(String name, SymbolTable.SymbolType symbolType, SymbolTable.DataType dataType,
                           int lineOffset, int wordOffset) {
        super(name, symbolType, dataType, 1, lineOffset, wordOffset);
        this.isConst = false;
    }

    public VariableSymbols(String name, SymbolTable.SymbolType symbolType, SymbolTable.DataType dataType,
                           int lineOffset, int wordOffset, String value) {
        super(name, symbolType, dataType, 1, lineOffset, wordOffset);
        this.isConst = false;
        this.value = value;
    }

    public VariableSymbols(String name, SymbolTable.SymbolType symbolType, SymbolTable.DataType dataType,
                           int lineOffset, int wordOffset, boolean isConst, String value) {
        super(name, symbolType, dataType, 1, lineOffset, wordOffset);
        this.isConst = isConst;
        this.value = value;
    }
}
