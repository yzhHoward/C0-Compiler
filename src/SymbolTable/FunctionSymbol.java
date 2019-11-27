package SymbolTable;

import SyntaxAnalyzer.SyntaxError;
import SyntaxAnalyzer.SyntaxException;

import java.util.HashMap;

public class FunctionSymbol extends Symbol {
    HashMap<String, VariableSymbol> argsMap;

    public FunctionSymbol(String name, SymbolType symbolType, DataType dataType,
                          int offset, int lineOffset, int wordOffset) {
        super(name, symbolType, dataType, offset, lineOffset, wordOffset);
        this.argsMap = new HashMap<>();
    }

    public boolean addArgs(String token, VariableSymbol variableSymbols) throws SyntaxException {
        if (argsMap.containsKey(token)) {
            throw new SyntaxException(SyntaxError.DuplicateSymbol);
        }
        argsMap.put(token, variableSymbols);
        return true;
    }
}
