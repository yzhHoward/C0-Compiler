package SyntaxAnalyzer;

import SymbolTable.DataType;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    private static Map<String, DataType> tokenDataTypeMap =
            new HashMap<String, DataType>() {
                {
                    put("char", DataType.Char);
                    put("int", DataType.Int);
                    put("void", DataType.Void);
                }
            };

    static DataType tokenToDataType(String token) {
        return tokenDataTypeMap.get(token);
    }
}
