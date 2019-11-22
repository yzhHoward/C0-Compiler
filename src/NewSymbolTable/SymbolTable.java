package NewSymbolTable;

import java.util.HashMap;
import java.util.LinkedList;

public class SymbolTable {
    public static LinkedList<HashMap<String, Symbols>> tables = new LinkedList<>();

    public static void nextLevel() {
        tables.add(new HashMap<>());
    }

    public static void prevLevel() {
        tables.removeLast();
    }

    public static boolean insertVariableSymbol(int level, String token, SymbolType symbolType, DataType dataType,
                                               int lineOffset, int wordOffset) {
        HashMap<String, Symbols> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new VariableSymbols(token, symbolType, dataType, lineOffset, wordOffset));
            return true;
        }
    }

    public static boolean insertVariableSymbol(int level, String token, SymbolType symbolType, DataType dataType,
                                               int lineOffset, int wordOffset, String value) {
        HashMap<String, Symbols> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new VariableSymbols(token, symbolType, dataType, lineOffset, wordOffset, value));
            return true;
        }
    }

    public static boolean insertVariableSymbol(int level, String token, SymbolType symbolType, DataType dataType,
                                               int lineOffset, int wordOffset, boolean isConst, String value) {
        HashMap<String, Symbols> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new VariableSymbols(token, symbolType, dataType, lineOffset, wordOffset, isConst, value));
            return true;
        }
    }

    public static boolean insertArraySymbol(int level, String token, SymbolType symbolType, DataType dataType,
                                            int length, int lineOffset, int wordOffset) {
        HashMap<String, Symbols> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new ArraySymbols(token, symbolType, dataType, length, lineOffset, wordOffset));
            return true;
        }
    }

    public static boolean insertArraySymbol(int level, String token, SymbolType symbolType, DataType dataType,
                                            int length, int lineOffset, int wordOffset, String[] values) {
        HashMap<String, Symbols> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new ArraySymbols(token, symbolType, dataType, length, lineOffset, wordOffset, values));
            return true;
        }
    }

    public static boolean insertArraySymbol(int level, String token, SymbolType symbolType, DataType dataType,
                                            int length, int lineOffset, int wordOffset, boolean isConst, String[] values) {
        HashMap<String, Symbols> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new ArraySymbols(token, symbolType, dataType, length, lineOffset, wordOffset, isConst, values));
            return true;
        }
    }

    public static boolean insertFunctionSymbol(String token, SymbolType symbolType, DataType dataType,
                                               int lineOffset, int wordOffset) {
        int level = 1;
        HashMap<String, Symbols> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            return false;
        } else {
            symbolTable.put(token, new FunctionSymbols(token, symbolType, dataType, lineOffset, wordOffset));
            return true;
        }
    }

    public static boolean updateVariableSymbol(String symbolName, String value) {
        HashMap<String, Symbols> symbolTable;
        for (int i = tables.size() - 1; i >= 0; --i) {
            symbolTable = tables.get(i);
            if (symbolTable.containsKey(symbolName)) {
                Symbols symbol = symbolTable.get(symbolName);
                if (symbol instanceof VariableSymbols) {
                    VariableSymbols variableSymbol = (VariableSymbols) symbol;
                    if ((variableSymbol.symbolType == SymbolType.Variable || variableSymbol.symbolType == SymbolType.Param) &&
                            !variableSymbol.isConst) {
                        switch (variableSymbol.dataType) {
                            case SignedChar:
                            case UnsignedChar:
                                if (value.length() == 1) {
                                    variableSymbol.value = value;
                                    return true;
                                }
                            case SignedInt:
                            case UnsignedInt:
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
        HashMap<String, Symbols> symbolTable;
        FunctionSymbols functionSymbol;
        symbolTable = tables.get(1);
        if (!symbolTable.containsKey(functionName)) {
            return false;
        }
        Symbols symbol = symbolTable.get(functionName);
        if (symbol instanceof FunctionSymbols) {
            functionSymbol = (FunctionSymbols) symbol;
        } else {
            return false;
        }
        return functionSymbol.addArgs(token, new VariableSymbols(token, SymbolType.Param, dataType, lineOffset, wordOffset));
    }

    public static VariableSymbols findVariableSymbol(String symbolName) {
        HashMap<String, Symbols> symbolTable;
        for (int i = tables.size() - 1; i >= 0; --i) {
            symbolTable = tables.get(i);
            if (symbolTable.containsKey(symbolName)) {
                Symbols symbol = symbolTable.get(symbolName);
                if (symbol instanceof VariableSymbols) {
                    return (VariableSymbols) symbol;
                }
            }
        }
        return null;
    }

    public static VariableSymbols findVariableSymbol(String symbolName, int level) {
        HashMap<String, Symbols> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(symbolName)) {
            Symbols symbol = symbolTable.get(symbolName);
            if (symbol instanceof VariableSymbols) {
                return (VariableSymbols) symbol;
            }
        }
        return null;
    }

    public static FunctionSymbols findFunctionSymbol(String symbolName) {
        HashMap<String, Symbols> symbolTable;
        for (int i = tables.size() - 1; i >= 0; --i) {
            symbolTable = tables.get(i);
            if (symbolTable.containsKey(symbolName)) {
                Symbols symbol = symbolTable.get(symbolName);
                if (symbol instanceof FunctionSymbols) {
                    return (FunctionSymbols) symbol;
                }
            }
        }
        return null;
    }

    public static FunctionSymbols findFunctionSymbol(String symbolName, int level) {
        HashMap<String, Symbols> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(symbolName)) {
            Symbols symbol = symbolTable.get(symbolName);
            if (symbol instanceof FunctionSymbols) {
                return (FunctionSymbols) symbol;
            }
        }
        return null;
    }

    public enum SymbolType {
        Variable,
        Function,
        Param
    }

    public enum DataType {
        SignedChar, UnsignedChar,
        SignedInt, UnsignedInt,
        SignedFloat, UnsignedFloat,
        SignedDouble, UnsignedDouble,
        Void,
        Array
    }
}
