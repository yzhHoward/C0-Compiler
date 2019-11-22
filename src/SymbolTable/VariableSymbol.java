package SymbolTable;

public class VariableSymbol extends Symbol {
    String value;
    int level;

    VariableSymbol(SymbolTables.SymbolType type, int level, int offset) {
        this.type = type;
        this.level = level;
        this.offset.add(offset);
    }

    VariableSymbol(SymbolTables.SymbolType type, String value, int level, int offset) {
        this.type = type;
        this.value = value;
        this.level = level;
        this.offset.add(offset);
    }
}
