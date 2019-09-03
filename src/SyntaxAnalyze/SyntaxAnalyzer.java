package SyntaxAnalyze;

import SymbolTable.Symbol;
import SymbolTable.SymbolTableMap;
import WordAnalyze.WordAnalyzer;

import java.io.IOException;
import java.util.LinkedList;

import static SymbolTable.SymbolTableMap.nextLevel;
import static SymbolTable.SymbolTableMap.prevLevel;

public class SyntaxAnalyzer {
    private WordAnalyzer wordAnalyzer;
    private PCodeWriter pCodeWriter;
    public String token;
    private WordAnalyzer.Symbols symbolType;
    public int lineOffset;
    public int wordOffset;
    private LinkedList<SyntaxRead> buf = new LinkedList<>();
    private int cursor = -1;
    private int level = 0;
    private String num;
    public Errors error;

    public enum Errors {
        WordAnalyseError,
        InvalidConstantDeclaration,
        InvalidVariableDeclaration,
        DuplicateSymbol,
        SymbolNotFound,
        ExpectIdentifier,
        ExpectInt32,
        ExpectSemicolonOrComma,
        ExpectRightParenthesisOrComma,
        ExpectReturnType,
        InvalidFunctionDeclaration,
        InvalidArgumentDeclaration,
        MissingFunctionBody,
        InvalidSentenceSequence,
        InvalidExpression,
        InvalidFactor,
        InvalidIfStatement,
        InvalidWhileStatement,
        InvalidAssignment,
        InvalidReturn,
        InvalidScanf,
        InvalidPrintf,
        InvalidCall,
        InvalidCondition,
        LessArguments,
        MoreArguments,
        AssignToConstant,
        AssignToFunction,
        AssignWithDeclaration,
        MissingMain,
        MissingSemicolon,
        MissingSentence,
        ReturnValueForVoidFunction,
        NoReturnValueForIntFunction,
        NotCallingFunction,
        UnsupportedFeature,
        UnreadError
    }

    public SyntaxAnalyzer(WordAnalyzer wordAnalyzer, String outputPath) {
        this.wordAnalyzer = wordAnalyzer;
        this.pCodeWriter = new PCodeWriter(outputPath);
    }

    public void start() throws IOException {
        wordAnalyzer.getsym();
        while (wordAnalyzer.symbol != WordAnalyzer.Symbols.EOF) {
            buf.add(new SyntaxRead(wordAnalyzer.token, wordAnalyzer.symbol, wordAnalyzer.lineOffset, wordAnalyzer.wordOffset));
            wordAnalyzer.getsym();
            if (wordAnalyzer.error != null) {
                System.out.println(wordAnalyzer.error + " at " + wordAnalyzer.lineOffset + ":" + wordAnalyzer.wordOffset + " word: " + wordAnalyzer.token);
                return;
            }
        }
        nextLevel();
        program();
    }

    private void read() {
        ++cursor;
        SyntaxRead syntaxRead = buf.get(cursor);
        token = syntaxRead.token;
        symbolType = syntaxRead.symbolType;
        lineOffset = syntaxRead.lineOffset;
        wordOffset = syntaxRead.wordOffset;
    }

    private void unread() {
        if (cursor > 0) {
            --cursor;
            SyntaxRead syntaxRead = buf.get(cursor);
            token = syntaxRead.token;
            symbolType = syntaxRead.symbolType;
            lineOffset = syntaxRead.lineOffset;
            wordOffset = syntaxRead.wordOffset;
        } else if (cursor == 0) {
            --cursor;
            token = null;
            symbolType = null;
            lineOffset = 1;
            wordOffset = 0;
        } else {
            error = Errors.UnreadError;
        }
    }

    // 统一规定：true表示没有遇到错误，false表示遇到错误，成功后统一不read()，开始的时候必须read()!!! md，不read()真的烦死了

    // 整数
    private boolean integer() {
        boolean minus = false;
        read();
        if (symbolType == WordAnalyzer.Symbols.Plus) {
            read();
        } else if (symbolType == WordAnalyzer.Symbols.Minus) {
            minus = true;
            read();
        }
        if (symbolType == WordAnalyzer.Symbols.UnsignedInt) {
            num = wordAnalyzer.transIntNum(minus);
        } else if (symbolType == WordAnalyzer.Symbols.UnsignedFloat) {
            num = wordAnalyzer.transFloatNum(minus);
        } else {
            error = Errors.ExpectInt32;
            return false;
        }
        return true;
    }

