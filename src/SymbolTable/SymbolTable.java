package SymbolTable;

import SyntaxAnalyzer.SyntaxError;
import SyntaxAnalyzer.SyntaxException;

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

    public static void insertVariableSymbol(int level, String token, boolean initialized, SymbolType symbolType, DataType dataType,
                                            int offset, int lineOffset, int wordOffset) throws SyntaxException {
        HashMap<String, Symbol> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            throw new SyntaxException(SyntaxError.DuplicateSymbol);
        } else {
            symbolTable.put(token, new VariableSymbol(token, initialized, symbolType, dataType, offset, lineOffset, wordOffset));
        }
    }

    public static void insertFunctionSymbol(String token, SymbolType symbolType, DataType dataType,
                                            int offset, int lineOffset, int wordOffset) throws SyntaxException {
        int level = 1;
        HashMap<String, Symbol> symbolTable;
        symbolTable = tables.get(level);
        if (symbolTable.containsKey(token)) {
            throw new SyntaxException(SyntaxError.DuplicateSymbol);
        } else {
            symbolTable.put(token, new FunctionSymbol(token, symbolType, dataType, offset, lineOffset, wordOffset));
        }
    }

    public static void updateFunctionSymbol(String functionName, String token, SymbolType symbolType, DataType dataType,
                                            int offset, int lineOffset, int wordOffset) throws SyntaxException {
        HashMap<String, Symbol> symbolTable;
        FunctionSymbol functionSymbol;
        symbolTable = tables.get(1);
        if (!symbolTable.containsKey(functionName)) {
            throw new SyntaxException(SyntaxError.SymbolNotFound);
        }
        Symbol symbol = symbolTable.get(functionName);
        if (symbol instanceof FunctionSymbol) {
            functionSymbol = (FunctionSymbol) symbol;
        } else {
            throw new SyntaxException(SyntaxError.UnknownError);
        }
        functionSymbol.addArgs(token, new VariableSymbol(token, true, symbolType, dataType, offset, lineOffset, wordOffset));
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
