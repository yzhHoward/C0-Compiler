package SymbolTable;

import java.util.LinkedList;

public class Symbol {
    String name;
    int level;
    public SymbolTables.SymbolType type;
    LinkedList<Integer> offset = new LinkedList<>();
}
