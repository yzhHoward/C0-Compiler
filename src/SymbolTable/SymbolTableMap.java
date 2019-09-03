package SymbolTable;

import java.util.HashMap;
import java.util.LinkedList;

public class SymbolTableMap {
    public static LinkedList<HashMap<String, Symbol>> symbolTables = new LinkedList<>();
    private static HashMap<String, FunctionSymbol> functionTable = new HashMap<>();

    public enum SymbolType {
        Const,
        Int,
        Float,
        IntFunction,
        FloatFunction,
        VoidFunction
    }

    public static boolean insertSymbol(String token, SymbolType type, int level, int offset) {
        //if (functionName == null) {
        HashMap<String, Symbol> symbolTable;
        symbolTable = symbolTables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new Symbol(type, level, offset));
            return true;
        }
        //}
        /*
        else {
            HashMap<String, SymbolTable.FunctionSymbol> hashMap = functionSymbolTable.computeIfAbsent(functionName, k -> new HashMap<>());
            if (symbolStackTable.containsKey(token)) {
                return false;
            } else {
                hashMap.put(token, new SymbolTable.FunctionSymbol(type, offset));
                return true;
            }
        }*/
    }

    public static void nextLevel() {
        symbolTables.add(new HashMap<>());
    }

    public static void prevLevel() {
        symbolTables.removeLast();
    }

    public static boolean createFunction(String functionName, SymbolType type, int offset) {
        FunctionSymbol functionSymbol = functionTable.get(functionName);
        if (functionSymbol == null) {
            functionTable.put(functionName, new FunctionSymbol(type, offset));
            insertSymbol(functionName, type, 0, offset);
            return true;
        }
        return false;
    }

    public static Symbol findSymbol(String symbolName) {
        HashMap<String, Symbol> symbolTable;
        for (int i = symbolTables.size() - 1; i >= 0; --i) {
            symbolTable = symbolTables.get(i);
            if (symbolTable.containsKey(symbolName)) {
                return symbolTable.get(symbolName);
            }
        }
        return null;
    }

    public static boolean findSymbol(String symbolName, int level) {
        HashMap<String, Symbol> symbolTable;
        symbolTable = symbolTables.get(level);
        return symbolTable.containsKey(symbolName);
    }

    public static boolean isFunction(String name) {
        HashMap<String, Symbol> symbolTable = symbolTables.get(1);
        Symbol symbol = symbolTable.get(name);
        if (symbol != null) {
            return symbol.type == SymbolType.IntFunction || symbol.type == SymbolType.FloatFunction ||
                    symbol.type == SymbolType.VoidFunction;
        }
        return false;
    }
}
