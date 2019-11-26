package SymbolTable;

import java.util.HashMap;

public class FunctionSymbol extends Symbol {
    HashMap<String, VariableSymbol> argsMap;

    public FunctionSymbol(String name, SymbolType symbolType, DataType dataType,
                          int lineOffset, int wordOffset) {
        super(name, symbolType, dataType, 1, lineOffset, wordOffset);
        this.argsMap = new HashMap<>();
    }

    public boolean addArgs(String token, VariableSymbol variableSymbols) {
        if (argsMap.containsKey(token)) {
            return false;
        }
        argsMap.put(token, variableSymbols);
        return true;
    }
}