    // 因子
    private boolean factor() {
        Symbol symbol;
        read();
        switch (symbolType) {
            case Identifier:
                if ((symbol = SymbolTableMap.findSymbol(token)) == null) {
                    error = Errors.SymbolNotFound;
                    return false;
                }
                if (symbol.type == SymbolTableMap.SymbolType.Int || symbol.type == SymbolTableMap.SymbolType.Float ||
                        symbol.type == SymbolTableMap.SymbolType.Const) {
                    return true;
                }
                unread();
                return callFunction();
            case LeftParenthesis:
                if (!expression()) {
                    return false;
                }
                read();
                if (symbolType == WordAnalyzer.Symbols.RightParenthesis) {
                    return true;
                } else {
                    error = Errors.InvalidFactor;
                    return false;
                }
            case UnsignedInt:
                return true;
            case UnsignedFloat:
                return true;
            default:
                error = Errors.InvalidFactor;
                return false;
        }
    }

    // 项
    private boolean item() {
        if (!factor()) {
            return false;
        }
        read();
        while (symbolType == WordAnalyzer.Symbols.Multi || symbolType == WordAnalyzer.Symbols.Div) {
            if (!factor()) {
                return false;
            }
            read();
        }
        unread();
        return true;
    }

    // 表达式
    private boolean expression() {
        boolean subzero = false;
        read();
        if (symbolType == WordAnalyzer.Symbols.Minus) {
            subzero = true;
        } else if (symbolType != WordAnalyzer.Symbols.Plus) {
            unread();
        }
        if (!item()) {
            return false;
        }
        read();
        while (symbolType == WordAnalyzer.Symbols.Plus || symbolType == WordAnalyzer.Symbols.Minus) {
            if (!item()) {
                return false;
            }
            read();
        }
        unread();
        return true;
    }

    // 条件
    private boolean condition() {
        if (!expression()) {
            return false;
        }
        read();
        if (symbolType == WordAnalyzer.Symbols.Less || symbolType == WordAnalyzer.Symbols.LessOrEqual ||
                symbolType == WordAnalyzer.Symbols.Greater || symbolType == WordAnalyzer.Symbols.GreaterOrEqual ||
                symbolType == WordAnalyzer.Symbols.Equal || symbolType == WordAnalyzer.Symbols.NotEqual) {
            // 后面可能会改成switch
            return expression();
        } else {
            unread();
            return true;
        }
    }

    // 返回语句
    private boolean returnAnalyse() {
        read();
        if (symbolType == WordAnalyzer.Symbols.LeftParenthesis) {
            if (!expression()) {
                return false;
            }
            read();
            if (symbolType == WordAnalyzer.Symbols.RightParenthesis) {
                return true;
            } else {
                error = Errors.InvalidReturn;
                return false;
            }
        } else if (symbolType == WordAnalyzer.Symbols.Semicolon) {
            return true;
        } else {
            error = Errors.InvalidReturn;
            return false;
        }
    }

    // 读语句
    private boolean printf() {
        read();
        if (symbolType != WordAnalyzer.Symbols.LeftParenthesis) {
            error = Errors.InvalidPrintf;
            return false;
        }
        read();
        if (symbolType == WordAnalyzer.Symbols.StringLiteral) {
            read();
            if (symbolType == WordAnalyzer.Symbols.RightParenthesis) {
                return true;
            }
            if (symbolType != WordAnalyzer.Symbols.Comma) {
                error = Errors.InvalidPrintf;
            }
            if (!expression()) {
                return false;
            }
        } else if (symbolType == WordAnalyzer.Symbols.RightParenthesis) {
            return true;
        } else {
            unread();
            if (!expression()) {
                return false;
            }
        }
        read();
        if (symbolType != WordAnalyzer.Symbols.RightParenthesis) {
            error = Errors.InvalidPrintf;
            return false;
        }
        return true;
    }

    // 读语句
    private boolean scanf() {
        read();
        if (symbolType != WordAnalyzer.Symbols.LeftParenthesis) {
            error = Errors.InvalidScanf;
            return false;
        }
        read();
        if (symbolType != WordAnalyzer.Symbols.Identifier) {
            error = Errors.ExpectIdentifier;
            return false;
        }
        read();
        if (symbolType != WordAnalyzer.Symbols.RightParenthesis) {
            error = Errors.InvalidScanf;
            return false;
        }
        return true;
    }

