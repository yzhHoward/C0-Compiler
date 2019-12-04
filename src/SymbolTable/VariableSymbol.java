package SymbolTable;

public class VariableSymbol extends Symbol {
    boolean initialized;

    public VariableSymbol(String name, boolean initialized, SymbolType symbolType, DataType dataType,
                          int level, int offset, int lineOffset, int wordOffset) {
        super(name, symbolType, dataType, level, offset, lineOffset, wordOffset);
        this.initialized = initialized;
    }

    public void setInitialized() {
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
