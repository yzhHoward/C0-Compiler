package SymbolTable;

public class VariableSymbol extends Symbol {
    boolean initialized;

    public VariableSymbol(String name, boolean initialized, SymbolType symbolType, DataType dataType,
                          int offset, int lineOffset, int wordOffset) {
        super(name, symbolType, dataType, offset, lineOffset, wordOffset);
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
