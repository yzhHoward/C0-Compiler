package SymbolTable;

import SyntaxAnalyzer.SyntaxError;
import SyntaxAnalyzer.SyntaxException;

import java.util.ArrayList;
import java.util.HashMap;

public class FunctionSymbol extends Symbol {
    private HashMap<String, VariableSymbol> argsMap;
    private ArrayList<VariableSymbol> args;

    public FunctionSymbol(String name, SymbolType symbolType, DataType dataType,
                          int level, int offset, int lineOffset, int wordOffset) {
        super(name, symbolType, dataType, level, offset, lineOffset, wordOffset);
        this.argsMap = new HashMap<>();
        this.args = new ArrayList<>();
    }

    public void addArgs(String token, VariableSymbol variableSymbol) throws SyntaxException {
        if (argsMap.containsKey(token)) {
            throw new SyntaxException(SyntaxError.DuplicateSymbol);
        }
        argsMap.put(token, variableSymbol);
        args.add(variableSymbol);
    }

    public DataType getArgDataTypeByIndex(int index) {
        if (index >= getArgsSize()) {
            return null;
        }
        return args.get(index).dataType;
    }

    public int getArgsSize() {
        return args.size();
    }
}
