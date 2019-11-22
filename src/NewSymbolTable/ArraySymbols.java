package NewSymbolTable;

public class ArraySymbols extends Symbols {
    boolean isConst;
    String[] values;

    public ArraySymbols(String name, SymbolTable.SymbolType symbolType, SymbolTable.DataType dataType,
                        int length, int lineOffset, int wordOffset) {
        super(name, symbolType, dataType, length, lineOffset, wordOffset);
        this.isConst = false;
        this.values = new String[length];
    }

    public ArraySymbols(String name, SymbolTable.SymbolType symbolType, SymbolTable.DataType dataType,
                        int length, int lineOffset, int wordOffset, String[] value) {
        super(name, symbolType, dataType, length, lineOffset, wordOffset);
        this.isConst = false;
        this.values = value;
    }

    public ArraySymbols(String name, SymbolTable.SymbolType symbolType, SymbolTable.DataType dataType,
                        int length, int lineOffset, int wordOffset, boolean isConst, String[] value) {
        super(name, symbolType, dataType, length, lineOffset, wordOffset);
        this.isConst = isConst;
        this.values = value;
    }
}
