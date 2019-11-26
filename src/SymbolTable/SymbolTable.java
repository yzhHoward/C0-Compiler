package SymbolTable;

import java.util.HashMap;
import java.util.LinkedList;

public class SymbolTable {
    public static LinkedList<HashMap<String, Symbol>> tables = new LinkedList<>();

    public static void nextLevel() {
        tables.add(new HashMap<>());
    }

    public static void prevLevel() {
        tables.removeLast();
    }

    public static boolean insertVariableSymbol(int level, String token, SymbolType symbolType, DataType dataType,
                                               int lineOffset, int wordOffset) {
        HashMap<String, Symbol> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new VariableSymbol(token, symbolType, dataType, lineOffset, wordOffset));
            return true;
        }
    }

    public static boolean insertVariableSymbol(int level, String token, SymbolType symbolType, DataType dataType,
                                               int lineOffset, int wordOffset, String value) {
        HashMap<String, Symbol> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new VariableSymbol(token, symbolType, dataType, lineOffset, wordOffset, value));
            return true;
        }
    }

    public static boolean insertVariableSymbol(int level, String token, SymbolType symbolType, DataType dataType,
                                               int lineOffset, int wordOffset, boolean isConst, String value) {
        HashMap<String, Symbol> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new VariableSymbol(token, symbolType, dataType, lineOffset, wordOffset, isConst, value));
            return true;
        }
    }

    public static boolean insertFunctionSymbol(String token, SymbolType symbolType, DataType dataType,
                                               int lineOffset, int wordOffset) {
        int level = 1;
        HashMap<String, Symbol> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new FunctionSymbol(token, symbolType, dataType, lineOffset, wordOffset));
            return true;
        }
    }

    public static boolean updateVariableSymbol(String symbolName, String value) {
        HashMap<String, Symbol> symbolTable;
        for (int i = tables.size() - 1; i >= 0; --i) {
            symbolTable = tables.get(i);
            if (symbolTable.containsKey(symbolName)) {
                Symbol symbol = symbolTable.get(symbolName);
                if (symbol instanceof VariableSymbol) {
                    VariableSymbol variableSymbol = (VariableSymbol) symbol;
                    if ((variableSymbol.symbolType == SymbolType.Variable || variableSymbol.symbolType == SymbolType.Param) &&
                            !variableSymbol.isConst) {
                        switch (variableSymbol.dataType) {
                            case SignedChar:
                                if (value.length() == 1) {
                                    variableSymbol.value = value;
                                    return true;
                                }
                            case SignedInt:
                                variableSymbol.value = String.valueOf(Integer.parseInt(value));
                        }
                        variableSymbol.value = value;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean updateFunctionSymbol(String functionName, String token, DataType dataType,
                                               int lineOffset, int wordOffset) {
        HashMap<String, Symbol> symbolTable;
        FunctionSymbol functionSymbol;
        symbolTable = tables.get(1);
        if (!symbolTable.containsKey(functionName)) {
            return false;
        }
        Symbol symbol = symbolTable.get(functionName);
        if (symbol instanceof FunctionSymbol) {
            functionSymbol = (FunctionSymbol) symbol;
        } else {
            return false;
        }
        return functionSymbol.addArgs(token, new VariableSymbol(token, SymbolType.Param, dataType, lineOffset, wordOffset));
    }

    public static VariableSymbol findVariableSymbol(String symbolName) {
        HashMap<String, Symbol> symbolTable;
        for (int i = tables.size() - 1; i >= 0; --i) {
            symbolTable = tables.get(i);
            if (symbolTable.containsKey(symbolName)) {
                Symbol symbol = symbolTable.get(symbolName);
                if (symbol instanceof VariableSymbol) {
                    return (VariableSymbol) symbol;
                }
            }
        }
        return null;
    }

    public static VariableSymbol findVariableSymbol(String symbolName, int level) {
        HashMap<String, Symbol> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(symbolName)) {
            Symbol symbol = symbolTable.get(symbolName);
            if (symbol instanceof VariableSymbol) {
                return (VariableSymbol) symbol;
            }
        }
        return null;
    }

    public static FunctionSymbol findFunctionSymbol(String symbolName) {
        HashMap<String, Symbol> symbolTable;
        for (int i = tables.size() - 1; i >= 0; --i) {
            symbolTable = tables.get(i);
            if (symbolTable.containsKey(symbolName)) {
                Symbol symbol = symbolTable.get(symbolName);
                if (symbol instanceof FunctionSymbol) {
                    return (FunctionSymbol) symbol;
                }
            }
        }
        return null;
    }

    public static FunctionSymbol findFunctionSymbol(String symbolName, int level) {
        HashMap<String, Symbol> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(symbolName)) {
            Symbol symbol = symbolTable.get(symbolName);
            if (symbol instanceof FunctionSymbol) {
                return (FunctionSymbol) symbol;
            }
        }
        return null;
    }
}
