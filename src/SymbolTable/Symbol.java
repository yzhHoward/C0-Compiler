package SymbolTable;

import java.util.LinkedList;

public class Symbol {
    String name;
    int value;
    int level;
    public SymbolTableMap.SymbolType type;
    LinkedList<Integer> offset = new LinkedList<>();

    Symbol(SymbolTableMap.SymbolType type, int level, int offset) {
        this.type = type;
        this.level = level;
        this.offset.add(offset);
    }
}
