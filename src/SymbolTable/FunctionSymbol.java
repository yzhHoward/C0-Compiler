package SymbolTable;

public class FunctionSymbol extends Symbol {
    String[] args;
    String[] locals;
    int address;
    boolean returned;

    FunctionSymbol(SymbolTables.SymbolType type, int offset) {
        this.type = type;
        this.offset.add(offset);
    }
}