    // 函数调用语句
    private boolean callFunction() {
        Symbol symbol;
        read();
        if (symbolType != WordAnalyzer.Symbols.Identifier) {
            error = Errors.ExpectIdentifier;
            return false;
        }
        if ((symbol = SymbolTableMap.findSymbol(token)) == null) {
            error = Errors.SymbolNotFound;
            return false;
        }
        if (symbol.type != SymbolTableMap.SymbolType.IntFunction && symbol.type != SymbolTableMap.SymbolType.FloatFunction &&
                symbol.type != SymbolTableMap.SymbolType.VoidFunction) {
            error = Errors.InvalidCall;
            return false;
        }
        read();
        if (symbolType != WordAnalyzer.Symbols.LeftParenthesis) {
            error = Errors.InvalidCall;
            return false;
        }
        read();
        if (symbolType == WordAnalyzer.Symbols.RightParenthesis) {
            return true;
        } else {
            unread();
            while (expression()) {
                read();
                if (symbolType == WordAnalyzer.Symbols.RightParenthesis) {
                    return true;
                } else if (symbolType != WordAnalyzer.Symbols.Comma) {
                    error = Errors.ExpectRightParenthesisOrComma;
                    return false;
                }
            }
            return false;
        }
    }

    // 赋值语句
    private boolean assignStatement() {
        read();
        if (symbolType != WordAnalyzer.Symbols.Identifier) {
            error = Errors.ExpectIdentifier;
            return false;
        }
        read();
        if (symbolType != WordAnalyzer.Symbols.Assign) {
            return false;
        }
        return expression();
    }

    // 循环语句
    private boolean whileStatement() {
        read();
        if (symbolType != WordAnalyzer.Symbols.LeftParenthesis) {
            error = Errors.InvalidWhileStatement;
            return false;
        }
        if (!condition()) {
            return false;
        }
        read();
        if (symbolType != WordAnalyzer.Symbols.RightParenthesis) {
            error = Errors.InvalidWhileStatement;
            return false;
        }
        return statement();
    }

    // 条件语句
    private boolean ifStatement() {
        read();
        if (symbolType != WordAnalyzer.Symbols.LeftParenthesis) {
            error = Errors.InvalidIfStatement;
            return false;
        }
        if (!condition()) {
            return false;
        }
        read();
        if (symbolType != WordAnalyzer.Symbols.RightParenthesis) {
            error = Errors.InvalidIfStatement;
            return false;
        }
        if (!statement()) {
            return false;
        }
        read();
        if (symbolType == WordAnalyzer.Symbols.Else) {
            return statement();
        }
        unread();
        return true;
    }

    // 语句 在这里true表示已经成功处理，false表示没有语句或者遇到异常了
    private boolean statement() {
        Symbol symbol;
        read();
        switch (symbolType) {
            case If:
                return ifStatement();
            case While:
                return whileStatement();
            case LeftBrace:
                ++level;
                nextLevel();
                return statementSequence();
            case Identifier:
                if ((symbol = SymbolTableMap.findSymbol(token)) == null) {
                    error = Errors.SymbolNotFound;
                    return false;
                }
                switch (symbol.type) {
                    case Int:
                    case Float:
                        unread();
                        if (!assignStatement()) {
                            return false;
                        }
                        read();
                        if (symbolType != WordAnalyzer.Symbols.Semicolon) {
                            error = Errors.ExpectSemicolonOrComma;
                            return false;
                        }
                        return true;
                    case Const:
                        error = Errors.AssignToConstant;
                        return false;
                    case IntFunction:
                    case FloatFunction:
                    case VoidFunction:
                        unread();
                        if (!callFunction()) {
                            return false;
                        }
                        read();
                        if (symbolType != WordAnalyzer.Symbols.Semicolon) {
                            error = Errors.ExpectSemicolonOrComma;
                            return false;
                        }
                        return true;
                }
            case Scanf:
                if (!scanf()) {
                    return false;
                }
                read();
                if (symbolType != WordAnalyzer.Symbols.Semicolon) {
                    error = Errors.ExpectSemicolonOrComma;
                    return false;
                }
                return true;
            case Printf:
                if (!printf()) {
                    return false;
                }
                read();
                if (symbolType != WordAnalyzer.Symbols.Semicolon) {
                    error = Errors.ExpectSemicolonOrComma;
                    return false;
                }
                return true;
            case Return:
                if (!returnAnalyse()) {
                    return false;
                }
                read();
                if (symbolType != WordAnalyzer.Symbols.Semicolon) {
                    error = Errors.ExpectSemicolonOrComma;
                    return false;
                }
            case Semicolon:
                return true;
            default:
                error = Errors.InvalidArgumentDeclaration;
                return false;
                /*System.out.println("这句话不应该出现的");
                return false;*/
        }
    }

