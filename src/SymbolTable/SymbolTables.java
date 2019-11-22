package SymbolTable;

import java.util.HashMap;
import java.util.LinkedList;

public class SymbolTables {
    public static LinkedList<HashMap<String, VariableSymbol>> variableTables = new LinkedList<>();
    public static HashMap<String, FunctionSymbol> functionTable = new HashMap<>();

    public enum SymbolType {
        Const,
        Int,
        Float,
        IntFunction,
        FloatFunction,
        VoidFunction
    }

    public static boolean insertVariableSymbol(String token, SymbolType type, int level, int offset) {
        HashMap<String, VariableSymbol> symbolTable;
        symbolTable = variableTables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new VariableSymbol(type, level, offset));
            return true;
        }
    }

    public static boolean insertVariableSymbol(String token, SymbolType type, String value, int level, int offset) {
        HashMap<String, VariableSymbol> symbolTable;
        symbolTable = variableTables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new VariableSymbol(type, value, level, offset));
            return true;
        }
    }

    public static void nextLevel() {
        variableTables.add(new HashMap<>());
    }

    public static void prevLevel() {
        variableTables.removeLast();
    }

    public static boolean insertFunctionSymbol(String functionName, SymbolType type, int offset) {
        FunctionSymbol functionSymbol = functionTable.get(functionName);
        if (functionSymbol == null) {
            functionTable.put(functionName, new FunctionSymbol(type, offset));
            insertVariableSymbol(functionName, type, 0, offset);
            return true;
        }
        return false;
    }

    public static LinkedList<VariableSymbol> getGlobals() {
        LinkedList<VariableSymbol> list = new LinkedList<>();
        HashMap<String, VariableSymbol> symbolTable = variableTables.get(1);
        symbolTable.forEach((key, value) -> {
            list.add(value);
        });
        return list;
    }

    public static VariableSymbol findVariableSymbol(String symbolName) {
        HashMap<String, VariableSymbol> symbolTable;
        for (int i = variableTables.size() - 1; i >= 0; --i) {
            symbolTable = variableTables.get(i);
            if (symbolTable.containsKey(symbolName)) {
                return symbolTable.get(symbolName);
            }
        }
        return null;
    }

    public static boolean findVariableSymbol(String symbolName, int level) {
        HashMap<String, VariableSymbol> symbolTable;
        symbolTable = variableTables.get(level);
        return symbolTable.containsKey(symbolName);
    }

    public static boolean isFunction(String name) {
        HashMap<String, VariableSymbol> symbolTable = variableTables.get(1);
        VariableSymbol variableSymbol = symbolTable.get(name);
        if (variableSymbol != null) {
            return variableSymbol.type == SymbolType.IntFunction || variableSymbol.type == SymbolType.FloatFunction ||
                    variableSymbol.type == SymbolType.VoidFunction;
        }
        return false;
    }
}
