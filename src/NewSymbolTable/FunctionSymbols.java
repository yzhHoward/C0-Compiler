package NewSymbolTable;

import java.util.HashMap;

public class FunctionSymbols extends Symbols {
    HashMap<String, VariableSymbols> argsMap;

    public FunctionSymbols(String name, SymbolTable.SymbolType symbolType, SymbolTable.DataType dataType,
                           int lineOffset, int wordOffset) {
        super(name, symbolType, dataType, 1, lineOffset, wordOffset);
        this.argsMap = new HashMap<>();
    }

    public boolean addArgs(String token, VariableSymbols variableSymbols) {
        if (argsMap.containsKey(token)) {
            return false;
        }
        argsMap.put(token, variableSymbols);
        return true;
    }
}