    // 语句序列
    private boolean statementSequence() {
        read();
        if (symbolType == WordAnalyzer.Symbols.RightBrace) {
            --level;
            prevLevel();
            return true;
        }
        unread();
        while (statement()) {
            if (error != null) {
                return false;
            }
            read();
            if (symbolType == WordAnalyzer.Symbols.RightBrace) {
                --level;
                prevLevel();
                return true;
            }
            unread();
        }
        return false;
    }

    // 参数
    private boolean argument() {
        SymbolTableMap.SymbolType type;
        read();
        if (symbolType == WordAnalyzer.Symbols.LeftParenthesis) {
            read();
            if (symbolType == WordAnalyzer.Symbols.RightParenthesis) {
                return true;
            }
            unread();
            while (true) {
                read();
                if (symbolType == WordAnalyzer.Symbols.Int) {
                    type = SymbolTableMap.SymbolType.Int;
                } else if (symbolType == WordAnalyzer.Symbols.Float) {
                    type = SymbolTableMap.SymbolType.Float;
                } else {
                    error = Errors.InvalidArgumentDeclaration;
                    return false;
                }
                read();
                if (symbolType != WordAnalyzer.Symbols.Identifier) {
                    error = Errors.ExpectIdentifier;
                    return false;
                }
                if (!SymbolTableMap.insertSymbol(token, type, level, lineOffset)) {
                    error = Errors.DuplicateSymbol;
                    return false;
                }
                read();
                if (symbolType == WordAnalyzer.Symbols.RightParenthesis) {
                    return true;
                } else if (symbolType != WordAnalyzer.Symbols.Comma) {
                    error = Errors.ExpectSemicolonOrComma;
                    return false;
                }
            }
        } else {
            error = Errors.InvalidArgumentDeclaration;
            return false;
        }
    }

    private void writeSymbols() {
        pCodeWriter.write("MKS", 0, 0);
        /*for i := 0; i < len(globals); i++ {
            pCodeWriter.target.PushBackInstruction(pcode.LIT, 0, 0)
        }
        pCodeWriter.target.PushBackInstruction(pcode.MKS, 0, 0)
        for i := 0; i < len(mainargs)+len(mainlocals); i++ {
            pCodeWriter.target.PushBackInstruction(pcode.LIT, 0, 0)
        }
        calladdress := pCodeWriter.target.GetNextAddress()
        pCodeWriter.target.PushBackInstruction(pcode.CAL, 0, -1)
        pCodeWriter.target.PushBackInstruction(pcode.STO, 0, 4)
        pCodeWriter.target.PushBackInstruction(pcode.RET, 0, 0)
        pCodeWriter.target.UpdateInstruction(calladdress, 0, int32(pCodeWriter.target.GetNextAddress()))*/
    }

    // 函数定义部分
    private boolean functionDefinition() {
        SymbolTableMap.SymbolType type;
        read();
        switch (symbolType) {
            case Int:
                type = SymbolTableMap.SymbolType.IntFunction;
                break;
            case Float:
                type = SymbolTableMap.SymbolType.FloatFunction;
                break;
            case Void:
                type = SymbolTableMap.SymbolType.VoidFunction;
                break;
            case Const:
                error = Errors.InvalidConstantDeclaration;
                return false;
            case LeftBrace:
            case RightBrace:
            case LeftParenthesis:
            case RightParenthesis:
                error = Errors.InvalidSentenceSequence;
                return false;
            default:
                error = Errors.ExpectReturnType;
                return false;
        }
        read();
        if (symbolType == WordAnalyzer.Symbols.Main) {
            if (type == SymbolTableMap.SymbolType.FloatFunction) {
                error = Errors.InvalidReturn;
                return false;
            }
            writeSymbols();
        }
        if (symbolType != WordAnalyzer.Symbols.Main && symbolType != WordAnalyzer.Symbols.Identifier) {
            error = Errors.ExpectIdentifier;
            return false;
        }
        if (!SymbolTableMap.createFunction(token, type, lineOffset)) {
            error = Errors.DuplicateSymbol;
            return false;
        }
        ++level;
        nextLevel();
        if (!argument()) {
            return false;
        }
        read();
        if (symbolType == WordAnalyzer.Symbols.LeftBrace) {
            if (!constantDeclaration()) {
                return false;
            }
            if (!variableDeclaration()) {
                return false;
            }
            if (!statementSequence()) {
                return false;
            }
        } else {
            error = Errors.MissingFunctionBody;
            return false;
        }
        if (type == SymbolTableMap.SymbolType.VoidFunction) {
            pCodeWriter.write("RET", 0, 0);
            return true;
        }
        pCodeWriter.write("LIT", 0, 0);
        pCodeWriter.write("STO", 0, 0);
        pCodeWriter.write("RET", 0, 0);
        return true;
    }

