package NewSyntaxAnalyzer;

import NewSymbolTable.SymbolTable;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    private static Map<String, SymbolTable.DataType> tokenDataTypeMap =
            new HashMap<String, SymbolTable.DataType>() {
                {
                    put("char", SymbolTable.DataType.SignedChar);
                    put("int", SymbolTable.DataType.SignedInt);
                    put("float", SymbolTable.DataType.SignedFloat);
                    put("double", SymbolTable.DataType.SignedDouble);
                    put("void", SymbolTable.DataType.Void);
                }
            };

    static SymbolTable.DataType tokenToDataType(String token) {
        return tokenDataTypeMap.get(token);
    }
}
