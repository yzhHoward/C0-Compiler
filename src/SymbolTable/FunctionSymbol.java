package SymbolTable;

import java.util.LinkedList;

public class FunctionSymbol {
    String name;
    String args[];
    String locals[];
    int address;
    int level;
    boolean returned;
    SymbolTableMap.SymbolType type;
    LinkedList<Integer> offset = new LinkedList<>();

    FunctionSymbol(SymbolTableMap.SymbolType type, int offset) {
        this.type = type;
        this.offset.add(offset);
    }
}