    // 变量说明部分
    private boolean variableDeclaration() {
        SymbolTableMap.SymbolType type;
        read();
        if (symbolType == WordAnalyzer.Symbols.Int) {
            type = SymbolTableMap.SymbolType.Int;
        } else if (symbolType == WordAnalyzer.Symbols.Float) {
            type = SymbolTableMap.SymbolType.Float;
        } else {
            // 不是声明头部
            unread();
            return true;
        }
        read();
        if (symbolType == WordAnalyzer.Symbols.Main) {
            unread();
            unread();
            return true;
        }
        if (symbolType != WordAnalyzer.Symbols.Identifier) {
            error = Errors.ExpectIdentifier;
            return false;
        }
        read();
        if (symbolType == WordAnalyzer.Symbols.Comma) {
            unread();
            if (!SymbolTableMap.insertSymbol(token, type, level, lineOffset)) {
                error = Errors.DuplicateSymbol;
                return false;
            }
            read();
            read();
            while (symbolType == WordAnalyzer.Symbols.Identifier) {
                if (!SymbolTableMap.insertSymbol(token, type, level, lineOffset)) {
                    error = Errors.DuplicateSymbol;
                    return false;
                }
                read();
                if (symbolType == WordAnalyzer.Symbols.Semicolon) {
                    return true;
                } else if (symbolType != WordAnalyzer.Symbols.Comma) {
                    error = Errors.ExpectSemicolonOrComma;
                    return false;
                }
                read();
            }
            error = Errors.ExpectIdentifier;
            return false;
        } else if (symbolType == WordAnalyzer.Symbols.Semicolon) {
            unread();
            if (!SymbolTableMap.insertSymbol(token, type, level, lineOffset)) {
                error = Errors.DuplicateSymbol;
                return false;
            }
            read();
            return true;
        } else if (symbolType == WordAnalyzer.Symbols.LeftParenthesis) {
            // 不是变量声明
            unread();
            unread();
            unread();
            return true;
        } else if (symbolType == WordAnalyzer.Symbols.Assign) {
            error = Errors.AssignWithDeclaration;
            return false;
        } else {
            error = Errors.InvalidVariableDeclaration;
            return false;
        }
    }

    // 常量说明部分
    private boolean constantDeclaration() {
        boolean exist = false;
        read();
        if (symbolType == WordAnalyzer.Symbols.Const) {
            read();
        } else {
            unread();
            return true;
        }
        while (symbolType == WordAnalyzer.Symbols.Identifier) {
            exist = true;
            if (!SymbolTableMap.insertSymbol(token, SymbolTableMap.SymbolType.Const, level, lineOffset)) {
                error = Errors.DuplicateSymbol;
                return false;
            }
            read();
            if (symbolType != WordAnalyzer.Symbols.Assign) {
                error = Errors.InvalidConstantDeclaration;
                return false;
            }
            read();
            if (symbolType != WordAnalyzer.Symbols.UnsignedInt) {
                error = Errors.ExpectInt32;
                return false;
            }
            read();
            if (symbolType == WordAnalyzer.Symbols.Semicolon) {
                return true;
            } else if (symbolType == WordAnalyzer.Symbols.Comma) {
                read();
            }
        }
        if (!exist) {
            error = Errors.ExpectIdentifier;
            return false;
        }
        unread();
        error = Errors.ExpectSemicolonOrComma;
        return false;
    }

    // 程序
    private void program() {
        if (!constantDeclaration()) {
            return;
        }
        if (!variableDeclaration()) {
            return;
        }
        while (functionDefinition()) {
            if (cursor == buf.size() - 1) {
                if (SymbolTableMap.findSymbol("main") == null) {
                    error = Errors.MissingMain;
                }
                return;
            }
        }
    }
}
